/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.Action;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.condition.TrueCondition;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.nc.NcConverter.LOGGER;
import static com.powsybl.nc.reader.ReaderUtils.MRID;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class GridStateAlterationRemedialActionReader extends AbstractReader<OperatorStrategy> {
    private static final String PREVENTIVE_KIND = "http://entsoe.eu/ns/nc#RemedialActionKind.preventive";
    private static final String CURATIVE_KIND = "http://entsoe.eu/ns/nc#RemedialActionKind.curative";
    private static final String COMBINATION_CONSTRAINT_KIND_INCLUDED = "http://entsoe.eu/ns/nc#ElementCombinationConstraintKind.included";

    private final Set<String> importedActions;
    private final Set<String> importedContingencies;

    public GridStateAlterationRemedialActionReader(QueryManager queryManager, Network network, Set<Action> actions, Set<Contingency> contingencies) {
        super(queryManager, network);
        this.importedActions = actions.stream().map(Action::getId).collect(Collectors.toSet());
        this.importedContingencies = contingencies.stream().map(Contingency::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<OperatorStrategy> readFromProfiles() {
        Set<OperatorStrategy> operatorStrategies = new HashSet<>();
        PropertyBags gridStateAlterationRemedialActions = queryManager.query("gridStateAlterationRemedialAction", NcProfile.REMEDIAL_ACTION);
        PropertyBags contingencyWithRemedialActions = queryManager.query("contingencyWithRemedialAction", NcProfile.REMEDIAL_ACTION);
        PropertyBags gridStateAlterations = queryManager.query("topologyAction", NcProfile.REMEDIAL_ACTION);
        gridStateAlterations.addAll(queryManager.query("shuntCompensatorModification", NcProfile.REMEDIAL_ACTION));
        gridStateAlterations.addAll(queryManager.query("rotatingMachineAction", NcProfile.REMEDIAL_ACTION));
        Map<String, Set<PropertyBag>> contingencyPerRemedialAction = ReaderUtils.groupOnAttribute(contingencyWithRemedialActions, "remedialAction", true);
        Map<String, Set<PropertyBag>> gridStateAlterationsPerRemedialAction = ReaderUtils.groupOnAttribute(gridStateAlterations, "gridStateAlterationRemedialAction", true);
        for (PropertyBag gridStateAlterationRemedialAction : gridStateAlterationRemedialActions) {
            String gridStateAlterationRemedialActionId = gridStateAlterationRemedialAction.get(MRID);
            operatorStrategies.addAll(processGridStateAlterationRemedialAction(
                gridStateAlterationRemedialAction,
                contingencyPerRemedialAction.getOrDefault(gridStateAlterationRemedialActionId, Set.of()),
                gridStateAlterationsPerRemedialAction.getOrDefault(gridStateAlterationRemedialActionId, Set.of())
            ));
        }
        return operatorStrategies;
    }

    private Set<OperatorStrategy> processGridStateAlterationRemedialAction(PropertyBag gridStateAlterationRemedialAction,
                                                                           Set<PropertyBag> contingencyWithRemedialActions,
                                                                           Set<PropertyBag> gridStateAlterations) {
        String operatorStrategyId = gridStateAlterationRemedialAction.get(MRID);

        boolean normalAvailable = Boolean.parseBoolean(gridStateAlterationRemedialAction.get("normalAvailable"));
        if (!normalAvailable) {
            LOGGER.warn("GridStateAlterationRemedialAction {} is not available and will be ignored.", operatorStrategyId);
            return Set.of();
        }

        List<String> actions = new ArrayList<>();
        for (PropertyBag gridStateAlteration : gridStateAlterations) {
            String actionId = gridStateAlteration.get(MRID);
            if (importedActions.contains(actionId)) {
                actions.add(actionId);
            } else {
                LOGGER.warn("Action {} associated with GridStateAlterationRemedialAction {} was not imported. GridStateAlterationRemedialAction will be ignored.", actionId, operatorStrategyId);
                return Set.of();
            }
        }

        // TODO: take into account association with AE for conditional triggering

        String kind = gridStateAlterationRemedialAction.get("kind");
        if (PREVENTIVE_KIND.equals(kind)) {
            if (!contingencyWithRemedialActions.isEmpty()) {
                LOGGER.info("Contingencies associated with preventive GridStateAlterationRemedialAction {} will not be taken into account.", operatorStrategyId);
            }
            return Set.of(new OperatorStrategy(operatorStrategyId, ContingencyContext.none(), new TrueCondition(), actions));
        } else if (CURATIVE_KIND.equals(kind)) {
            if (contingencyWithRemedialActions.isEmpty()) {
                return Set.of(new OperatorStrategy(operatorStrategyId, ContingencyContext.onlyContingencies(), new TrueCondition(), actions));
            } else {
                Set<OperatorStrategy> contingencyOperatorStrategies = new HashSet<>();
                for (PropertyBag contingencyWithRemedialAction : contingencyWithRemedialActions) {
                    String contingencyId = contingencyWithRemedialAction.get("contingency");
                    if (!importedContingencies.contains(contingencyId)) {
                        LOGGER.info("Contingency {} associated with GridStateAlterationRemedialAction {} was not imported. No OperatorStrategy will be created for this contingency.",
                            contingencyId, operatorStrategyId);
                    } else {
                        Optional<OperatorStrategy> curativeOperatorStrategy = processGridStateAlterationRemedialActionForContingency(
                            operatorStrategyId, contingencyId, actions, contingencyWithRemedialAction
                        );
                        curativeOperatorStrategy.ifPresent(contingencyOperatorStrategies::add);
                    }
                }
                if (contingencyOperatorStrategies.isEmpty()) {
                    LOGGER.warn("GridStateAlterationRemedialAction {} has no valid contingency associated and will be ignored.", operatorStrategyId);
                    return Set.of();
                } else {
                    return contingencyOperatorStrategies;
                }
            }
        } else {
            LOGGER.warn("GridStateAlterationRemedialAction {} has an unknown kind {} and will be ignored.", operatorStrategyId, kind);
            return Set.of();
        }
    }

    private Optional<OperatorStrategy> processGridStateAlterationRemedialActionForContingency(String operatorStrategyId,
                                                                                              String contingencyId,
                                                                                              List<String> actions,
                                                                                              PropertyBag contingencyWithRemedialAction) {
        boolean normalEnabled = Boolean.parseBoolean(contingencyWithRemedialAction.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.info("Association between Contingency {} and GridStateAlterationRemedialAction {} is disabled and will be ignored. "
                    + "No OperatorStrategy will be created for this contingency.",
                contingencyId, operatorStrategyId);
            return Optional.empty();
        }

        String combinationConstraintKind = contingencyWithRemedialAction.get("combinationConstraintKind");
        if (!COMBINATION_CONSTRAINT_KIND_INCLUDED.equals(combinationConstraintKind)) {
            LOGGER.info("Association between Contingency {} and GridStateAlterationRemedialAction {} is not included and will be ignored. "
                    + "No OperatorStrategy will be created for this contingency.",
                contingencyId, operatorStrategyId);
            return Optional.empty();
        }

        return Optional.of(new OperatorStrategy(
            operatorStrategyId + "@" + contingencyId,
            ContingencyContext.specificContingency(contingencyId),
            new TrueCondition(),
            actions
        ));
    }
}
