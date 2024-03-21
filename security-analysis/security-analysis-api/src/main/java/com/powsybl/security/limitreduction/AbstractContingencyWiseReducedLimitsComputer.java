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
import com.powsybl.iidm.network.limitreduction.computer.AbstractReducedLimitsComputer;
import com.powsybl.iidm.network.limitreduction.computer.ReducedLimitsComputer;
import com.powsybl.iidm.network.limitreduction.result.LimitsContainer;
import com.powsybl.iidm.network.limitreduction.result.UnalteredLimitsContainer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.contingency.ContingencyContextType.*;

/**
 * Abstract class responsible for computing reduced limits using a {@link LimitReductionList}.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractContingencyWiseReducedLimitsComputer<P, L> extends AbstractReducedLimitsComputer<P, L> {
    private final LimitReductionList limitReductionList;
    private List<LimitReduction> reductionsForCurrentContingencyId = Collections.emptyList();
    private boolean sameReductionsAsForPreviousContingencyId = false;

    /**
     * Create a new {@link AbstractContingencyWiseReducedLimitsComputer} using a list of reductions.
     * @param limitReductionList the list of the reductions to use when computing reduced limits.
     */
    protected AbstractContingencyWiseReducedLimitsComputer(LimitReductionList limitReductionList) {
        super();
        this.limitReductionList = limitReductionList;
        computeReductionsForCurrentContingencyId(null);
    }

    @Override
    protected Optional<LimitsContainer<L>> computeLimitsWithAppliedReduction(P processable, LimitType limitType, ThreeSides side) {
        OriginalLimitsGetter<P, L> originalLimitsGetter = Objects.requireNonNull(getOriginalLimitsGetter());
        Optional<L> originalLimits = originalLimitsGetter.getLimits(processable, limitType, side);
        if (reductionsForCurrentContingencyId.isEmpty() || originalLimits.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return originalLimits.map(UnalteredLimitsContainer::new);
        }

        AbstractLimitsReducerCreator<L, AbstractLimitsReducer<L>> limitsReducerCreator = Objects.requireNonNull(getLimitsReducerCreator());
        NetworkElement networkElement = Objects.requireNonNull(asNetworkElement(processable));
        AbstractLimitsReducer<L> limitsReducer = limitsReducerCreator.create(networkElement.getId(), originalLimits.get());
        updateLimitReducer(limitsReducer, networkElement, limitType);

        LimitsContainer<L> limitsContainer = limitsReducer.getReducedLimits();
        // Cache the value to avoid recomputing it
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

    /**
     * <p>Return a {@link NetworkElement} representation of <code>processable</code>
     * (itself if it already is a {@link NetworkElement} or an adapter).</p>
     * @param processable the object which limits should be computed.
     * @return a {@link NetworkElement} representation of <code>processable</code>
     */
    protected abstract NetworkElement asNetworkElement(P processable);

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElement networkElement, LimitType limitType) {
        for (LimitReduction limitReduction : reductionsForCurrentContingencyId) {
            if (limitReduction.getLimitType() == limitType &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReduction)) {
                setLimitReductionsToLimitReducer(limitsReducer, limitReduction);
            }
        }
    }

    /**
     * <p>Change the contingency for which the altered limits must be computed.</p>
     * @param contingencyId the ID of the new contingency
     * @return <code>true</code> if the reductions to use for the new contingency are the same as for the previous one,
     * <code>false</code> otherwise.
     */
    public boolean changeContingencyId(String contingencyId) {
        var reductionsForPreviousContingencyId = reductionsForCurrentContingencyId;
        computeReductionsForCurrentContingencyId(contingencyId);
        sameReductionsAsForPreviousContingencyId = reductionsForCurrentContingencyId.equals(reductionsForPreviousContingencyId);
        if (!isSameReductionsAsForPreviousContingencyId()) {
            // The limit reductions are not the same as for the previous contingencyId, we clear the cache.
            clearCache();
        }
        return isSameReductionsAsForPreviousContingencyId();
    }

    private void computeReductionsForCurrentContingencyId(String contingencyId) {
        reductionsForCurrentContingencyId = limitReductionList.getLimitReductions().stream()
                .filter(l -> isContingencyContextListApplicable(l.getContingencyContext(), contingencyId))
                .toList();
    }

    /**
     * <p>Indicate if the reductions currently applied are as for the previous contingency.</p>
     * @return <code>true</code> if the reductions to use for the current contingency are the same as for the previous one,
     * <code>false</code> otherwise.
     */
    public boolean isSameReductionsAsForPreviousContingencyId() {
        return sameReductionsAsForPreviousContingencyId;
    }

    private void setLimitReductionsToLimitReducer(AbstractLimitsReducer<?> limitsReducer, LimitReduction limitReduction) {
        if (isPermanentLimitAffectedByLimitReduction(limitReduction)) {
            limitsReducer.setPermanentLimitReduction(limitReduction.getValue());
        }
        limitsReducer.getTemporaryLimitsAcceptableDurationStream()
                .filter(acceptableDuration -> isTemporaryLimitAffectedByLimitReduction(acceptableDuration, limitReduction))
                .forEach(acceptableDuration -> limitsReducer.setTemporaryLimitReduction(acceptableDuration,
                        limitReduction.getValue()));
    }

    protected static boolean isContingencyContextListApplicable(ContingencyContext contingencyContext, String contingencyId) {
        return contingencyContext == null
                || contingencyContext.getContextType() == ALL
                || contingencyContext.getContextType() == NONE && contingencyId == null
                || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId);
    }

    protected static boolean isEquipmentAffectedByLimitReduction(NetworkElement networkElement, LimitReduction limitReduction) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        List<NetworkElementCriterion> networkElementCriteria = limitReduction.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected static boolean isPermanentLimitAffectedByLimitReduction(LimitReduction limitReduction) {
        return limitReduction.getDurationCriteria().isEmpty()
                || limitReduction.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected static boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitReduction limitReduction) {
        return limitReduction.getDurationCriteria().isEmpty()
                || limitReduction.getDurationCriteria().stream()
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
