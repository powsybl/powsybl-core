/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.*;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleContingencyDbFacade implements ContingencyDbFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleContingencyDbFacade.class);

    private final ContingenciesAndActionsDatabaseClient contingenciesActionsDbClient;

    private final Network network;

    public SimpleContingencyDbFacade(ContingenciesAndActionsDatabaseClient contingenciesActionsDbClient, Network network) {
        this.contingenciesActionsDbClient = contingenciesActionsDbClient;
        this.network = network;
    }

    @Override
    public synchronized List<Contingency> getContingencies() {
        return contingenciesActionsDbClient.getContingencies(network);
    }

    private static boolean constraintsMatch(ActionsContingenciesAssociation association, List<LimitViolation> limitViolations) {
        for (Constraint constraint : association.getConstraints()) {
            switch (constraint.getType()) {
                case BRANCH_OVERLOAD:
                    if (limitViolations == null) {
                        return true;
                    } else {
                        for (LimitViolation limitViolation : limitViolations) {
                            if (limitViolation.getLimitType() == LimitViolationType.CURRENT
                                    && limitViolation.getSubject().getId().equals(constraint.getEquipment())) {
                                return true;
                            }
                        }
                        return false;
                    }

                default:
                    throw new AssertionError();
            }
        }
        return true;
    }

    @Override
    public synchronized List<List<Action>> getCurativeActions(Contingency contingency, List<LimitViolation> limitViolations) {
        Objects.requireNonNull(contingency);
        List<List<Action>> curativeActions = new ArrayList<>();
        for (ActionsContingenciesAssociation association : contingenciesActionsDbClient.getActionsCtgAssociations(network)) {
            if (!association.getContingenciesId().contains(contingency.getId())) {
                continue;
            }
            if (!constraintsMatch(association, limitViolations)) {
                continue;
            }
            for (String actionId : association.getActionsId()) {
                Action action = contingenciesActionsDbClient.getAction(actionId, network);
                if (action != null) {
                    curativeActions.add(Collections.singletonList(action));
                } else {
                    ActionPlan actionPlan = contingenciesActionsDbClient.getActionPlan(actionId);
                    if (actionPlan != null) {
                        for (ActionPlanOption option : actionPlan.getPriorityOption().values()) {
                            if (option.getLogicalExpression().getOperator() instanceof UnaryOperator) {
                                UnaryOperator op = (UnaryOperator)  option.getLogicalExpression().getOperator();
                                curativeActions.add(Collections.singletonList(contingenciesActionsDbClient.getAction(op.getActionId(), network)));
                            } else {
                                throw new AssertionError("Operator " + option.getLogicalExpression().getOperator().getClass() + " not yet supported");
                            }
                        }
                    } else {
                        LOGGER.error("Action {} not found for contingency {}", actionId , contingency.getId());
                    }
                }
            }
        }
        return curativeActions;
    }
}
