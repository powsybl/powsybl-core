/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.ImporterContext;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.nc.NcConverter.LOGGER;
import static com.powsybl.nc.reader.ReaderUtils.MRID;

/**
 * TODO: Currently only handles line/branch AEs based on a conducting equipment.
 * TODO: Implicit links due to isCombinableWithContingency not yet handled, explicit links need to be made. + allow exclusion
 * TODO: use flowReliabilityMargin to generate limit reductions
 *
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class AssessedElementReader extends AbstractReader<StateMonitor> {
    private static final String COMBINATION_CONSTRAINT_KIND_INCLUDED = "http://entsoe.eu/ns/nc#ElementCombinationConstraintKind.included";

    private final Set<String> preventiveBranchIds;
    private final Map<String, Set<String>> curativeBranchIds;

    public AssessedElementReader(QueryManager queryManager,
                                 ImporterContext importerContext,
                                 Network network,
                                 Set<Contingency> contingencies) {
        super(queryManager, importerContext, network);
        this.preventiveBranchIds = new HashSet<>();
        this.curativeBranchIds = contingencies.stream()
            .collect(Collectors.toMap(Contingency::getId, k -> new HashSet<>()));
    }

    @Override
    public Set<StateMonitor> readFromProfiles() {
        Set<StateMonitor> stateMonitors = new HashSet<>();
        PropertyBags assessedElements = queryManager.query("assessedElement", NcProfile.ASSESSED_ELEMENT);
        PropertyBags assessedElementWithContingencies = queryManager.query("assessedElementWithContingency", NcProfile.ASSESSED_ELEMENT);
        Map<String, Set<PropertyBag>> contingenciesPerAssessedElement = ReaderUtils.groupOnAttribute(assessedElementWithContingencies, "assessedElement", true);
        assessedElements.forEach(assessedElement ->
            processAssessedElement(assessedElement, contingenciesPerAssessedElement.getOrDefault(assessedElement.get(MRID), Set.of())));

        if (!preventiveBranchIds.isEmpty()) {
            stateMonitors.add(new StateMonitor(ContingencyContext.none(), preventiveBranchIds, Set.of(), Set.of()));
        }

        curativeBranchIds.forEach((contingencyId, branchIds) -> {
            if (!branchIds.isEmpty()) {
                stateMonitors.add(new StateMonitor(ContingencyContext.specificContingency(contingencyId), branchIds, Set.of(), Set.of()));
            }
        });

        return stateMonitors;
    }

    private void processAssessedElement(PropertyBag assessedElement, Set<PropertyBag> contingenciesWithAssessedElement) {
        String assessedElementId = assessedElement.get(MRID);

        boolean normalEnabled = Boolean.parseBoolean(assessedElement.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.warn("AssessedElement {} is disabled and will be ignored.", assessedElementId);
            return;
        }

        String conductingEquipment = ReaderUtils.getElementIdFromResourceUri(assessedElement.get("conductingEquipment"));
        if (conductingEquipment == null) {
            LOGGER.warn("AssessedElement {} has no conductingEquipment and will be ignored.", assessedElementId);
            return;
        }

        Branch<?> branch = network.getBranch(conductingEquipment);
        if (branch == null) {
            LOGGER.warn("ConductingEquipment {} belonging to AssessedElement {} is not a branch. AssessedElement will be ignored.",
                conductingEquipment, assessedElementId);
            return;
        }

        boolean inBaseCase = Boolean.parseBoolean(assessedElement.get("inBaseCase"));
        if (inBaseCase) {
            preventiveBranchIds.add(branch.getId());
        }

        Set<String> contingencies = new HashSet<>();
        for (PropertyBag contingencyWithAssessedElement : contingenciesWithAssessedElement) {
            String contingencyId = ReaderUtils.getElementIdFromResourceUri(contingencyWithAssessedElement.get("contingency"));

            if (!curativeBranchIds.containsKey(contingencyId)) {
                LOGGER.info("Contingency {} associated to AssessedElement {} was not imported. The branch {} will not be monitored for this contingency.",
                    contingencyId, assessedElementId, branch.getId());
            } else if (!COMBINATION_CONSTRAINT_KIND_INCLUDED.equals(contingencyWithAssessedElement.get("combinationConstraintKind"))) {
                LOGGER.info("Association between Contingency {} and AssessedElement {} is not included. The branch {} will not be monitored for this contingency.",
                    contingencyId, assessedElementId, branch.getId());
            } else if (!Boolean.parseBoolean(contingencyWithAssessedElement.getOrDefault("normalEnabled", "true"))) {
                LOGGER.info("Association between Contingency {} and AssessedElement {} is disabled. The branch {} will not be monitored for this contingency.",
                    contingencyId, assessedElementId, branch.getId());
            } else {
                curativeBranchIds.get(contingencyId).add(branch.getId());
                contingencies.add(contingencyId);
            }
        }

        if (!contingenciesWithAssessedElement.isEmpty() && contingencies.isEmpty()) {
            LOGGER.warn("No Contingency associated to AssessedElement {} was properly imported. The branch {} will not be monitored in curative.",
                assessedElementId, branch.getId());
            return;
        }

        // TODO: handle flow reliability margin
        double flowReliabilityMargin;
        if (assessedElement.get("flowReliabilityMargin") != null) {
            try {
                flowReliabilityMargin = Double.parseDouble(assessedElement.get("flowReliabilityMargin"));
            } catch (NumberFormatException e) {
                flowReliabilityMargin = 0.0;
            }
        } else {
            flowReliabilityMargin = 0.0;
        }

        // store AssessedElement native data in context for later use
        importerContext.addImportedAssessedElement(assessedElementId, branch, inBaseCase, contingencies);
    }
}
