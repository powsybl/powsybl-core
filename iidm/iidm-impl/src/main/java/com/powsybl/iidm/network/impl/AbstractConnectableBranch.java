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

    private final FlowsLimitsHolder operationalLimitsHolder1;

    private final FlowsLimitsHolder operationalLimitsHolder2;

    AbstractConnectableBranch(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(network, id, name, fictitious);
        operationalLimitsHolder1 = new OperationalLimitsGroupsImpl(this, "limits1");
        operationalLimitsHolder2 = new OperationalLimitsGroupsImpl(this, "limits2");
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

    private FlowsLimitsHolder getOperationalLimitsHolder1() {
        return operationalLimitsHolder1;
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId1() {
        return getOperationalLimitsHolder1().getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups1() {
        return getOperationalLimitsHolder1().getOperationalLimitsGroups();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id) {
        return getOperationalLimitsHolder1().getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup1() {
        return getOperationalLimitsHolder1().getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup1(String id) {
        return getOperationalLimitsHolder1().newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup1(String id) {
        getOperationalLimitsHolder1().setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup1(String id) {
        getOperationalLimitsHolder1().removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup1() {
        getOperationalLimitsHolder1().cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return getOperationalLimitsHolder1().newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getOperationalLimitsHolder1().newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getOperationalLimitsHolder1().newApparentPowerLimits();
    }

    private FlowsLimitsHolder getOperationalLimitsHolder2() {
        return operationalLimitsHolder2;
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups2() {
        return getOperationalLimitsHolder2().getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId2() {
        return getOperationalLimitsHolder2().getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id) {
        return getOperationalLimitsHolder2().getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup2() {
        return getOperationalLimitsHolder2().getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup2(String id) {
        return getOperationalLimitsHolder2().newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup2(String id) {
        getOperationalLimitsHolder2().setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup2(String id) {
        getOperationalLimitsHolder2().removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup2() {
        getOperationalLimitsHolder2().cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getOperationalLimitsHolder2().newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getOperationalLimitsHolder2().newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getOperationalLimitsHolder2().newApparentPowerLimits();
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

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(double limitReductionValue) {
        return checkPermanentLimit1(limitReductionValue, LimitType.CURRENT) || checkPermanentLimit2(limitReductionValue, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        return BranchUtil.getOverloadDuration(checkTemporaryLimits1(LimitType.CURRENT), checkTemporaryLimits2(LimitType.CURRENT));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, double limitReductionValue, LimitType type) {
        return BranchUtil.getFromSide(side,
            () -> checkPermanentLimit1(limitReductionValue, type),
            () -> checkPermanentLimit2(limitReductionValue, type));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(double limitReductionValue, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.ONE, limitReductionValue, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(double limitReductionValue, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.TWO, limitReductionValue, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, double limitReductionValue, LimitType type) {
        return BranchUtil.getFromSide(side,
            () -> checkTemporaryLimits1(limitReductionValue, type),
            () -> checkTemporaryLimits2(limitReductionValue, type));
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(double limitReductionValue, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReductionValue, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(double limitReductionValue, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.TWO, limitReductionValue, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
    }

    public double getValueForLimit(Terminal t, LimitType type) {
        return BranchUtil.getValueForLimit(t, type);
    }
}
