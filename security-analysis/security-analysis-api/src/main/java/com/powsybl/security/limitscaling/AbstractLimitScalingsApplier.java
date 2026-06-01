/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.NetworkElementVisitor;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.AbstractLimitsComputerWithCache;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.IdenticalLimitsContainer;
import com.powsybl.security.limitscaling.computation.AbstractLimitsScaler;
import com.powsybl.security.limitscaling.computation.AbstractLimitsScalerCreator;

import java.util.*;

import static com.powsybl.contingency.ContingencyContextType.*;

/**
 * Abstract class responsible for computing reduced limits using a list of {@link LimitScaling}.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitScalingsApplier<P, L> extends AbstractLimitsComputerWithCache<P, L> {
    private final List<LimitScaling> limitScalingList;
    private List<LimitScaling> reductionsForThisContingency = Collections.emptyList();

    /**
     * Create a new {@link AbstractLimitScalingsApplier} using a list of reductions.
     * @param limitScalingList the list of the reductions to use when computing reduced limits.
     */
    protected AbstractLimitScalingsApplier(List<LimitScaling> limitScalingList) {
        super();
        this.limitScalingList = limitScalingList;
        computeReductionsForThisContingency(null);
    }

    @Override
    protected Collection<LimitsContainer<L>> computeUncachedLimits(P processable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        NetworkElement networkElement = Objects.requireNonNull(asNetworkElement(processable));
        OriginalLimitsGetter<P, L> originalLimitsGetter = Objects.requireNonNull(getOriginalLimitsGetter());
        AbstractLimitsScalerCreator<L, AbstractLimitsScaler<L>> limitsReducerCreator = Objects.requireNonNull(getLimitsReducerCreator());

        HashMap<String, L> originalLimitsByGroupId = originalLimitsGetter.getLimits(processable, limitType, side);
        Collection<LimitsContainer<L>> limitsContainers = HashSet.newHashSet(3);

        for (Map.Entry<String, L> entry : originalLimitsByGroupId.entrySet()) {
            limitsContainers.add(computeUncacheLimit(networkElement, entry.getKey(), entry.getValue(),
                    limitsReducerCreator, limitType, side, monitoringOnly));
        }
        // Cache the value to avoid recomputing it
        putInCache(processable, limitType, side, monitoringOnly, limitsContainers);
        return limitsContainers;
    }

    private LimitsContainer<L> computeUncacheLimit(NetworkElement networkElement, String groupId, L originalLimits,
                                                   AbstractLimitsScalerCreator<L, AbstractLimitsScaler<L>> limitsReducerCreator,
                                                   LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        if (reductionsForThisContingency.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return new IdenticalLimitsContainer<>(originalLimits, groupId);
        }

        AbstractLimitsScaler<L> limitsReducer = limitsReducerCreator.create(networkElement.getId(), groupId, originalLimits);
        updateLimitReducer(limitsReducer, networkElement, limitType, side, monitoringOnly);

        return limitsReducer.getLimits();
    }

    /**
     * Return an {@link OriginalLimitsGetter} allowing to retrieve
     * {@link L} from a network element of type {@link P}.
     * @return an original limits getter
     */
    protected abstract OriginalLimitsGetter<P, L> getOriginalLimitsGetter();

    /**
     * Return the {@link AbstractLimitsScaler} creator, which will be used to create an object of type {@link L} containing the modified limits.
     * @return the creator for {@link AbstractLimitsScaler}
     */
    protected abstract AbstractLimitsScalerCreator<L, AbstractLimitsScaler<L>> getLimitsReducerCreator();

    /**
     * <p>Return a {@link NetworkElement} representation of <code>processable</code>
     * (itself if it already is a {@link NetworkElement} or an adapter).</p>
     * @param processable the object which limits should be computed.
     * @return a {@link NetworkElement} representation of <code>processable</code>
     */
    protected abstract NetworkElement asNetworkElement(P processable);

    private void updateLimitReducer(AbstractLimitsScaler<?> limitsReducer, NetworkElement networkElement,
                                    LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        for (LimitScaling limitScaling : reductionsForThisContingency) {
            if (limitScaling.getLimitType() == limitType
                    && limitScaling.isMonitoringOnly() == monitoringOnly
                    && isNetworkElementAffectedByLimitReduction(networkElement, side, limitScaling)
                    && isOperationalLimitsGroupAffectedByLimitReduction(limitsReducer.getLimitsGroupId(), limitScaling)) {
                setLimitReductionsToLimitReducer(limitsReducer, limitScaling);
            }
        }
    }

    /**
     * <p>Change the contingency for which the reduced limits must be computed.</p>
     * @param contingencyId the ID of the new contingency, or <code>null</code> if you study the pre-contingency state.
     */
    public void setWorkingContingency(String contingencyId) {
        var reductionsForPreviousContingency = reductionsForThisContingency;
        computeReductionsForThisContingency(contingencyId);
        if (!reductionsForThisContingency.equals(reductionsForPreviousContingency)) {
            // The limit reductions are not the same as for the previous contingencyId, we clear the cache.
            clearCache();
        }
    }

    private void computeReductionsForThisContingency(String contingencyId) {
        reductionsForThisContingency = limitScalingList.stream()
                .filter(l -> isContingencyInContingencyContext(l.getContingencyContext(), contingencyId))
                .toList();
    }

    private void setLimitReductionsToLimitReducer(AbstractLimitsScaler<?> limitsReducer, LimitScaling limitScaling) {
        if (isPermanentLimitAffectedByLimitReduction(limitScaling)) {
            limitsReducer.setPermanentLimitReduction(limitScaling.getValue());
        }
        limitsReducer.getTemporaryLimitsAcceptableDurationStream()
                .filter(acceptableDuration -> isTemporaryLimitAffectedByLimitReduction(acceptableDuration, limitScaling))
                .forEach(acceptableDuration -> limitsReducer.setTemporaryLimitReduction(acceptableDuration,
                        limitScaling.getValue()));
    }

    protected static boolean isContingencyInContingencyContext(ContingencyContext contingencyContext, String contingencyId) {
        return contingencyContext == null
                || contingencyContext.getContextType() == ALL
                || contingencyContext.getContextType() == NONE && contingencyId == null
                || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId);
    }

    protected static boolean isNetworkElementAffectedByLimitReduction(NetworkElement networkElement, ThreeSides side, LimitScaling limitScaling) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement, side);
        List<NetworkElementCriterion> networkElementCriteria = limitScaling.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected static boolean isPermanentLimitAffectedByLimitReduction(LimitScaling limitScaling) {
        return limitScaling.getDurationCriteria().isEmpty()
                || limitScaling.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected static boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitScaling limitScaling) {
        return limitScaling.getDurationCriteria().isEmpty()
                || limitScaling.getDurationCriteria().stream()
                    .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                    .map(AbstractTemporaryDurationCriterion.class::cast)
                    .anyMatch(c -> c.filter(temporaryLimitAcceptableDuration));
    }

    protected static boolean isOperationalLimitsGroupAffectedByLimitReduction(String operationalLimitsGroupId, LimitScaling limitScaling) {
        return limitScaling.getOperationalLimitsGroupIdsSelection().isEmpty()
            || limitScaling.getOperationalLimitsGroupIdsSelection().contains(operationalLimitsGroupId);
    }

    /**
     * Interface for objects allowing to retrieve limits (of generic type {@link L}) from an object
     * manageable by the {@link LimitsComputer} (of generic type {@link P}).
     *
     * @param <P> Generic type for the network element for which we want to retrieve the limits
     * @param <L> Generic type for the limits to retrieve
     */
    protected interface OriginalLimitsGetter<P, L> {
        HashMap<String, L> getLimits(P e, LimitType limitType, ThreeSides side);
    }
}
