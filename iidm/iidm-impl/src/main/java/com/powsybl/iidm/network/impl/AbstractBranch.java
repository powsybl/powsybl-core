/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBranch<I extends Branch<I>> extends AbstractConnectable<I> implements CurrentLimitsOwner<Side>, Branch<I> {

    private CurrentLimits limits1;

    private CurrentLimits limits2;

    AbstractBranch(String id, String name, boolean fictitious) {
        super(id, name, fictitious);
    }

    @Override
    public TerminalExt getTerminal1() {
        return terminals.get(0);
    }

    @Override
    public TerminalExt getTerminal2() {
        return terminals.get(1);
    }

    @Override
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

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = getTerminal1().getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = getTerminal2().getVoltageLevel().getId().equals(voltageLevelId);
        if (side1 && side2) {
            throw new PowsyblException("Both terminals are connected to voltage level " + voltageLevelId);
        } else if (side1) {
            return getTerminal1();
        } else if (side2) {
            return getTerminal2();
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    public Side getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (terminals.get(0) == terminal) {
            return Side.ONE;
        } else if (terminals.get(1) == terminal) {
            return Side.TWO;
        } else {
            throw new AssertionError("The terminal is not connected to this branch");
        }
    }

    @Override
    public void setCurrentLimits(Branch.Side side, CurrentLimitsImpl limits) {
        switch (side) {
            case ONE:
                CurrentLimits oldValue1 = limits1;
                limits1 = limits;
                notifyUpdate("currentLimits1", oldValue1, limits1);
                break;
            case TWO:
                CurrentLimits oldValue2 = limits2;
                limits2 = limits;
                notifyUpdate("currentLimits2", oldValue2, limits2);
                break;
            default:
                throw new AssertionError("Unexpected Branch.Side value: " + side);
        }
    }

    @Override
    public CurrentLimits getCurrentLimits(Side side) {
        switch (side) {
            case ONE:
                return limits1;
            case TWO:
                return limits2;
            default:
                throw new AssertionError("Unexpected Branch.Side value: " + side);
        }

    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return limits1;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return limits2;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.TWO, this);
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
        return checkPermanentLimit1(limitReduction) || checkPermanentLimit2(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1();
        Branch.Overload o2 = checkTemporaryLimits2();
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
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

    @Override
    public boolean checkPermanentLimit(Side side) {
        return checkPermanentLimit(side, 1f);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public boolean checkPermanentLimit1() {
        return checkPermanentLimit1(1f);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public boolean checkPermanentLimit2() {
        return checkPermanentLimit2(1f);
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side, float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction);

            case TWO:
                return checkTemporaryLimits2(limitReduction);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side) {
        return checkTemporaryLimits(side, 1f);
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public Branch.Overload checkTemporaryLimits1() {
        return checkTemporaryLimits1(1f);
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public Branch.Overload checkTemporaryLimits2() {
        return checkTemporaryLimits2(1f);
    }
}
