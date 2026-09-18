/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.*;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.assessedelement.NcAssessedElement;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithContingency;
import com.powsybl.security.monitor.StateMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class AssessedElementConverter extends AbstractNcConverter {
    private final Set<String> contingencyIds;

    private static final class EquipmentAccumulator {
        private final Set<String> branchIds = new LinkedHashSet<>();
        private final Set<String> voltageLevelIds = new LinkedHashSet<>();
        private final Set<String> threeWindingsTransformerIds = new LinkedHashSet<>();

        void add(MonitoredEquipment equipment) {
            branchIds.addAll(equipment.branchIds());
            voltageLevelIds.addAll(equipment.voltageLevelIds());
            threeWindingsTransformerIds.addAll(equipment.threeWindingsTransformerIds());
        }

        boolean isEmpty() {
            return branchIds.isEmpty() && voltageLevelIds.isEmpty() && threeWindingsTransformerIds.isEmpty();
        }
    }

    private record MonitoredEquipment(Set<String> branchIds, Set<String> voltageLevelIds,
                                      Set<String> threeWindingsTransformerIds) {
        Set<String> ids() {
            Set<String> ids = new LinkedHashSet<>(branchIds);
            ids.addAll(voltageLevelIds);
            ids.addAll(threeWindingsTransformerIds);
            return ids;
        }
    }

    record Result(List<StateMonitor> monitors, Map<String, Context> contexts) {
    }

    record Context(Set<String> equipmentIds, Set<String> contingencies) {
    }

    AssessedElementConverter(NcModel model, Network network, Set<String> contingencyIds, ReportNode reportNode) {
        super(model, network, reportNode);
        this.contingencyIds = Set.copyOf(contingencyIds);
    }

    Result convert() {
        EquipmentAccumulator preventiveEquipment = new EquipmentAccumulator();
        Map<String, Context> contexts = new LinkedHashMap<>();
        Map<String, EquipmentAccumulator> equipmentByContingency = new LinkedHashMap<>();
        Map<String, List<NcAssessedElementWithContingency>> links = model.getAssessedElementWithContingencies().stream()
            .collect(Collectors.groupingBy(NcAssessedElementWithContingency::assessedElement));

        contingencyIds.forEach(contingencyId -> equipmentByContingency.put(contingencyId, new EquipmentAccumulator()));

        for (NcAssessedElement assessedElement : model.getAssessedElements()) {
            process(assessedElement, links, preventiveEquipment, equipmentByContingency, contexts);
        }

        List<StateMonitor> monitors = createMonitors(preventiveEquipment, equipmentByContingency);

        return new Result(List.copyOf(monitors), Map.copyOf(contexts));
    }

    private void process(NcAssessedElement assessedElement, Map<String, List<NcAssessedElementWithContingency>> links,
                         EquipmentAccumulator preventiveEquipment,
                         Map<String, EquipmentAccumulator> equipmentByContingency,
                         Map<String, Context> contexts) {
        if (!assessedElement.enabled()) {
            NcConversionReports.assessedElementDisabled(reportNode, assessedElement.mrid());
            return;
        }

        MonitoredEquipment equipment = resolveEquipment(assessedElement);
        if (equipment == null) {
            return;
        }

        if (assessedElement.inBaseCase()) {
            preventiveEquipment.add(equipment);
        }

        List<NcAssessedElementWithContingency> assessedElementLinks = links.getOrDefault(assessedElement.mrid(), List.of());

        Set<String> linkedContingencies = resolveLinkedContingencies(assessedElement, assessedElementLinks,
            equipmentByContingency.keySet(), assessedElement.conductingEquipment());

        if (!assessedElementLinks.isEmpty() && linkedContingencies.isEmpty()) {
            NcConversionReports.assessedElementWithoutImportedContingency(reportNode, assessedElement.mrid(),
                assessedElement.conductingEquipment());
            return;
        }

        linkedContingencies.forEach(contingency -> equipmentByContingency.get(contingency).add(equipment));
        contexts.put(assessedElement.mrid(), new Context(equipment.ids(), linkedContingencies));
    }

    private MonitoredEquipment resolveEquipment(NcAssessedElement assessedElement) {
        if (assessedElement.conductingEquipment() == null) {
            NcConversionReports.assessedElementWithoutConductingEquipment(reportNode, assessedElement.mrid());
            return null;
        }
        var identifiable = network.getIdentifiable(assessedElement.conductingEquipment());

        if (identifiable == null) {
            NcConversionReports.assessedElementEquipmentNotFound(reportNode, assessedElement.conductingEquipment(),
                assessedElement.mrid());
            return null;
        }

        if (identifiable instanceof Branch<?> branch) {
            Set<String> voltageLevelIds = resolveVoltageLevelIds(List.of(branch.getTerminal1(), branch.getTerminal2()));
            return new MonitoredEquipment(Set.of(branch.getId()), voltageLevelIds, Set.of());
        }

        if (identifiable instanceof ThreeWindingsTransformer transformer) {
            Set<String> voltageLevelIds = resolveVoltageLevelIds(transformer.getTerminals());
            return new MonitoredEquipment(Set.of(), voltageLevelIds, Set.of(transformer.getId()));
        }

        if (identifiable instanceof Connectable<?> connectable) {
            Set<String> voltageLevelIds = resolveVoltageLevelIds(connectable.getTerminals());
            return new MonitoredEquipment(Set.of(), voltageLevelIds, Set.of());
        }

        if (identifiable instanceof Switch swt) {
            return new MonitoredEquipment(Set.of(), Set.of(swt.getVoltageLevel().getId()), Set.of());
        }

        NcConversionReports.assessedElementEquipmentNotSupported(reportNode, assessedElement.conductingEquipment(),
            assessedElement.mrid());
        return null;
    }

    private static Set<String> resolveVoltageLevelIds(Collection<? extends Terminal> terminals) {
        return terminals.stream()
            .map(Terminal::getVoltageLevel)
            .filter(AssessedElementConverter::hasVoltageLimits)
            .map(VoltageLevel::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean hasVoltageLimits(VoltageLevel voltageLevel) {
        return !Double.isNaN(voltageLevel.getLowVoltageLimit())
            || !Double.isNaN(voltageLevel.getHighVoltageLimit());
    }

    private Set<String> resolveLinkedContingencies(NcAssessedElement assessedElement,
                                                   List<NcAssessedElementWithContingency> assessedElementLinks,
                                                   Set<String> importedContingencies, String equipmentId) {
        if (assessedElement.isCombinableWithContingency()) {
            if (!assessedElementLinks.isEmpty()) {
                NcConversionReports.assessedElementCombinableAssociationsIgnored(reportNode, assessedElement.mrid());
            }
            return new LinkedHashSet<>(importedContingencies);
        }

        Set<String> linkedContingencies = new LinkedHashSet<>();
        for (NcAssessedElementWithContingency link : assessedElementLinks) {
            if (link.isExcluded()) {
                NcConversionReports.assessedElementExcludedAssociationNotSupported(reportNode, link.contingency(),
                    assessedElement.mrid());
            } else if (!link.enabled()) {
                NcConversionReports.assessedElementAssociationDisabled(reportNode, link.contingency(),
                    assessedElement.mrid());
            } else if (!link.isIncluded()) {
                NcConversionReports.assessedElementAssociationKindNotSupported(reportNode, link.contingency(),
                    assessedElement.mrid(), link.combinationConstraintKind());
            } else if (!importedContingencies.contains(link.contingency())) {
                NcConversionReports.assessedElementAssociationContingencyNotImported(reportNode, link.contingency(),
                    assessedElement.mrid(), equipmentId);
            } else {
                linkedContingencies.add(link.contingency());
            }
        }
        return linkedContingencies;
    }

    private static List<StateMonitor> createMonitors(EquipmentAccumulator preventiveEquipment,
                                                     Map<String, EquipmentAccumulator> equipmentByContingency) {
        List<StateMonitor> monitors = new ArrayList<>();

        if (!preventiveEquipment.isEmpty()) {
            monitors.add(new StateMonitor(ContingencyContext.none(), preventiveEquipment.branchIds,
                preventiveEquipment.voltageLevelIds, preventiveEquipment.threeWindingsTransformerIds));
        }

        equipmentByContingency.forEach((contingency, equipment) -> {
            if (!equipment.isEmpty()) {
                monitors.add(new StateMonitor(ContingencyContext.specificContingency(contingency), equipment.branchIds,
                    equipment.voltageLevelIds, equipment.threeWindingsTransformerIds));
            }
        });

        return monitors;
    }
}
