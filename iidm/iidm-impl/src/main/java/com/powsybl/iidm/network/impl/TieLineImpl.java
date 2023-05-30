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
import com.powsybl.iidm.network.util.TieLineUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TieLineImpl extends AbstractIdentifiable<TieLine> implements TieLine {

    @Override
    public NetworkImpl getNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access network of removed tie line " + id);
        }
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Tie Line";
    }

    private DanglingLineImpl danglingLine1;

    private DanglingLineImpl danglingLine2;

    private final Ref<NetworkImpl> networkRef;

    private boolean removed = false;

    private final OperationalLimitsHolderImpl operationalLimitsHolder1;

    private final OperationalLimitsHolderImpl operationalLimitsHolder2;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = network;
        this.operationalLimitsHolder1 = new OperationalLimitsHolderImpl(this, "limits1");
        this.operationalLimitsHolder2 = new OperationalLimitsHolderImpl(this, "limits2");
    }

    void attachDanglingLines(DanglingLineImpl dl1, DanglingLineImpl dl2) {
        this.danglingLine1 = attach(dl1);
        this.danglingLine2 = attach(dl2);
    }

    private DanglingLineImpl attach(DanglingLineImpl danglingLine) {
        danglingLine.setTieLine(this);
        return danglingLine;
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(danglingLine1.getUcteXnodeCode()).orElseGet(() -> danglingLine2.getUcteXnodeCode());
    }

    @Override
    public DanglingLineImpl getDanglingLine1() {
        return danglingLine1;
    }

    @Override
    public DanglingLineImpl getDanglingLine2() {
        return danglingLine2;
    }

    @Override
    public DanglingLineImpl getDanglingLine(Branch.Side side) {
        switch (side) {
            case ONE:
                return danglingLine1;
            case TWO:
                return danglingLine2;
            default:
                throw new IllegalStateException("Unknown branch side " + side);
        }
    }

    @Override
    public DanglingLine getDanglingLine(String voltageLevelId) {
        if (danglingLine1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return danglingLine1;
        }
        if (danglingLine2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return danglingLine2;
        }
        return null;
    }

    // danglingLine1 and danglingLine2 are dangling lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        return TieLineUtil.getR(danglingLine1, danglingLine2);
    }

    // danglingLine1 and danglingLine2 are dangling lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        return TieLineUtil.getX(danglingLine1, danglingLine2);
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(danglingLine1, danglingLine2);
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(danglingLine1, danglingLine2);
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(danglingLine1, danglingLine2);
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(danglingLine1, danglingLine2);
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        // Remove dangling lines
        danglingLine1.removeTieLine();
        danglingLine2.removeTieLine();

        // Remove this tie line from the network
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    @Override
    public TerminalExt getTerminal1() {
        return danglingLine1.getTerminal();
    }

    @Override
    public TerminalExt getTerminal2() {
        return danglingLine2.getTerminal();
    }

    @Override
    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE: return getTerminal1();
            case TWO: return getTerminal2();
            default: throw new IllegalStateException();
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

        if (danglingLine1.getTerminal() == terminal) {
            return Side.ONE;
        } else if (danglingLine2.getTerminal() == terminal) {
            return Side.TWO;
        } else {
            throw new IllegalStateException("The terminal is not connected to this branch");
        }
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits1() {
        return operationalLimitsHolder1.getOperationalLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return operationalLimitsHolder1.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return operationalLimitsHolder1.getNullableOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return operationalLimitsHolder1.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return operationalLimitsHolder1.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return operationalLimitsHolder1.getNullableOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return operationalLimitsHolder1.newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return operationalLimitsHolder2.getOperationalLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return operationalLimitsHolder1.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return operationalLimitsHolder1.getNullableOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return operationalLimitsHolder1.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return operationalLimitsHolder2.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return operationalLimitsHolder2.getNullableOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return operationalLimitsHolder2.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return operationalLimitsHolder2.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return operationalLimitsHolder2.getNullableOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return operationalLimitsHolder2.newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return operationalLimitsHolder2.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return operationalLimitsHolder2.getNullableOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return operationalLimitsHolder2.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getCurrentLimits1();
        } else if (side == Branch.Side.TWO) {
            return getCurrentLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getActivePowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getActivePowerLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getApparentPowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getApparentPowerLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
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
            case ONE: return checkPermanentLimit1(limitReduction, type);
            case TWO: return checkPermanentLimit2(limitReduction, type);
            default: throw new IllegalStateException();
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
            case ONE: return checkTemporaryLimits1(limitReduction, type);
            case TWO: return checkTemporaryLimits2(limitReduction, type);
            default: throw new IllegalStateException();
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
