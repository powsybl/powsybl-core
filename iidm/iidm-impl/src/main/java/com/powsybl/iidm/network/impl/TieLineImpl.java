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
    public Network getParentNetwork() {
        Network subnetwork1 = danglingLine1.getParentNetwork();
        Network subnetwork2 = danglingLine2.getParentNetwork();
        if (subnetwork1 == subnetwork2) {
            return subnetwork1;
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

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = network;
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
    public DanglingLineImpl getDanglingLine(Side side) {
        return BranchUtil.getFromSide(side, this::getDanglingLine1, this::getDanglingLine2);
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
        remove(false);
    }

    @Override
    public void remove(boolean updateDanglingLines) {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        if (updateDanglingLines) {
            updateDanglingLine(danglingLine1);
            updateDanglingLine(danglingLine2);
        }

        // Remove dangling lines
        danglingLine1.removeTieLine();
        danglingLine2.removeTieLine();

        // invalidate components
        network.getConnectedComponentsManager().invalidate();
        network.getSynchronousComponentsManager().invalidate();

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
        return BranchUtil.getFromSide(side, this::getTerminal1, this::getTerminal2);
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return BranchUtil.getTerminal(voltageLevelId, getTerminal1(), getTerminal2());
    }

    public Side getSide(Terminal terminal) {
        return BranchUtil.getSide(terminal, getTerminal1(), getTerminal2());
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits1() {
        return danglingLine1.getOperationalLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return danglingLine1.getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return danglingLine1.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return danglingLine1.getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return danglingLine1.newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return danglingLine2.getOperationalLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return danglingLine1.getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return danglingLine1.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return danglingLine2.getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return danglingLine2.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return danglingLine2.getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return danglingLine2.newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return danglingLine2.getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return danglingLine2.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(Side side) {
        return BranchUtil.getFromSide(side, this::getCurrentLimits1, this::getCurrentLimits2);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(Side side) {
        return BranchUtil.getFromSide(side, this::getActivePowerLimits1, this::getActivePowerLimits2);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(Side side) {
        return BranchUtil.getFromSide(side, this::getApparentPowerLimits1, this::getApparentPowerLimits2);
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
    public boolean checkPermanentLimit(Side side, float limitReduction, LimitType type) {
        return BranchUtil.getFromSide(side,
            () -> checkPermanentLimit1(limitReduction, type),
            () -> checkPermanentLimit2(limitReduction, type));
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
        return BranchUtil.getFromSide(side,
            () -> checkTemporaryLimits1(limitReduction, type),
            () -> checkTemporaryLimits2(limitReduction, type));
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
        return BranchUtil.getValueForLimit(t, type);
    }

    private static void updateDanglingLine(DanglingLine danglingLine) {
        // Only update if we have values
        if (!Double.isNaN(danglingLine.getBoundary().getP())) {
            danglingLine.setP0(-danglingLine.getBoundary().getP());
            if (danglingLine.getGeneration() != null) {
                // We do not reset regulation if we only have computed a dc load flow
                danglingLine.getGeneration().setTargetP(0.0);
            }
        }
        if (!Double.isNaN(danglingLine.getBoundary().getQ())) {
            danglingLine.setQ0(-danglingLine.getBoundary().getQ());
            if (danglingLine.getGeneration() != null) {
                // If q values are available a complete ac load flow has been computed, we reset regulation
                danglingLine.getGeneration().setTargetQ(0.0).setVoltageRegulationOn(false).setTargetV(Double.NaN);
            }
        }
    }
}
