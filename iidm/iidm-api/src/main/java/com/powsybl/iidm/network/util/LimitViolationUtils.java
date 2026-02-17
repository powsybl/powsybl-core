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

import java.util.*;
import java.util.function.Function;

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

    /**
     * @deprecated use checkAllTemporaryLimits(branch, side, limitsComputer, i, type).iterator().next() for migration
     */
    @Deprecated(since = "7.2.0", forRemoval = true)
    public static Overload checkTemporaryLimits(Branch<?> branch, TwoSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        return getLimits(branch, side.toThreeSides(), type, limitsComputer)
                .map(limits -> getOverload(limits, i))
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * @deprecated use checkAllTemporaryLimits(transformer, side, limitsComputer, i, type).iterator().next() for migration
     */
    @Deprecated(since = "7.2.0", forRemoval = true)
    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        return getLimits(transformer, side, type, limitsComputer)
                .map(limits -> getOverload(limits, i))
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * Checks temporary limits on all selected sets (can be multiple selected) on the given side of the branch, for the limitType
     * @param branch the branch on which to check the limits
     * @param side the side of the branch to check
     * @param limitsComputer how to access the limit, refer to {@link LimitsComputer}
     * @param i the value at the given side of the branch that is of the type limitType (for example, if we are checking the current, it will be the intensity in Ampere)
     * @param type the type we are checking the limit of
     * @return a collection of {@link Overload} representing all the violations that happened on selected limit sets, on the <code>side</code> of the <code>branch</code> when checking the <code>type</code> with a value of <code>i</code> going through it
     */
    public static Collection<Overload> checkAllTemporaryLimits(Branch<?> branch, TwoSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        return limitsComputer.computeLimits(branch, type, side.toThreeSides(), false)
                .stream()
                .map(limits -> getOverload(limits, i))
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Checks temporary limits on all selected sets (can be multiple selected) on the given side of the transformer, for the limitType
     * @param transformer the transformer on which to check the limits
     * @param side the side of the transformer to check
     * @param limitsComputer how to access the limit, refer to {@link LimitsComputer}
     * @param i the value at the given side of the transformer that is of the type limitType (for example, if we are checking the current, it will be the intensity in Ampere)
     * @param type the type we are checking the limit of
     * @return a collection of {@link Overload} representing all the violations that happened on selected limits, on the <code>side</code> of the <code>transformer</code> when checking the <code>type</code> with a value of <code>i</code> going through it
     */
    public static Collection<Overload> checkAllTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double i, LimitType type) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        return limitsComputer.computeLimits(transformer, type, side, false)
                .stream()
                .map(limits -> getOverload(limits, i))
                .flatMap(Optional::stream)
                .toList();
    }

    private static OverloadImpl createOverload(LoadingLimits.TemporaryLimit tl,
                                               double previousLimit,
                                               String previousLimitName,
                                               LimitsContainer<LoadingLimits> limitsContainer,
                                               boolean isFirstTemporaryLimit,
                                               int previousAcceptableDuration,
                                               double limitReductionValue) {
        double limit = previousLimit;
        double reduction = limitReductionValue;
        String operationalLimitsGroupId = "";
        if (limitsContainer != null) {
            operationalLimitsGroupId = limitsContainer.getOperationalLimitsGroupId();
            if (limitsContainer.isDistinct()) {
                AbstractDistinctLimitsContainer<?, ?> container = (AbstractDistinctLimitsContainer<?, ?>) limitsContainer;
                if (isFirstTemporaryLimit) {
                    limit = container.getOriginalPermanentLimit();
                    reduction = container.getPermanentLimitReduction();
                } else {
                    limit = container.getOriginalTemporaryLimit(previousAcceptableDuration);
                    reduction = container.getTemporaryLimitReduction(previousAcceptableDuration);
                }
            }
        }
        return tl != null ?
            new OverloadImpl(tl, previousLimitName, limit, reduction, operationalLimitsGroupId) :
            new OverloadImpl(previousLimitName, limit, reduction, operationalLimitsGroupId);
    }

    private static OverloadImpl getOverload(LoadingLimits limits, double i, double limitReductionValue) {
        double permanentLimit = limits.getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return null;
        }
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limits.getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        LoadingLimits.TemporaryLimit lastTemporaryLimit = null;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit * limitReductionValue && i < tl.getValue() * limitReductionValue) {
                return createOverload(tl, previousLimit, previousLimitName, null, false, -1, limitReductionValue);
            }
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
            lastTemporaryLimit = tl;
        }
        if (lastTemporaryLimit != null && i >= limitReductionValue * lastTemporaryLimit.getValue()) {
            return createOverload(null, previousLimit, previousLimitName, null, temporaryLimits.size() == 1, -1, limitReductionValue);
        }
        return null;
    }

    public static Optional<Overload> getOverload(LimitsContainer<LoadingLimits> limitsContainer, double i) {
        double permanentLimit = limitsContainer.getLimits().getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return Optional.empty();
        }
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limitsContainer.getLimits().getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        int previousAcceptableDuration = 0; // never mind initialisation it will be overridden with first loop
        boolean isFirstTemporaryLimit = true;
        LoadingLimits.TemporaryLimit lastTemporaryLimit = null;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit && i < tl.getValue()) {
                return Optional.of(createOverload(tl, previousLimit, previousLimitName, limitsContainer, isFirstTemporaryLimit, previousAcceptableDuration, 1));
            }
            isFirstTemporaryLimit = false;
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
            previousAcceptableDuration = tl.getAcceptableDuration();
            lastTemporaryLimit = tl;
        }
        if (lastTemporaryLimit != null && i >= lastTemporaryLimit.getValue()) {
            return Optional.of(createOverload(null, previousLimit, previousLimitName, limitsContainer, temporaryLimits.size() == 1, previousAcceptableDuration, 1));
        }
        return Optional.empty();
    }

    public static PermanentLimitCheckResult checkPermanentLimitIfAny(LimitsContainer<LoadingLimits> limitsContainer, double i) {
        return checkPermanentLimitIfAny(limitsContainer, i, 1);
    }

    private static PermanentLimitCheckResult checkPermanentLimitIfAny(LimitsContainer<LoadingLimits> limitsContainer, double i, double limitReductionValue) {
        double permanentLimit = limitsContainer.getLimits().getPermanentLimit();
        double originalPermanentLimit = limitsContainer.getOriginalLimits().getPermanentLimit();
        String opGroupId = limitsContainer.getOperationalLimitsGroupId();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return new PermanentLimitCheckResult(false, Double.NaN, limitReductionValue, opGroupId);
        }
        if (i >= permanentLimit * limitReductionValue) {
            return new PermanentLimitCheckResult(true, originalPermanentLimit, limitsContainer.isDistinct() ? ((AbstractDistinctLimitsContainer<?, ?>) limitsContainer).getPermanentLimitReduction() : limitReductionValue, opGroupId);
        }
        return new PermanentLimitCheckResult(false, originalPermanentLimit, limitReductionValue, opGroupId);
    }

    /**
     * Checks if the value <code>i</code> goes over any permanent limit of the <code>type</code>, for the <code>side</code> of the <code>identifiable</code>, taking into
     * account any potential modification of that limit by a factor <code>limitReductionValue</code>
     * @param identifiable the identifiable on which to check the permanent limits (usually a branch or a three-winding transformer)
     * @param side the side of the identifiable to look (two sides)
     * @param limitReductionValue used to reduce the permanent limit
     * @param i the physical value of electrical value of the given type (Ampere for current, MVar for reactive power, MW for active power)
     * @param type the type of the electrical value we are checking
     * @return true if <code>i</code> is above any of the selected {@link OperationalLimitsGroup} permanent limit, false otherwise
     */
    public static boolean checkPermanentLimit(Identifiable<?> identifiable, TwoSides side, double limitReductionValue, double i, LimitType type) {
        return checkPermanentLimit(identifiable, side.toThreeSides(), limitReductionValue, i, type);
    }

    /**
     * Checks if the value <code>i</code> goes over any permanent limit of the <code>type</code>, for the <code>side</code> of the <code>identifiable</code>, taking into
     * account any potential modification of that limit by a factor <code>limitReductionValue</code>
     * @param identifiable the identifiable on which to check the permanent limits (usually a branch or a three-winding transformer)
     * @param side the side of the identifiable to look (three sides)
     * @param limitReductionValue used to reduce the permanent limit
     * @param i the physical value of electrical value of the given type (Ampere for current, MVar for reactive power, MW for active power)
     * @param type the type of the electrical value we are checking
     * @return true if <code>i</code> is above any of the selected {@link OperationalLimitsGroup} permanent limit, false otherwise
     */
    public static boolean checkPermanentLimit(Identifiable<?> identifiable, ThreeSides side, double limitReductionValue, double i, LimitType type) {
        for (LimitsContainer<LoadingLimits> limit : getAllLimits(identifiable, side, type, LimitsComputer.NO_MODIFICATIONS)) {
            if (checkPermanentLimitIfAny(limit, i, limitReductionValue).isOverload()) {
                return true;
            }
        }
        return false;
    }

    public static PermanentLimitCheckResult checkPermanentLimit(Branch<?> branch, TwoSides side, double i, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return getLimits(branch, side.toThreeSides(), type, computer)
                .map(l -> checkPermanentLimitIfAny(l, i))
                .orElse(new PermanentLimitCheckResult(false, Double.NaN, 1.0, ""));
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
        return checkAllTemporaryLimits(transformer, side, computer, getValueForLimit(transformer.getTerminal(side), type), type).iterator().next();
    }

    /**
     * Return the limit associated with the <code>side</code> of the <code>identifiable</code> of the <code>type</code>,
     * only for the last selected {@link OperationalLimitsGroup}, as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()}
     * @param identifiable on what we want to check the limit of the last selected group
     * @param side the side of the <code>identifiable</code> we want the limit of
     * @param type the type of the limit, refer to {@link LimitType}
     * @param computer how the limit is calculated (could be a reduced limit for example)
     * @return an {@link LimitsContainer} with the limit corresponding to the last selected {@link OperationalLimitsGroup},
     * of the <code>type</code> on the <code>side</code> of the <code>identifiable</code>, calculated using <code>computer</code>,
     * if any {@link OperationalLimitsGroup} was selected.
     * An empty {@link Optional} otherwise.
     */
    public static Optional<? extends LimitsContainer<LoadingLimits>> getLimits(Identifiable<?> identifiable, ThreeSides side, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        Optional<String> groupId = switch (identifiable.getType()) {
            case LINE, TWO_WINDINGS_TRANSFORMER, TIE_LINE -> {
                Branch<?> branch = (Branch<?>) identifiable;
                yield side.toTwoSides() == TwoSides.ONE ? branch.getSelectedOperationalLimitsGroupId1() : branch.getSelectedOperationalLimitsGroupId2();
            }
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getLeg(side).getSelectedOperationalLimitsGroupId();
            default -> Optional.empty();
        };
        return groupId.flatMap(s -> computer.computeLimits(identifiable, type, side, false).stream()
                .filter(container -> s.equals(container.getOperationalLimitsGroupId()))
                .findFirst());
    }

    /**
     * Get all the limits of {@link OperationalLimitsGroup} that are currently selected on the <code>side</code>
     * of the <code>identifiable</code> that are of the given <code>type</code>, calculated using the <code>computer</code>.
     * This is done for all the selected groups as defined by {@link FlowsLimitsHolder#getAllSelectedOperationalLimitsGroups()}
     * @param identifiable on what we want to check all the limits of all the selected groups
     * @param side the side of the <code>identifiable</code> we want the limits of
     * @param type the type of the limits, refer to {@link LimitType}
     * @param computer how the limit is calculated (could be a reduced limit for example)
     * @return a collection of {@link LimitsContainer} containing the limits corresponding to all selected {@link OperationalLimitsGroup},
     * of the <code>type</code> on the <code>side</code> of the <code>identifiable</code>, calculated using <code>computer</code>.
     * Might be empty if no {@link OperationalLimitsGroup} is selected.
     */
    public static Collection<? extends LimitsContainer<LoadingLimits>> getAllLimits(Identifiable<?> identifiable, ThreeSides side, LimitType type, LimitsComputer<Identifiable<?>, LoadingLimits> computer) {
        return computer.computeLimits(identifiable, type, side, false);
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

    /**
     * Get the limit associated to the last selected {@link OperationalLimitsGroup} ( as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()})
     * of the <code>side</code> of <code>identifiable</code>, that is of the given <code>type</code>
     * @param identifiable on what we want to check the limit of the last selected group
     * @param side the side of the <code>identifiable</code> we want the limit of
     * @param limitType the type of the limit, refer to {@link LimitType}
     * @return the {@link LoadingLimits} of the last selected {@link OperationalLimitsGroup} of the <code>side</code> of <code>identifiable</code>,
     * that is of the given <code>type</code> if any was selected. An empty {@link Optional} otherwise.
     */
    public static Optional<LoadingLimits> getLoadingLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        Optional<? extends LoadingLimits> limit = switch (identifiable.getType()) {
            case LINE, TIE_LINE, TWO_WINDINGS_TRANSFORMER -> ((Branch<?>) identifiable).getLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER ->
                ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
            default -> Optional.empty();
        };
        return limit.map(Function.identity());
    }

    /**
     * Get all the limits of {@link OperationalLimitsGroup} that are currently selected on the <code>side</code>
     * of the <code>identifiable</code> that are of the given <code>type</code>
     * This is done for all the selected groups as defined by {@link FlowsLimitsHolder#getAllSelectedOperationalLimitsGroups()}
     * @param identifiable on what we want to check all the limits of all the selected groups
     * @param side the side of the <code>identifiable</code> we want the limits of
     * @param limitType the type of the limits, refer to {@link LimitType}
     * @return a collection containing all the limits associated to each selected {@link OperationalLimitsGroup} of <code>identifiable</code>,
     * on the <code>side</code> of the given <code>type</code>.
     * Might be empty if none is selected.
     */
    public static Collection<LoadingLimits> getAllSelectedLoadingLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        Collection<? extends LoadingLimits> limits = switch (identifiable.getType()) {
            case LINE, TIE_LINE, TWO_WINDINGS_TRANSFORMER -> ((Branch<?>) identifiable).getAllSelectedLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getLeg(side).getAllSelectedLimits(limitType);
            default -> Collections.emptyList();
        };
        //prevent unchecked type cast by copying
        return new ArrayList<>(limits);
    }

    /**
     * Get all the {@link OperationalLimitsGroup} that are selected on this <code>side</code> of the <code>identifiable</code>
     * @param identifiable the identifiable on which to get the groups
     * @param side the side on which to look for the groups
     * @return a collection {@link OperationalLimitsGroup} of the <code>identifiable</code> on the given <code>side</code>.
     * Might be empty if none is selected.
     * Will be empty if groups cannot be defined on this identifiable (ie something other than a line, a two / three windings transformer or a tie line)
     */
    public static Collection<OperationalLimitsGroup> getAllSelectedLimitsGroups(Identifiable<?> identifiable, ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE, TWO_WINDINGS_TRANSFORMER, TIE_LINE -> ((Branch<?>) identifiable).getAllSelectedOperationalLimitsGroups(side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getLeg(side).getAllSelectedOperationalLimitsGroups();
            default -> List.of();
        };
    }

}
