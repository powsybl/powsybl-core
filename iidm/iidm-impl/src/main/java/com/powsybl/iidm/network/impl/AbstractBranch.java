/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBranch<I extends Connectable<I>> extends AbstractConnectable<I> implements CurrentLimitsOwner<Side> {

    private CurrentLimits limits1;

    private CurrentLimits limits2;

    AbstractBranch(String id, String name) {
        super(id, name);
    }

    public TerminalExt getTerminal1() {
        return terminals.get(0);
    }

    public TerminalExt getTerminal2() {
        return terminals.get(1);
    }

    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();

            case TWO:
                return getTerminal2();

            default:
                throw new AssertionError();
        }
    }

    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = getTerminal1().getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = getTerminal2().getVoltageLevel().getId().equals(voltageLevelId);
        if (side1 && side2) {
            throw new RuntimeException("Both terminals are connected to voltage level " + voltageLevelId);
        } else if (side1) {
            return getTerminal1();
        } else if (side2) {
            return getTerminal2();
        } else {
            throw new RuntimeException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    @Override
    public void setCurrentLimits(Branch.Side side, CurrentLimitsImpl limits) {
        switch (side) {
            case ONE:
                limits1 = limits;
                break;
            case TWO:
                limits2 = limits;
                break;
            default:
                throw new InternalError();
        }
    }

    public CurrentLimits getCurrentLimits(Side side) {
        switch (side) {
            case ONE:
                return limits1;
            case TWO:
                return limits2;
            default:
                throw new InternalError();
        }

    }

    public CurrentLimits getCurrentLimits1() {
        return limits1;
    }

    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    public CurrentLimits getCurrentLimits2() {
        return limits2;
    }

    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.TWO, this);
    }

    public boolean isOverloaded() {
        return isOverloaded(1.0f, Integer.MAX_VALUE);
    }

    public boolean isOverloaded(float limitReduction, int duration) {
        if (checkPermanentLimit1(limitReduction) && limits1.getTemporaryLimits().size() == 0) {
            return true;
        }
        if (checkPermanentLimit2(limitReduction) && limits2.getTemporaryLimits().size() == 0) {
            return true;
        }
        return checkTemporaryLimits1(limitReduction, duration) != null || checkTemporaryLimits2(limitReduction, duration) != null;
    }

    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1();
        Branch.Overload o2 = checkTemporaryLimits2();
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    public boolean checkPermanentLimit(Side side, float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction);

            case TWO:
                return checkPermanentLimit2(limitReduction);

            default:
                throw new AssertionError();
        }
    }

    public boolean checkPermanentLimit(Side side) {
        return checkPermanentLimit(side, 1f);
    }

    public boolean checkPermanentLimit1(float limitReduction) {
        return checkPermanentLimit(getTerminal1(), getCurrentLimits1(), limitReduction);
    }

    public boolean checkPermanentLimit1() {
        return checkPermanentLimit1(1f);
    }

    public boolean checkPermanentLimit2(float limitReduction) {
        return checkPermanentLimit(getTerminal2(), getCurrentLimits2(), limitReduction);
    }

    public boolean checkPermanentLimit2() {
        return checkPermanentLimit2(1f);
    }

    // return true if overloaded
    private static boolean checkPermanentLimit(Terminal terminal, CurrentLimits limits, float limitReduction) {
        float i = terminal.getI();
        if (limits != null && !Float.isNaN(limits.getPermanentLimit()) && !Float.isNaN(i)) {
            if (i >= limits.getPermanentLimit() * limitReduction) {
                return true;
            }
        }
        return false;
    }

    static final class OverloadImpl implements Branch.Overload {

        private final CurrentLimits.TemporaryLimit temporaryLimit;

        private final float previousLimit;

        private OverloadImpl(CurrentLimits.TemporaryLimit temporaryLimit, float previousLimit) {
            this.temporaryLimit = Objects.requireNonNull(temporaryLimit);
            this.previousLimit = previousLimit;
        }

        @Override
        public CurrentLimits.TemporaryLimit getTemporaryLimit() {
            return temporaryLimit;
        }

        @Override
        public float getPreviousLimit() {
            return previousLimit;
        }
    }

    public OverloadImpl checkTemporaryLimits(Side side, float limitReduction) {
        return checkTemporaryLimits(side, limitReduction, Integer.MAX_VALUE);
    }

    private OverloadImpl checkTemporaryLimits(Side side, float limitReduction, int duration) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction, duration);

            case TWO:
                return checkTemporaryLimits2(limitReduction, duration);

            default:
                throw new AssertionError();
        }
    }

    public OverloadImpl checkTemporaryLimits(Side side) {
        return checkTemporaryLimits(side, 1f);
    }

    public OverloadImpl checkTemporaryLimits1(float limitReduction) {
        return checkTemporaryLimits1(limitReduction, Integer.MAX_VALUE);
    }

    private OverloadImpl checkTemporaryLimits1(float limitReduction, int duration) {
        return checkTemporaryLimits(getTerminal1(), limits1, limitReduction, duration);
    }

    public OverloadImpl checkTemporaryLimits1() {
        return checkTemporaryLimits1(1f);
    }

    public OverloadImpl checkTemporaryLimits2(float limitReduction) {
        return checkTemporaryLimits(getTerminal2(), limits2, limitReduction);
    }

    private OverloadImpl checkTemporaryLimits2(float limitReduction, int duration) {
        return checkTemporaryLimits(getTerminal2(), limits2, limitReduction, duration);
    }

    public OverloadImpl checkTemporaryLimits2() {
        return checkTemporaryLimits2(1f);
    }

    private static OverloadImpl checkTemporaryLimits(TerminalExt t, CurrentLimits limits, float limitReduction) {
        return checkTemporaryLimits(t, limits, limitReduction, Integer.MAX_VALUE);
    }

    private static OverloadImpl checkTemporaryLimits(TerminalExt t, CurrentLimits limits, float limitReduction, int duration) {
        Objects.requireNonNull(t);
        float i = t.getI();
        if (limits != null && !Float.isNaN(limits.getPermanentLimit()) && !Float.isNaN(i)) {
            float previousLimit = limits.getPermanentLimit();
            for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) { // iterate in ascending order
                if (i >= previousLimit * limitReduction) {
                    if (i < tl.getValue() * limitReduction && duration < tl.getAcceptableDuration()) {
                        return null;
                    } else if (i < tl.getValue() * limitReduction && duration > tl.getAcceptableDuration()) {
                        return new OverloadImpl(tl, previousLimit);
                    }
                }
                previousLimit = tl.getValue();
            }
        }
        return null;
    }
}
