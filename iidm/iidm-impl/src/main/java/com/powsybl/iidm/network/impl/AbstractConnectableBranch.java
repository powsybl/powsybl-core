/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractConnectableBranch<I extends Branch<I> & Connectable<I>> extends AbstractConnectable<I> implements Branch<I> {

    protected final OperationalLimitsHolderImpl operationalLimitsHolder1;

    protected final OperationalLimitsHolderImpl operationalLimitsHolder2;

    AbstractConnectableBranch(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
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
    public Terminal getTerminal(TwoSides side) {
        return BranchUtil.getFromSide(side, this::getTerminal1, this::getTerminal2);
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return BranchUtil.getTerminal(voltageLevelId, getTerminal1(), getTerminal2());
    }

    public TwoSides getSide(Terminal terminal) {
        return BranchUtil.getSide(terminal, getTerminal1(), getTerminal2());
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
    public Optional<CurrentLimits> getCurrentLimits(TwoSides side) {
        return BranchUtil.getFromSide(side, this::getCurrentLimits1, this::getCurrentLimits2);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        return BranchUtil.getFromSide(side, this::getActivePowerLimits1, this::getActivePowerLimits2);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        return BranchUtil.getFromSide(side, this::getApparentPowerLimits1, this::getApparentPowerLimits2);
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
        return BranchUtil.getOverloadDuration(checkTemporaryLimits1(LimitType.CURRENT), checkTemporaryLimits2(LimitType.CURRENT));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, float limitReduction, LimitType type) {
        return BranchUtil.getFromSide(side,
            () -> checkPermanentLimit1(limitReduction, type),
            () -> checkPermanentLimit2(limitReduction, type));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, float limitReduction, LimitType type) {
        return BranchUtil.getFromSide(side,
            () -> checkTemporaryLimits1(limitReduction, type),
            () -> checkTemporaryLimits2(limitReduction, type));
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
    }

    public double getValueForLimit(Terminal t, LimitType type) {
        return BranchUtil.getValueForLimit(t, type);
    }
}
