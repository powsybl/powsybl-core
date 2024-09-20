/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.limitmodification.result.AbstractDistinctLimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * Helper methods for checking the occurrence of overloads.
 *
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public final class LimitViolationUtils {

    public static final String PERMANENT_LIMIT_NAME = "permanent";

    private LimitViolationUtils() {
    }

    public static Overload checkTemporaryLimits(Branch<?> branch, TwoSides side, double limitReductionValue, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        return getLimits(branch, side.toThreeSides(), type, LimitsComputer.NO_MODIFICATIONS)
            .map(limits -> getOverload(limits.getLimits(), i, limitReductionValue))
            .orElse(null);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, double limitReductionValue, double i, LimitType type) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        return getLimits(transformer, side, type, LimitsComputer.NO_MODIFICATIONS)
            .map(limits -> getOverload(limits.getLimits(), i, limitReductionValue))
            .orElse(null);
    }

    public static Overload checkTemporaryLimits(Branch<?> branch, TwoSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        return getLimits(branch, side.toThreeSides(), type, limitsComputer)
                .map(limits -> getOverload(limits, i))
                .orElse(null);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        return getLimits(transformer, side, type, limitsComputer)
                .map(limits -> getOverload(limits, i))
                .orElse(null);
    }

    private static OverloadImpl getOverload(LoadingLimits limits, double i, double limitReductionValue) {
        double permanentLimit = limits.getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return null;
        }
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limits.getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit * limitReductionValue && i < tl.getValue() * limitReductionValue) {
                return new OverloadImpl(tl, previousLimitName, previousLimit);
            }
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
        }
        return null;
    }

    private static Overload getOverload(LimitsContainer<LoadingLimits> limitsContainer, double i) {
        double permanentLimit = limitsContainer.getLimits().getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return null;
        }
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limitsContainer.getLimits().getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        int previousAcceptableDuration = 0; // never mind initialisation it will be override with first loop
        boolean firstIteration = true;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit && i < tl.getValue()) {
                if (firstIteration) {
                    return new OverloadImpl(tl, previousLimitName,
                        limitsContainer.isDistinct() ?
                            ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getOriginalPermanentLimit() : previousLimit,
                        limitsContainer.isDistinct() ?
                            ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getPermanentLimitReduction() : 1);
                } else {
                    return new OverloadImpl(tl, previousLimitName,
                        limitsContainer.isDistinct() ?
                            ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getOriginalTemporaryLimit(previousAcceptableDuration) : previousLimit,
                        limitsContainer.isDistinct() ?
                            ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getTemporaryLimitReduction(previousAcceptableDuration) : 1);
                }
            }
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
            previousAcceptableDuration = tl.getAcceptableDuration();
            firstIteration = false;
        }
        return null;
    }

    private static PermanentLimitCheckResult checkPermanentLimitIfAny(LimitsContainer<LoadingLimits> limitsContainer, double i) {
        return checkPermanentLimitIfAny(limitsContainer, i, 1);
    }

    private static PermanentLimitCheckResult checkPermanentLimitIfAny(LimitsContainer<LoadingLimits> limitsContainer, double i, double limitReductionValue) {
        double permanentLimit = limitsContainer.getLimits().getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return new PermanentLimitCheckResult(false, limitReductionValue);
        }
        if (i >= permanentLimit * limitReductionValue) {
            return new PermanentLimitCheckResult(true, limitsContainer.isDistinct() ? ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getPermanentLimitReduction() : limitReductionValue);
        }
        return new PermanentLimitCheckResult(false, limitReductionValue);
    }

    public static boolean checkPermanentLimit(Branch<?> branch, TwoSides side, double limitReductionValue, double i, LimitType type) {
        return getLimits(branch, side.toThreeSides(), type, LimitsComputer.NO_MODIFICATIONS)
            .map(l -> checkPermanentLimitIfAny(l, i, limitReductionValue).isOverload())
            .orElse(false);
    }

    public static PermanentLimitCheckResult checkPermanentLimit(Branch<?> branch, TwoSides side, double i, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return getLimits(branch, side.toThreeSides(), type, computer)
                .map(l -> checkPermanentLimitIfAny(l, i))
                .orElse(new PermanentLimitCheckResult(false, 1.0));
    }

    public static boolean checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, double limitReductionValue, double i, LimitType type) {
        return getLimits(transformer, side, type, LimitsComputer.NO_MODIFICATIONS)
            .map(l -> checkPermanentLimitIfAny(l, i, limitReductionValue).isOverload())
            .orElse(false);
    }

    public static PermanentLimitCheckResult checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, LimitsComputer<Identifiable<?>, LoadingLimits> computer, double i, LimitType type) {
        return getLimits(transformer, side, type, computer)
                .map(l -> checkPermanentLimitIfAny(l, i))
                .orElse(null);
    }

    public static double getValueForLimit(Terminal t, LimitType type) {
        return switch (type) {
            case ACTIVE_POWER -> t.getP();
            case APPARENT_POWER -> Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT -> t.getI();
            default ->
                    throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        };
    }

    public static boolean checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, double limitReductionValue, LimitType type) {
        return checkPermanentLimit(transformer, side, limitReductionValue, getValueForLimit(transformer.getTerminal(side), type), type);
    }

    public static PermanentLimitCheckResult checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return checkPermanentLimit(transformer, side, computer, getValueForLimit(transformer.getTerminal(side), type), type);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, double limitReductionValue, LimitType type) {
        return checkTemporaryLimits(transformer, side, limitReductionValue, getValueForLimit(transformer.getTerminal(side), type), type);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return checkTemporaryLimits(transformer, side, computer, getValueForLimit(transformer.getTerminal(side), type), type);
    }

    public static Optional<? extends LimitsContainer<LoadingLimits>> getLimits(Identifiable<?> transformer, ThreeSides side, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return computer.computeLimits(transformer, type, side, false);
    }

    /**
     * @deprecated should use {@link #getLoadingLimits(Identifiable, LimitType, ThreeSides)} instead
     */
    @Deprecated(since = "6.4.0")
    public static Optional<? extends LoadingLimits> getLimits(Branch<?> branch, TwoSides side, LimitType type) {
        return branch.getLimits(type, side);
    }

    /**
     * @deprecated should use {@link #getLoadingLimits(Identifiable, LimitType, ThreeSides)} instead
     */
    @Deprecated(since = "6.4.0")
    public static Optional<? extends LoadingLimits> getLimits(ThreeWindingsTransformer transformer, ThreeSides side, LimitType type) {
        return transformer.getLeg(side).getLimits(type);
    }

    public static Optional<LoadingLimits> getLoadingLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE -> (Optional<LoadingLimits>) ((Line) identifiable).getLimits(limitType, side.toTwoSides());
            case TIE_LINE -> (Optional<LoadingLimits>) ((TieLine) identifiable).getLimits(limitType, side.toTwoSides());
            case TWO_WINDINGS_TRANSFORMER ->
                    (Optional<LoadingLimits>) ((TwoWindingsTransformer) identifiable).getLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER ->
                    (Optional<LoadingLimits>) ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
            default -> Optional.empty();
        };
    }
}
