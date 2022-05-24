/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBranch<I extends Branch<I>> extends AbstractConnectable<I> implements Branch<I> {

    private final OperationalLimitsHolderImpl operationalLimitsHolder1;

    private final OperationalLimitsHolderImpl operationalLimitsHolder2;

    AbstractBranch(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(network, id, name, fictitious);
        operationalLimitsHolder1 = new OperationalLimitsHolderImpl(this, "limits1");
        operationalLimitsHolder2 = new OperationalLimitsHolderImpl(this, "limits2");
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
    public Optional<CurrentLimits> getCurrentLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.CURRENT, CurrentLimitsSet.class);
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.CURRENT, CurrentLimitsSet.class).orElse(null);
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet1() {
        return operationalLimitsHolder1.getOperationalLimitsSet(LimitType.CURRENT, CurrentLimitsSet.class);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class).orElse(null);
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet1() {
        return operationalLimitsHolder1.getOperationalLimitsSet(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return operationalLimitsHolder1.getActiveLimits(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class).orElse(null);
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet1() {
        return operationalLimitsHolder1.getOperationalLimitsSet(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return operationalLimitsHolder1.newCurrentLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return operationalLimitsHolder1.newApparentPowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.CURRENT, CurrentLimitsSet.class);
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.CURRENT, CurrentLimitsSet.class).orElse(null);
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet2() {
        return operationalLimitsHolder2.getOperationalLimitsSet(LimitType.CURRENT, CurrentLimitsSet.class);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class).orElse(null);
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet2() {
        return operationalLimitsHolder2.getOperationalLimitsSet(LimitType.ACTIVE_POWER, ActivePowerLimitsSet.class);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return operationalLimitsHolder2.getActiveLimits(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class).orElse(null);
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet2() {
        return operationalLimitsHolder2.getOperationalLimitsSet(LimitType.APPARENT_POWER, ApparentPowerLimitsSet.class);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return operationalLimitsHolder1.newActivePowerLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return operationalLimitsHolder2.newCurrentLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return operationalLimitsHolder2.newApparentPowerLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return operationalLimitsHolder2.newActivePowerLimits();
    }

    OperationalLimitsHolderImpl getLimitsHolder1() {
        return operationalLimitsHolder1;
    }

    OperationalLimitsHolderImpl getLimitsHolder2() {
        return operationalLimitsHolder2;
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT) || checkPermanentLimit2(limitReduction, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1(LimitType.CURRENT);
        Branch.Overload o2 = checkTemporaryLimits2(LimitType.CURRENT);
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public boolean checkPermanentLimit(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction, type);

            case TWO:
                return checkPermanentLimit2(limitReduction, type);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean checkPermanentLimit(Side side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction, type);

            case TWO:
                return checkTemporaryLimits2(limitReduction, type);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
    }

    public double getValueForLimit(Terminal t, LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return t.getP();
            case APPARENT_POWER:
                return Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT:
                return t.getI();
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }
}
