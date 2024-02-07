/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.criterion.translation.NetworkElement;
import com.powsybl.security.detectors.criterion.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.security.detectors.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.detectors.criterion.network.AbstractNetworkElementCriterion;
import com.powsybl.security.detectors.criterion.network.NetworkElementVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.powsybl.contingency.ContingencyContextType.*;

//TODO rename and move this class
/**
 * Implements the behaviour for limit violation detection with limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class LimitViolationDetectorWithLimitReduction {
    private final LimitReductionDefinitionList limitReductionDefinitionList;
    private String currentContingencyId = null;
    private List<LimitReductionDefinitionList.LimitReductionDefinition> definitionsForCurrentContingencyId = Collections.emptyList();
    private List<LimitReductionDefinitionList.LimitReductionDefinition> definitionsForPreviousContingencyId = Collections.emptyList();

    public LimitViolationDetectorWithLimitReduction(LimitReductionDefinitionList limitReductionDefinitionList) {
        this.limitReductionDefinitionList = limitReductionDefinitionList;
        computeDefinitionsForCurrentContingencyId();
    }

    public <T> Optional<T> getLimitsWithAppliedReduction(NetworkElement<T> networkElement, LimitType limitType,
                                                         ThreeSides side, Supplier<AbstractLimitsReducer<T>> limitsReducerSupplier) {
        Optional<T> originalLimits = networkElement.getLimits(limitType, side);
        if (originalLimits.isEmpty()) {
            return Optional.empty();
        }
        AbstractLimitsReducer<T> limitsReducer = limitsReducerSupplier.get();
        limitsReducer.initializeLimits(originalLimits.get());
        updateLimitReducer(limitsReducer, networkElement, limitType);
        return Optional.of(limitsReducer.generateReducedLimits());
    }

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElement<?> networkElement, LimitType limitType) {
        for (LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition : definitionsForCurrentContingencyId) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReductionDefinition.getNetworkElementCriteria())) {
                manageLimits(limitsReducer, limitReductionDefinition);
            }
        }
    }

    public boolean changeContingencyId(String contingencyId) {
        definitionsForPreviousContingencyId = definitionsForCurrentContingencyId;
        currentContingencyId = contingencyId;
        computeDefinitionsForCurrentContingencyId();
        return sameDefinitionsAsForPreviousContingencyId();
    }

    public boolean sameDefinitionsAsForPreviousContingencyId() {
        return definitionsForCurrentContingencyId.equals(definitionsForPreviousContingencyId);
    }

    private void computeDefinitionsForCurrentContingencyId() {
        definitionsForCurrentContingencyId = limitReductionDefinitionList.getLimitReductionDefinitions().stream()
                .filter(l -> isContingencyContextApplicable(l.getContingencyContexts(), currentContingencyId))
                .toList();
    }

    //TODO to rename
    private void manageLimits(AbstractLimitsReducer<?> limitsReducer, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        if (isPermanentLimitAffectedByLimitReduction(limitReductionDefinition)) {
            limitsReducer.setPermanentLimitReduction(limitReductionDefinition.getLimitReduction());
        }
        limitsReducer.getTemporaryLimitsAcceptableDurationStream()
                .filter(acceptableDuration -> isTemporaryLimitAffectedByLimitReduction(limitReductionDefinition, acceptableDuration))
                .forEach(acceptableDuration -> limitsReducer.setTemporaryLimitReduction(acceptableDuration,
                        limitReductionDefinition.getLimitReduction()));
    }

    private boolean isContingencyContextApplicable(List<ContingencyContext> contingencyContextList, String contingencyId) {
        return contingencyContextList.stream().anyMatch(contingencyContext ->
                contingencyContext.getContextType() == ALL
                        || contingencyContext.getContextType() == NONE && contingencyId == null
                        || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                        || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId)
        );
    }

    private boolean isEquipmentAffectedByLimitReduction(NetworkElement<?> networkElement, List<AbstractNetworkElementCriterion> networkElementCriteria) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        return networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    private boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().stream().anyMatch(limitDurationCriterion ->
                limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    private boolean isTemporaryLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition,
                                                             int temporaryLimitAcceptableDuration) {
        return limitReductionDefinition.getDurationCriteria().stream()
                .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                .map(AbstractTemporaryDurationCriterion.class::cast)
                .anyMatch(temporaryLimitDurationCriterion -> temporaryLimitDurationCriterion.isAcceptableDurationWithinCriterionBounds(temporaryLimitAcceptableDuration));
    }
}
