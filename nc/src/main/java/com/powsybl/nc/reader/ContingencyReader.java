/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.contingency.BatteryContingency;
import com.powsybl.contingency.BoundaryLineContingency;
import com.powsybl.contingency.BusContingency;
import com.powsybl.contingency.BusbarSectionContingency;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.DcGroundContingency;
import com.powsybl.contingency.DcLineContingency;
import com.powsybl.contingency.GeneratorContingency;
import com.powsybl.contingency.HvdcLineContingency;
import com.powsybl.contingency.LineContingency;
import com.powsybl.contingency.LoadContingency;
import com.powsybl.contingency.ShuntCompensatorContingency;
import com.powsybl.contingency.StaticVarCompensatorContingency;
import com.powsybl.contingency.SwitchContingency;
import com.powsybl.contingency.ThreeWindingsTransformerContingency;
import com.powsybl.contingency.TieLineContingency;
import com.powsybl.contingency.TwoWindingsTransformerContingency;
import com.powsybl.contingency.VoltageSourceConverterContingency;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ContingencyReader extends AbstractReader<Contingency> {
    private static final String OUT_OF_SERVICE_CONTINGENT_STATUS = "http://iec.ch/TC57/CIM100#ContingencyEquipmentStatusKind.outOfService";

    public ContingencyReader(QueryManager queryManager, Network network) {
        super(queryManager, network);
    }

    @Override
    public Set<Contingency> readFromProfiles() {
        Set<Contingency> contingencies = new HashSet<>();
        PropertyBags ordinaryContingencies = queryManager.query("ordinaryContingency", NcProfile.CONTINGENCY);
        PropertyBags exceptionalContingencies = queryManager.query("exceptionalContingency", NcProfile.CONTINGENCY);
        PropertyBags outOfRangeContingencies = queryManager.query("outOfRangeContingency", NcProfile.CONTINGENCY);
        PropertyBags contingencyEquipments = queryManager.query("contingencyEquipment", NcProfile.CONTINGENCY);
        Map<String, Set<PropertyBag>> contingencyElementsPerContingency = groupContingencyElementsPerContingency(contingencyEquipments);
        Set<PropertyBag> allContingencies = new HashSet<>(ordinaryContingencies);
        allContingencies.addAll(exceptionalContingencies);
        allContingencies.addAll(outOfRangeContingencies);
        for (PropertyBag contingency : allContingencies) {
            Optional<Contingency> contingencyOpt = processContingency(contingency, contingencyElementsPerContingency.getOrDefault(contingency.get("mRID"), Set.of()));
            contingencyOpt.ifPresent(contingencies::add);
        }
        return contingencies;
    }

    private static Map<String, Set<PropertyBag>> groupContingencyElementsPerContingency(PropertyBags contingencyElements) {
        Map<String, Set<PropertyBag>> contingencyElementsPerContingency = new HashMap<>();
        contingencyElements.forEach(
            contingencyElement -> contingencyElementsPerContingency.computeIfAbsent(ReaderUtils.getElementIdFromResourceUri(contingencyElement.get("contingency")),
                k -> new HashSet<>()).add(contingencyElement));
        return contingencyElementsPerContingency;
    }

    private Optional<Contingency> processContingency(PropertyBag contingency, Set<PropertyBag> contingencyEquipments) {
        String contingencyId = contingency.get("mRID");
        String contingencyName = contingency.get("name");

        boolean mustStudy = Boolean.parseBoolean(contingency.get("normalMustStudy"));
        if (!mustStudy) {
            LOGGER.warn("Contingency {} should not be studied and will be ignored.", contingencyId);
            return Optional.empty();
        }

        List<ContingencyElement> contingencyElements = new ArrayList<>();
        for (PropertyBag contingencyEquipment : contingencyEquipments) {
            Optional<ContingencyElement> contingencyElement = processContingencyElement(contingencyEquipment, contingencyId);
            contingencyElement.ifPresent(contingencyElements::add);
        }

        if (contingencyElements.isEmpty()) {
            LOGGER.warn("Contingency {} does not contain any valid equipment and will be ignored.", contingencyId);
            return Optional.empty();
        }

        return Optional.of(new Contingency(contingencyId, contingencyName, contingencyElements));
    }

    private Optional<ContingencyElement> processContingencyElement(PropertyBag contingencyEquipment, String contingencyId) {
        String equipment = ReaderUtils.getElementIdFromResourceUri(contingencyEquipment.get("equipment"));
        String contingentStatus = contingencyEquipment.get("contingentStatus");

        if (!OUT_OF_SERVICE_CONTINGENT_STATUS.equals(contingentStatus)) {
            LOGGER.info("ContingencyEquipment with equipment {} associated to Contingency {} must not be put out of service and will be ignored.", equipment, contingencyId);
            return Optional.empty();
        }

        Identifiable<?> networkElement = network.getIdentifiable(equipment);
        if (networkElement == null) {
            LOGGER.info("ContingencyEquipment with equipment {} associated to Contingency {} refers to a non-existing network element and will be ignored.", equipment, contingencyId);
            return Optional.empty();
        }

        return switch (networkElement.getType()) {
            case BATTERY -> Optional.of(new BatteryContingency(equipment));
            case BOUNDARY_LINE -> Optional.of(new BoundaryLineContingency(equipment));
            case BUS, DC_BUS -> Optional.of(new BusContingency(equipment));
            case BUSBAR_SECTION -> Optional.of(new BusbarSectionContingency(equipment));
            case DC_GROUND -> Optional.of(new DcGroundContingency(equipment));
            case DC_LINE -> Optional.of(new DcLineContingency(equipment));
            case GENERATOR -> Optional.of(new GeneratorContingency(equipment));
            case HVDC_LINE -> Optional.of(new HvdcLineContingency(equipment));
            case LINE -> Optional.of(new LineContingency(equipment));
            case LOAD -> Optional.of(new LoadContingency(equipment));
            case SHUNT_COMPENSATOR -> Optional.of(new ShuntCompensatorContingency(equipment));
            case STATIC_VAR_COMPENSATOR -> Optional.of(new StaticVarCompensatorContingency(equipment));
            case SWITCH, DC_SWITCH -> Optional.of(new SwitchContingency(equipment));
            case THREE_WINDINGS_TRANSFORMER -> Optional.of(new ThreeWindingsTransformerContingency(equipment));
            case TIE_LINE -> Optional.of(new TieLineContingency(equipment));
            case TWO_WINDINGS_TRANSFORMER -> Optional.of(new TwoWindingsTransformerContingency(equipment));
            case VOLTAGE_SOURCE_CONVERTER -> Optional.of(new VoltageSourceConverterContingency(equipment));
            default -> {
                LOGGER.warn("ContingencyEquipment with equipment {} associated to Contingency {} is not suitable to define a contingency and will be ignored.", equipment, contingencyId);
                yield Optional.empty();
            }
        };

    }
}
