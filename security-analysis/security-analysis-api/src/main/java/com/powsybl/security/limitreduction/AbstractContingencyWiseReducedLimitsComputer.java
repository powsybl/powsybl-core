/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.NetworkElementVisitor;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.contingency.ContingencyContextType.*;

/**
 * Abstract class responsible for computing reduced limits using a {@link LimitReductionDefinitionList}.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractContingencyWiseReducedLimitsComputer<P, L> extends AbstractReducedLimitsComputer<P, L> {
    private final LimitReductionDefinitionList limitReductionDefinitionList;
    private List<LimitReductionDefinition> definitionsForCurrentContingencyId = Collections.emptyList();
    private boolean sameDefinitionsAsForPreviousContingencyId = false;

    /**
     * Create a new {@link AbstractContingencyWiseReducedLimitsComputer} using a list of reduction definitions.
     * @param limitReductionDefinitionList the list of the reduction definitions to use when computing reduced limits.
     */
    protected AbstractContingencyWiseReducedLimitsComputer(LimitReductionDefinitionList limitReductionDefinitionList) {
        super();
        this.limitReductionDefinitionList = limitReductionDefinitionList;
        computeDefinitionsForCurrentContingencyId(null);
    }

    @Override
    protected Optional<LimitsContainer<L>> computeLimitsWithAppliedReduction(P processable, LimitType limitType, ThreeSides side) {
        OriginalLimitsGetter<P, L> originalLimitsGetter = Objects.requireNonNull(getOriginalLimitsGetter());
        Optional<L> originalLimits = originalLimitsGetter.getLimits(processable, limitType, side);
        if (definitionsForCurrentContingencyId.isEmpty() || originalLimits.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return originalLimits.map(l -> new LimitsContainer<>(l, l));
        }

        AbstractLimitsReducerCreator<L, AbstractLimitsReducer<L>> limitsReducerCreator = Objects.requireNonNull(getLimitsReducerCreator());
        NetworkElement networkElement = Objects.requireNonNull(asNetworkElement(processable));
        AbstractLimitsReducer<L> limitsReducer = limitsReducerCreator.create(networkElement.getId(), originalLimits.get());
        updateLimitReducer(limitsReducer, networkElement, limitType);

        L reducedLimits = limitsReducer.getReducedLimits();
        // Cache the value to avoid recomputing it
        LimitsContainer<L> limitsContainer = new LimitsContainer<>(reducedLimits, originalLimits.get());
        putInCache(processable, limitType, side, limitsContainer);
        return Optional.of(limitsContainer);
    }

    /**
     * Return an {@link OriginalLimitsGetter} allowing to retrieve
     * {@link L} from a network element of type {@link P}.
     * @return an original limits getter
     */
    protected abstract OriginalLimitsGetter<P, L> getOriginalLimitsGetter();

    /**
     * Return the {@link AbstractLimitsReducer} creator, which will be used to create an object of type {@link L} containing the modified limits.
     * @return the creator for {@link AbstractLimitsReducer}
     */
    protected abstract AbstractLimitsReducerCreator<L, AbstractLimitsReducer<L>> getLimitsReducerCreator();

    protected abstract NetworkElement asNetworkElement(P processable);

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElement networkElement, LimitType limitType) {
        for (LimitReductionDefinition limitReductionDefinition : definitionsForCurrentContingencyId) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReductionDefinition)) {
                setLimitReductionsToLimitReducer(limitsReducer, limitReductionDefinition);
            }
        }
    }

    /**
     * <p>Change the contingency for which the altered limits must be computed.</p>
     * @param contingencyId the ID of the new contingency
     * @return <code>true</code> if the reduction definitions to use for the new contingency are the same as for the previous one,
     * <code>false</code> otherwise.
     */
    public boolean changeContingencyId(String contingencyId) {
        var definitionsForPreviousContingencyId = definitionsForCurrentContingencyId;
        computeDefinitionsForCurrentContingencyId(contingencyId);
        sameDefinitionsAsForPreviousContingencyId = definitionsForCurrentContingencyId.equals(definitionsForPreviousContingencyId);
        if (!isSameDefinitionsAsForPreviousContingencyId()) {
            // The limit reductions are not the same as for the previous contingencyId, we clear the cache.
            clearCache();
        }
        return isSameDefinitionsAsForPreviousContingencyId();
    }

    private void computeDefinitionsForCurrentContingencyId(String contingencyId) {
        definitionsForCurrentContingencyId = limitReductionDefinitionList.getLimitReductionDefinitions().stream()
                .filter(l -> isContingencyContextListApplicable(l.getContingencyContexts(), contingencyId))
                .toList();
    }

    /**
     * <p>Indicate if the reduction definitions currently applied are as for the previous contingency.</p>
     * @return <code>true</code> if the reduction definitions to use for the current contingency are the same as for the previous one,
     * <code>false</code> otherwise.
     */
    public boolean isSameDefinitionsAsForPreviousContingencyId() {
        return sameDefinitionsAsForPreviousContingencyId;
    }

    private void setLimitReductionsToLimitReducer(AbstractLimitsReducer<?> limitsReducer, LimitReductionDefinition limitReductionDefinition) {
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

    protected static boolean isEquipmentAffectedByLimitReduction(NetworkElement networkElement, LimitReductionDefinition limitReductionDefinition) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        List<NetworkElementCriterion> networkElementCriteria = limitReductionDefinition.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected static boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected static boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                    .map(AbstractTemporaryDurationCriterion.class::cast)
                    .anyMatch(c -> c.filter(temporaryLimitAcceptableDuration));
    }

    /**
     * Interface for objects allowing to retrieve limits (of generic type {@link L}) from an object
     * manageable by the {@link ReducedLimitsComputer} (of generic type {@link P}).
     *
     * @param <P> Generic type for the network element for which we want to retrieve the limits
     * @param <L> Generic type for the limits to retrieve
     */
    protected interface OriginalLimitsGetter<P, L> {
        Optional<L> getLimits(P e, LimitType limitType, ThreeSides side);
    }
}
