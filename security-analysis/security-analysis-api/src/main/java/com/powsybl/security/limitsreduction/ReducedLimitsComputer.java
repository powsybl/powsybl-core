/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.criterion.translation.DefaultNetworkElement;
import com.powsybl.iidm.network.util.criterion.translation.NetworkElement;
import com.powsybl.security.limitsreduction.criterion.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.network.AbstractNetworkElementCriterion;
import com.powsybl.security.limitsreduction.criterion.network.NetworkElementVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.powsybl.contingency.ContingencyContextType.*;

/**
 * Class responsible for computing reduced limits using a {@link LimitReductionDefinitionList}.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ReducedLimitsComputer {
    private final LimitReductionDefinitionList limitReductionDefinitionList;
    private List<LimitReductionDefinitionList.LimitReductionDefinition> definitionsForCurrentContingencyId = Collections.emptyList();
    boolean sameDefinitionsAsForPreviousContingencyId = false;

    public ReducedLimitsComputer(LimitReductionDefinitionList limitReductionDefinitionList) {
        this.limitReductionDefinitionList = limitReductionDefinitionList;
        computeDefinitionsForCurrentContingencyId(null);
    }

    public Optional<LoadingLimits> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        return getLimitsWithAppliedReduction(new DefaultNetworkElement(identifiable), limitType, side, DefaultLimitsReducerCreator.getInstance());
    }

    public <T> Optional<T> getLimitsWithAppliedReduction(NetworkElement<T> networkElement, LimitType limitType, ThreeSides side,
                                                         AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        Optional<T> originalLimits = networkElement.getLimits(limitType, side);
        if (definitionsForCurrentContingencyId.isEmpty() || originalLimits.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return originalLimits;
        }
        AbstractLimitsReducer<T> limitsReducer = limitsReducerCreator.create(networkElement.getId(), originalLimits.get());
        updateLimitReducer(limitsReducer, networkElement, limitType);
        return Optional.of(limitsReducer.getReducedLimits());
    }

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElement<?> networkElement, LimitType limitType) {
        for (LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition : definitionsForCurrentContingencyId) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReductionDefinition)) {
                setLimitReductionsToLimitReducer(limitsReducer, limitReductionDefinition);
            }
        }
    }

    public boolean changeContingencyId(String contingencyId) {
        var definitionsForPreviousContingencyId = definitionsForCurrentContingencyId;
        computeDefinitionsForCurrentContingencyId(contingencyId);
        sameDefinitionsAsForPreviousContingencyId = definitionsForCurrentContingencyId.equals(definitionsForPreviousContingencyId);
        //TODO Add a cache { (networkElement.getId(), limitType) -> Optional<T> } to avoid recomputing all the limits
        // if the definitions are the same as for the previous contingencyId
        return isSameDefinitionsAsForPreviousContingencyId();
    }

    private void computeDefinitionsForCurrentContingencyId(String contingencyId) {
        definitionsForCurrentContingencyId = limitReductionDefinitionList.getLimitReductionDefinitions().stream()
                .filter(l -> isContingencyContextListApplicable(l.getContingencyContexts(), contingencyId))
                .toList();
    }

    public boolean isSameDefinitionsAsForPreviousContingencyId() {
        return sameDefinitionsAsForPreviousContingencyId;
    }

    private void setLimitReductionsToLimitReducer(AbstractLimitsReducer<?> limitsReducer, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        if (isPermanentLimitAffectedByLimitReduction(limitReductionDefinition)) {
            limitsReducer.setPermanentLimitReduction(limitReductionDefinition.getLimitReduction());
        }
        limitsReducer.getTemporaryLimitsAcceptableDurationStream()
                .filter(acceptableDuration -> isTemporaryLimitAffectedByLimitReduction(acceptableDuration, limitReductionDefinition))
                .forEach(acceptableDuration -> limitsReducer.setTemporaryLimitReduction(acceptableDuration,
                        limitReductionDefinition.getLimitReduction()));
    }

    protected boolean isContingencyContextListApplicable(List<ContingencyContext> contingencyContextList, String contingencyId) {
        return contingencyContextList.isEmpty()
                || contingencyContextList.stream().anyMatch(contingencyContext ->
                    contingencyContext.getContextType() == ALL
                        || contingencyContext.getContextType() == NONE && contingencyId == null
                        || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                        || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId)
        );
    }

    protected boolean isEquipmentAffectedByLimitReduction(NetworkElement<?> networkElement, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        List<AbstractNetworkElementCriterion> networkElementCriteria = limitReductionDefinition.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                    .map(AbstractTemporaryDurationCriterion.class::cast)
                    .anyMatch(c -> c.filter(temporaryLimitAcceptableDuration));
    }
}
