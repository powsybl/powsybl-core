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
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.NetworkElementVisitor;
import com.powsybl.security.limitsreduction.criteria.translation.DefaultNetworkElementWithLimitsAdapter;
import com.powsybl.security.limitsreduction.criteria.translation.NetworkElementWithLimits;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;

import java.util.*;

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
    private boolean sameDefinitionsAsForPreviousContingencyId = false;

    private final Map<CacheKey, Object> reducedLimitsCache;

    public ReducedLimitsComputer(LimitReductionDefinitionList limitReductionDefinitionList) {
        this.limitReductionDefinitionList = limitReductionDefinitionList;
        this.reducedLimitsCache = new HashMap<>();
        computeDefinitionsForCurrentContingencyId(null);
    }

    public Optional<LoadingLimits> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        return getLimitsWithAppliedReduction(new DefaultNetworkElementWithLimitsAdapter(identifiable), limitType, side,
                (id, originalLimits) -> new DefaultLimitsReducer(originalLimits));
    }

    public <T> Optional<T> getLimitsWithAppliedReduction(NetworkElementWithLimits<T> networkElement, LimitType limitType, ThreeSides side,
                                                         AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(networkElement.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((T) reducedLimitsCache.get(cacheKey));
        }
        Optional<T> originalLimits = networkElement.getLimits(limitType, side);
        if (definitionsForCurrentContingencyId.isEmpty() || originalLimits.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return originalLimits;
        }
        AbstractLimitsReducer<T> limitsReducer = limitsReducerCreator.create(networkElement.getId(), originalLimits.get());
        updateLimitReducer(limitsReducer, networkElement, limitType);
        T reducedLimits = limitsReducer.getReducedLimits();
        // Cache the value to avoid recomputing it
        reducedLimitsCache.put(cacheKey, reducedLimits);
        return Optional.of(reducedLimits);
    }

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElementWithLimits<?> networkElement, LimitType limitType) {
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
        if (!isSameDefinitionsAsForPreviousContingencyId()) {
            // The limit reductions are not the same as for the previous contingencyId, we clear the cache.
            reducedLimitsCache.clear();
        }
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

    protected static boolean isContingencyContextListApplicable(List<ContingencyContext> contingencyContextList, String contingencyId) {
        return contingencyContextList.isEmpty()
                || contingencyContextList.stream().anyMatch(contingencyContext ->
                    contingencyContext.getContextType() == ALL
                        || contingencyContext.getContextType() == NONE && contingencyId == null
                        || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                        || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId)
        );
    }

    protected static boolean isEquipmentAffectedByLimitReduction(NetworkElementWithLimits<?> networkElement, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        List<NetworkElementCriterion> networkElementCriteria = limitReductionDefinition.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected static boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected static boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                    .map(AbstractTemporaryDurationCriterion.class::cast)
                    .anyMatch(c -> c.filter(temporaryLimitAcceptableDuration));
    }

    private record CacheKey(String networkElementId, LimitType type, ThreeSides side) {
    }
}
