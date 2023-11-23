/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.iidm.network.*;
import com.powsybl.security.detectors.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.detectors.criterion.network.AbstractNetworkElementCriterion;

import java.util.*;

/**
 * Implements the behaviour for limit violation detection with limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitViolationDetectorWithLimitReduction {

    Network network;
    LimitReductionDefinitionList limitReductionDefinitionList;

    public LimitViolationDetectorWithLimitReduction(Network network, LimitReductionDefinitionList limitReductionDefinitionList) {
        this.network = network;
        this.limitReductionDefinitionList = limitReductionDefinitionList;
    }

    public HashMap<LimitType, HashMap<LoadingLimitType, Double>> getLimitsWithAppliedReduction(Contingency contingency, Identifiable identifiable, Branch.Side side, LimitType limitType) {
        //NB : How to deal with Side ? We have Branch.Side, HvdcLine.Side, ThreeWindingsTransformer.Side...

        //TODO Apply contingency on network (or on a network variant)

        for (LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition : limitReductionDefinitionList.getLimitReductionDefinitions()) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isContingencyContextApplicable(limitReductionDefinition.getContingencyContexts(), contingency) &&
                    isEquipmentAffectedByLimitReduction(identifiable.getId(), limitReductionDefinition.getNetworkElementCriteria())) {
                if (identifiable instanceof Line line) {
                    switch (limitType) {
                        case CURRENT:
                            Optional<CurrentLimits> currentLimits = line.getCurrentLimits(side);
                            if (currentLimits.isPresent()) {
                                manageLimits(currentLimits.get(), limitReductionDefinition);
                            }
                            //TODO add limits to return HashMap
                            break;
                        case ACTIVE_POWER:
                            Optional<ActivePowerLimits> activePowerLimits = line.getActivePowerLimits(side);
                            if (activePowerLimits.isPresent()) {
                                manageLimits(activePowerLimits.get(), limitReductionDefinition);
                            }
                            //TODO add limits to return HashMap
                            break;
                        case APPARENT_POWER:
                            Optional<ApparentPowerLimits> apparentPowerLimits = line.getApparentPowerLimits(side);
                            if (apparentPowerLimits.isPresent()) {
                                manageLimits(apparentPowerLimits.get(), limitReductionDefinition);
                            }
                            //TODO add limits to return HashMap
                            break;
                    }
                } else if (identifiable instanceof HvdcLine hvdcLine) {
                    //TODO are there limits on HvdcLine ?
                } else if (identifiable instanceof TieLine tieLine) {
                    //TODO + are there ApparentPowerLimits and ActivePowerLimits ?
                    //NB : CurrentLimits available through the dangling lines
                } else if (identifiable instanceof TwoWindingsTransformer) {
                    //TODO
                } else if (identifiable instanceof ThreeWindingsTransformer) {
                    //TODO
                }
            }
        }

        return null;
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

    private boolean isContingencyContextApplicable(List<ContingencyContext> contingencyContextList, Contingency contingency) {
        for (ContingencyContext contingencyContext : contingencyContextList) {
            if (contingencyContext.getContextType() == ContingencyContextType.ALL_CONTINGENCIES ||
                    contingencyContext.getContextType() == ContingencyContextType.ALL) {
                return true;
            }
            if (contingencyContext.getContextType() == ContingencyContextType.SPECIFIC &&
                    contingencyContext.getContingencyId().equals(contingency.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEquipmentAffectedByLimitReduction(String networkElementId, List<AbstractNetworkElementCriterion> networkElementCriteria) {
        Identifiable identifiable = network.getIdentifiable(networkElementId);
        for (AbstractNetworkElementCriterion networkElementCriterion : networkElementCriteria) {
            if (networkElementCriterion.getNetworkElementIds().contains(networkElementId)) {
                return true;
            }
            if (identifiable instanceof Line) {
                //TODO
            } else if (identifiable instanceof HvdcLine) {
                //TODO
            } else if (identifiable instanceof TieLine) {
                //TODO
            } else if (identifiable instanceof TwoWindingsTransformer) {
                //TODO
            } else if (identifiable instanceof ThreeWindingsTransformer) {
                //TODO
            }
        }
        return false;
    }

    private boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().stream().anyMatch(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    private boolean isTemporaryLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition, LoadingLimits.TemporaryLimit temporaryLimit) {
        limitReductionDefinition.getDurationCriteria().stream().filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY));
        //TODO
        return false;
    }
}
