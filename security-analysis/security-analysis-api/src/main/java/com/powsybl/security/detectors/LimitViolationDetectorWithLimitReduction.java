/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.iidm.network.*;
import com.powsybl.security.detectors.criterion.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.security.detectors.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.detectors.criterion.network.AbstractNetworkElementCriterion;
import com.powsybl.iidm.network.util.translation.NetworkElement;
import com.powsybl.security.detectors.criterion.network.NetworkElementVisitor;

import java.util.*;

/**
 * Implements the behaviour for limit violation detection with limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitViolationDetectorWithLimitReduction {

    LimitReductionDefinitionList limitReductionDefinitionList;

    public LimitViolationDetectorWithLimitReduction(LimitReductionDefinitionList limitReductionDefinitionList) {
        this.limitReductionDefinitionList = limitReductionDefinitionList;
    }

    public HashMap<LimitType, HashMap<LoadingLimitType, Double>> getLimitsWithAppliedReduction(String contingencyId, NetworkElement networkElement, ThreeSides side, LimitType limitType) {

        for (LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition : limitReductionDefinitionList.getLimitReductionDefinitions()) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isContingencyContextApplicable(limitReductionDefinition.getContingencyContexts(), contingencyId) &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReductionDefinition.getNetworkElementCriteria())) {
                Optional<? extends LoadingLimits> optionalLoadingLimits = networkElement.getLoadingLimits(limitType, side);
                optionalLoadingLimits.ifPresent(loadingLimits -> manageLimits(loadingLimits, limitReductionDefinition));
                //TODO add limits to return HashMap
            }
        }
        return new HashMap<>();
    }

    private void manageLimits(LoadingLimits loadingLimits, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {

        double returnedPermanentLimit = loadingLimits.getPermanentLimit();
        if (isPermanentLimitAffectedByLimitReduction(limitReductionDefinition)) {
            returnedPermanentLimit = returnedPermanentLimit * limitReductionDefinition.getLimitReduction();
        }
        List<LoadingLimits.TemporaryLimit> limitsToBeChecked = new ArrayList<>();
        for (LoadingLimits.TemporaryLimit temporaryLimit : loadingLimits.getTemporaryLimits()) {
            LoadingLimits.TemporaryLimit returnedTemporaryLimit = temporaryLimit;
            if (isTemporaryLimitAffectedByLimitReduction(limitReductionDefinition, returnedTemporaryLimit)) {
                String name = temporaryLimit.getName();
                double value = temporaryLimit.getValue() * limitReductionDefinition.getLimitReduction();
                int acceptableDuration = temporaryLimit.getAcceptableDuration();
                boolean hasOverloadingProtection = temporaryLimit.isFictitious();
                //TODO create a temporary limit with those elements and add it to temporaryLimitsToBeChecked instead of the non reduced temporary limit
            }
            limitsToBeChecked.add(returnedTemporaryLimit);
        }
    }

    private boolean isContingencyContextApplicable(List<ContingencyContext> contingencyContextList, String contingencyId) {
        for (ContingencyContext contingencyContext : contingencyContextList) {
            if (contingencyContext.getContextType() == ContingencyContextType.ONLY_CONTINGENCIES ||
                    contingencyContext.getContextType() == ContingencyContextType.ALL) {
                return true;
            }
            if (contingencyContext.getContextType() == ContingencyContextType.SPECIFIC &&
                    contingencyContext.getContingencyId().equals(contingencyId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEquipmentAffectedByLimitReduction(NetworkElement networkElement, List<AbstractNetworkElementCriterion> networkElementCriteria) {

        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);

        return networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    private boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().stream().anyMatch(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    private boolean isTemporaryLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition, LoadingLimits.TemporaryLimit temporaryLimit) {
        return limitReductionDefinition.getDurationCriteria().stream()
                .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                .map(AbstractTemporaryDurationCriterion.class::cast)
                .anyMatch(temporaryLimitDurationCriterion -> temporaryLimitDurationCriterion.isTemporaryLimitWithinCriterionBounds(temporaryLimit));
    }
}
