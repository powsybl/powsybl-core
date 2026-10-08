/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.converter.util.UcteExporterReports;
import com.powsybl.ucte.network.UcteCountryCode;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The voltage levels to export and their UCTE country. Voltage levels without substation ("orphans") get a country
 * computed from their connectivity; those for which none can be found are excluded from the export. Never modifies
 * the network.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
public final class ExportedVoltageLevels {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportedVoltageLevels.class);

    private static final ExportedVoltageLevels ALL = new ExportedVoltageLevels(Map.of(), Set.of());

    private final Map<String, UcteCountryCode> orphanCountries;
    private final Set<String> excludedVoltageLevelIds;

    private ExportedVoltageLevels(Map<String, UcteCountryCode> orphanCountries, Set<String> excludedVoltageLevelIds) {
        this.orphanCountries = Map.copyOf(orphanCountries);
        this.excludedVoltageLevelIds = Set.copyOf(excludedVoltageLevelIds);
    }

    /**
     * No voltage level is excluded, and no country is computed: for naming strategies that do not need countries.
     */
    public static ExportedVoltageLevels all() {
        return ALL;
    }

    /**
     * Validates the substation countries and computes the country of every voltage level without substation:
     * <ul>
     *     <li>a substation without country, or with a country not supported by UCTE-DEF, is rejected;</li>
     *     <li>if the network has exactly one country, every orphan voltage level gets it;</li>
     *     <li>otherwise, an orphan voltage level gets the country of the substations of its connected component,
     *     considering lines only (whatever their status; tie lines and boundary lines are not considered). A
     *     component with several countries is rejected. A component without country is isolated: rejected if one of its
     *     voltage levels holds equipment (busbar sections aside), excluded from the export and reported otherwise.</li>
     * </ul>
     */
    public static ExportedVoltageLevels compute(Network network, ReportNode reportNode) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(reportNode);
        Set<UcteCountryCode> countries = EnumSet.noneOf(UcteCountryCode.class);
        network.getSubstations().forEach(substation -> countries.add(substationCountry(substation)));

        List<VoltageLevel> orphans = network.getVoltageLevelStream().filter(ExportedVoltageLevels::isOrphan).toList();
        if (orphans.isEmpty()) {
            return new ExportedVoltageLevels(Map.of(), Set.of());
        }
        if (countries.size() == 1) {
            UcteCountryCode country = countries.iterator().next();
            Map<String, UcteCountryCode> orphanCountries = new HashMap<>();
            orphans.forEach(orphan -> orphanCountries.put(orphan.getId(), country));
            return new ExportedVoltageLevels(orphanCountries, Set.of());
        }
        return computeFromConnectivity(network, reportNode);
    }

    /**
     * Gives each orphan voltage level the country of the substations of its connected component, in a graph whose
     * vertices are the voltage levels and whose edges are the lines touching an orphan voltage level. Rejects a
     * component with several countries, and handles components without country with
     * {@link #excludeIsolatedOrphans(List, ReportNode)}.
     */
    private static ExportedVoltageLevels computeFromConnectivity(Network network, ReportNode reportNode) {
        Graph<String, Line> graph = new Pseudograph<>(Line.class);
        network.getVoltageLevelStream().forEach(voltageLevel -> graph.addVertex(voltageLevel.getId()));
        network.getLineStream().forEach(line -> {
            VoltageLevel voltageLevel1 = line.getTerminal1().getVoltageLevel();
            VoltageLevel voltageLevel2 = line.getTerminal2().getVoltageLevel();
            if (isOrphan(voltageLevel1) || isOrphan(voltageLevel2)) {
                graph.addEdge(voltageLevel1.getId(), voltageLevel2.getId(), line);
            }
        });

        Map<String, UcteCountryCode> orphanCountries = new HashMap<>();
        List<VoltageLevel> isolatedOrphans = new ArrayList<>();
        for (Set<String> component : new ConnectivityInspector<>(graph).connectedSets()) {
            List<VoltageLevel> voltageLevels = component.stream().map(network::getVoltageLevel).toList();
            List<VoltageLevel> componentOrphans = voltageLevels.stream()
                    .filter(ExportedVoltageLevels::isOrphan)
                    .sorted(Comparator.comparing(Identifiable::getId))
                    .toList();
            if (componentOrphans.isEmpty()) {
                continue;
            }
            Set<UcteCountryCode> componentCountries = voltageLevels.stream()
                    .flatMap(voltageLevel -> voltageLevel.getSubstation().stream())
                    .map(ExportedVoltageLevels::substationCountry)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(UcteCountryCode.class)));
            if (componentCountries.isEmpty()) {
                isolatedOrphans.addAll(componentOrphans);
            } else if (componentCountries.size() == 1) {
                UcteCountryCode country = componentCountries.iterator().next();
                componentOrphans.forEach(orphan -> orphanCountries.put(orphan.getId(), country));
            } else {
                throw new UcteException(
                        String.format("Cannot determine the country of voltage levels %s: they are connected to " +
                                        "substations of several countries %s",
                                componentOrphans.stream().map(Identifiable::getId).toList(),
                                componentCountries)
                );
            }
        }
        return new ExportedVoltageLevels(orphanCountries, excludeIsolatedOrphans(isolatedOrphans, reportNode));
    }

    private static Set<String> excludeIsolatedOrphans(List<VoltageLevel> isolatedOrphans, ReportNode reportNode) {
        List<String> withEquipment = isolatedOrphans.stream()
                .sorted(Comparator.comparing(Identifiable::getId))
                .filter(orphan -> !equipmentIds(orphan).isEmpty())
                .map(orphan -> orphan.getId() + " " + equipmentIds(orphan))
                .toList();
        if (!withEquipment.isEmpty()) {
            throw new UcteException("Voltage levels connected to no substation cannot hold equipment: " + String.join(", ", withEquipment));
        }
        Set<String> excluded = new HashSet<>();
        isolatedOrphans.stream().sorted(Comparator.comparing(Identifiable::getId)).forEach(orphan -> {
            LOGGER.warn("Voltage level {} is connected to no substation and holds no equipment: it is not exported", orphan.getId());
            UcteExporterReports.orphanVoltageLevelNotExported(reportNode, orphan.getId());
            excluded.add(orphan.getId());
        });
        return excluded;
    }

    /**
     * Ids of the switches and of the connectables other than busbar sections, sorted.
     */
    private static List<String> equipmentIds(VoltageLevel voltageLevel) {
        Stream<String> connectableIds = voltageLevel.getConnectableStream()
                .filter(connectable -> connectable.getType() != IdentifiableType.BUSBAR_SECTION)
                .map(Identifiable::getId);
        Stream<String> switchIds = StreamSupport.stream(voltageLevel.getSwitches().spliterator(), false).map(Identifiable::getId);
        return Stream.concat(connectableIds, switchIds).sorted().toList();
    }

    private static boolean isOrphan(VoltageLevel voltageLevel) {
        return voltageLevel.getSubstation().isEmpty();
    }

    public UcteCountryCode getCountry(VoltageLevel voltageLevel) {
        Optional<Substation> substation = voltageLevel.getSubstation();
        if (substation.isPresent()) {
            return substationCountry(substation.get());
        }
        UcteCountryCode country = orphanCountries.get(voltageLevel.getId());
        if (country == null) {
            throw new UcteException("Voltage level " + voltageLevel.getId() + " is connected to no substation: no country");
        }
        return country;
    }

    public boolean isExported(VoltageLevel voltageLevel) {
        return !excludedVoltageLevelIds.contains(voltageLevel.getId());
    }

    private static UcteCountryCode substationCountry(Substation substation) {
        Country country = substation.getCountry()
                .orElseThrow(() -> new UcteException("Substation " + substation.getId() + " has no country"));
        return UcteCountryCode.fromCountry(country);
    }
}
