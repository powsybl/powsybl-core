/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.action.Action;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.condition.AtLeastOneViolationCondition;
import com.powsybl.contingency.strategy.condition.Condition;
import com.powsybl.contingency.strategy.condition.TrueCondition;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithRemedialAction;
import com.powsybl.nc.model.contingency.NcContingencyWithRemedialAction;
import com.powsybl.nc.model.remedialaction.NcGridStateAlteration;
import com.powsybl.nc.model.remedialaction.NcGridStateAlterationRemedialAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class OperatorStrategyConverter extends AbstractNcConverter {
    private static final String PREVENTIVE_KIND = "http://entsoe.eu/ns/nc#RemedialActionKind.preventive";
    private static final String CURATIVE_KIND = "http://entsoe.eu/ns/nc#RemedialActionKind.curative";

    private final Set<String> contingencyIds;
    private final Map<String, AssessedElementConverter.Context> assessedElementContexts;
    private final Map<String, Action> actions;

    OperatorStrategyConverter(NcModel model, Network network, Set<String> contingencyIds,
                              Map<String, AssessedElementConverter.Context> assessedElementContexts,
                              Map<String, Action> actions, ReportNode reportNode) {
        super(model, network, reportNode);
        this.contingencyIds = Set.copyOf(contingencyIds);
        this.assessedElementContexts = Map.copyOf(assessedElementContexts);
        this.actions = Map.copyOf(actions);
    }

    List<OperatorStrategy> convert() {
        Map<String, List<NcGridStateAlteration>> alterations = collectAlterations();

        Map<String, List<NcContingencyWithRemedialAction>> contingencyLinks = model.getContingencyWithRemedialActions().stream()
            .collect(Collectors.groupingBy(NcContingencyWithRemedialAction::remedialAction));

        Map<String, List<NcAssessedElementWithRemedialAction>> assessedElementLinks = model.getAssessedElementWithRemedialActions().stream()
            .collect(Collectors.groupingBy(NcAssessedElementWithRemedialAction::remedialAction));

        List<OperatorStrategy> strategies = new ArrayList<>();

        for (NcGridStateAlterationRemedialAction remedialAction : model.getGridStateAlterationRemedialActions()) {
            strategies.addAll(process(remedialAction, alterations, contingencyLinks, assessedElementLinks));
        }

        return List.copyOf(strategies);
    }

    private List<OperatorStrategy> process(NcGridStateAlterationRemedialAction remedialAction,
                                           Map<String, List<NcGridStateAlteration>> alterations,
                                           Map<String, List<NcContingencyWithRemedialAction>> contingencyLinks,
                                           Map<String, List<NcAssessedElementWithRemedialAction>> assessedElementLinks) {
        if (!remedialAction.available()) {
            return List.of();
        }

        List<String> actionIds = resolveActionIds(remedialAction, alterations);
        if (actionIds == null) {
            return List.of();
        }
        Condition condition = resolveCondition(assessedElementLinks.getOrDefault(remedialAction.mrid(), List.of()));

        if (PREVENTIVE_KIND.equals(remedialAction.kind())) {
            return List.of(new OperatorStrategy(remedialAction.mrid(), ContingencyContext.none(), condition, actionIds));
        }
        if (!CURATIVE_KIND.equals(remedialAction.kind())) {
            NcConversionReports.remedialActionKindNotSupported(reportNode, remedialAction.mrid(), remedialAction.kind());
            return List.of();
        }

        List<String> linkedContingencies = resolveContingencies(
            contingencyLinks.getOrDefault(remedialAction.mrid(), List.of()));
        if (linkedContingencies.isEmpty()) {
            return List.of(new OperatorStrategy(remedialAction.mrid(), ContingencyContext.onlyContingencies(), condition, actionIds));
        }
        return linkedContingencies.stream()
            .map(contingency -> new OperatorStrategy(remedialAction.mrid() + "@" + contingency,
                ContingencyContext.specificContingency(contingency), condition, actionIds))
            .toList();
    }

    private List<String> resolveActionIds(NcGridStateAlterationRemedialAction remedialAction,
                                          Map<String, List<NcGridStateAlteration>> alterations) {
        List<NcGridStateAlteration> nativeActions = alterations.getOrDefault(remedialAction.mrid(), List.of());
        List<String> actionIds = nativeActions.stream()
            .map(NcGridStateAlteration::mrid)
            .filter(actions::containsKey)
            .toList();
        if (actionIds.size() != nativeActions.size()) {
            NcConversionReports.remedialActionWithUnconvertedAction(reportNode, remedialAction.mrid());
            return null;
        }
        return actionIds;
    }

    private List<String> resolveContingencies(List<NcContingencyWithRemedialAction> links) {
        return links.stream()
            .filter(NcContingencyWithRemedialAction::enabled)
            .filter(NcContingencyWithRemedialAction::isIncluded)
            .map(NcContingencyWithRemedialAction::contingency)
            .filter(contingencyIds::contains)
            .distinct()
            .toList();
    }

    private Condition resolveCondition(List<NcAssessedElementWithRemedialAction> links) {
        List<String> equipmentIds = links.stream()
            .filter(NcAssessedElementWithRemedialAction::enabled)
            .filter(NcAssessedElementWithRemedialAction::isIncluded)
            .map(NcAssessedElementWithRemedialAction::assessedElement)
            .map(assessedElementContexts::get)
            .filter(Objects::nonNull)
            .flatMap(context -> context.equipmentIds().stream())
            .distinct()
            .toList();
        return equipmentIds.isEmpty() ? new TrueCondition() : new AtLeastOneViolationCondition(equipmentIds);
    }

    private Map<String, List<NcGridStateAlteration>> collectAlterations() {
        Map<String, List<NcGridStateAlteration>> result = new LinkedHashMap<>();
        addAlterations(result, model.getTopologyActions());
        addAlterations(result, model.getShuntCompensatorModifications());
        addAlterations(result, model.getRotatingMachineActions());
        addAlterations(result, model.getTapPositionActions());
        return result;
    }

    private static void addAlterations(Map<String, List<NcGridStateAlteration>> target,
                                       Collection<? extends NcGridStateAlteration> alterations) {
        for (NcGridStateAlteration alteration : alterations) {
            if (alteration.gridStateAlterationRemedialAction() != null) {
                target.computeIfAbsent(alteration.gridStateAlterationRemedialAction(), ignored -> new ArrayList<>()).add(alteration);
            }
        }
    }
}
