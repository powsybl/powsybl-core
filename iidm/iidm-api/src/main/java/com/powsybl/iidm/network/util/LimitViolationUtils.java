/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.Objects;

/**
 *
 * Helper methods for checking the occurence of overloads.
 *
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public final class LimitViolationUtils {

    public static final String PERMANENT_LIMIT_NAME = "permanent";

    private LimitViolationUtils() {
    }

    public static Overload checkTemporaryLimits(Branch<?> branch, TwoSides side, float limitReduction, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        return branch.getLimits(type, side)
                .map(limits -> getOverload(limits, i, limitReduction))
                .orElse(null);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, float limitReduction, double i, LimitType type) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        return transformer.getLeg(side).getLimits(type)
                .map(limits -> getOverload(limits, i, limitReduction))
                .orElse(null);
    }

    private static OverloadImpl getOverload(LoadingLimits limits, double i, float limitReduction) {
        double permanentLimit = limits.getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return null;
        }
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limits.getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit * limitReduction && i < tl.getValue() * limitReduction) {
                return new OverloadImpl(tl, previousLimitName, previousLimit);
            }
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
        }
        return null;
    }

    private static boolean checkPermanentLimitIfAny(LoadingLimits limits, double i, float limitReduction) {
        double permanentLimit = limits.getPermanentLimit();
        if (Double.isNaN(i) || Double.isNaN(permanentLimit)) {
            return false;
        }
        return i >= permanentLimit * limitReduction;
    }

    public static boolean checkPermanentLimit(Branch<?> branch, TwoSides side, float limitReduction, double i, LimitType type) {
        return branch.getLimits(type, side)
                .map(l -> checkPermanentLimitIfAny(l, i, limitReduction))
                .orElse(false);
    }

    public static boolean checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, float limitReduction, double i, LimitType type) {
        return transformer.getLeg(side).getLimits(type)
                .map(l -> checkPermanentLimitIfAny(l, i, limitReduction))
                .orElse(false);
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

    public static boolean checkPermanentLimit(ThreeWindingsTransformer transformer, ThreeSides side, float limitReduction, LimitType type) {
        return checkPermanentLimit(transformer, side, limitReduction, getValueForLimit(transformer.getTerminal(side), type), type);
    }

    public static Overload checkTemporaryLimits(ThreeWindingsTransformer transformer, ThreeSides side, float limitReduction, LimitType type) {
        return checkTemporaryLimits(transformer, side, limitReduction, getValueForLimit(transformer.getTerminal(side), type), type);
    }
}
