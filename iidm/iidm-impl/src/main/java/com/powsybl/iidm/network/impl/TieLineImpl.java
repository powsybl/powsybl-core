/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.iidm.network.util.TieLineUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
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
        Network subnetwork1 = boundaryLine1.getParentNetwork();
        Network subnetwork2 = boundaryLine2.getParentNetwork();
        if (subnetwork1 == subnetwork2) {
            return subnetwork1;
        }
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Tie Line";
    }

    private BoundaryLineImpl boundaryLine1;

    private BoundaryLineImpl boundaryLine2;

    private final Ref<NetworkImpl> networkRef;

    private boolean removed = false;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = network;
    }

    void attachBoundaryLines(BoundaryLineImpl dl1, BoundaryLineImpl dl2) {
        this.boundaryLine1 = attach(dl1);
        this.boundaryLine2 = attach(dl2);
    }

    private BoundaryLineImpl attach(BoundaryLineImpl boundaryLine) {
        boundaryLine.setTieLine(this);
        return boundaryLine;
    }

    @Override
    public String getPairingKey() {
        return Optional.ofNullable(boundaryLine1.getPairingKey()).orElseGet(() -> boundaryLine2.getPairingKey());
    }

    @Override
    public BoundaryLineImpl getBoundaryLine1() {
        return boundaryLine1;
    }

    @Override
    public BoundaryLineImpl getBoundaryLine2() {
        return boundaryLine2;
    }

    @Override
    public BoundaryLineImpl getBoundaryLine(TwoSides side) {
        return BranchUtil.getFromSide(side, this::getBoundaryLine1, this::getBoundaryLine2);
    }

    @Override
    public BoundaryLine getBoundaryLine(String voltageLevelId) {
        if (boundaryLine1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine1;
        }
        if (boundaryLine2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine2;
        }
        return null;
    }

    // boundaryLine1 and boundaryLine2 are dangling lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        return TieLineUtil.getR(boundaryLine1, boundaryLine2);
    }

    // boundaryLine1 and boundaryLine2 are dangling lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        return TieLineUtil.getX(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(boundaryLine1, boundaryLine2);
    }

    @Override
    public void remove() {
        remove(false);
    }

    @Override
    public void remove(boolean updateBoundaryLines) {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        if (updateBoundaryLines) {
            updateBoundaryLine(boundaryLine1);
            updateBoundaryLine(boundaryLine2);
        }

        // Remove dangling lines
        boundaryLine1.removeTieLine();
        boundaryLine2.removeTieLine();

        // invalidate components
        network.getConnectedComponentsManager().invalidate();
        network.getSynchronousComponentsManager().invalidate();

        // Remove this tie line from the network
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    @Override
    public boolean connectBoundaryLines() {
        return connectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL_BREAKER, null);
    }

    @Override
    public boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate) {
        return connectBoundaryLines(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate, TwoSides side) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminalsOfBoundaryLines(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnectBoundaryLines() {
        return disconnectBoundaryLines(SwitchPredicates.IS_CLOSED_BREAKER, null);
    }

    @Override
    public boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable) {
        return disconnectBoundaryLines(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable, TwoSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminalsOfBoundaryLines(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    private List<Terminal> getTerminalsOfBoundaryLines(TwoSides side) {
        return side == null ? List.of(getTerminal1(), getTerminal2()) : switch (side) {
            case ONE -> List.of(getTerminal1());
            case TWO -> List.of(getTerminal2());
        };
    }

    @Override
    public TerminalExt getTerminal1() {
        return boundaryLine1.getTerminal();
    }

    @Override
    public TerminalExt getTerminal2() {
        return boundaryLine2.getTerminal();
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
    public Optional<String> getSelectedOperationalLimitsGroupId1() {
        return boundaryLine1.getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups1() {
        return boundaryLine1.getOperationalLimitsGroups();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id) {
        return boundaryLine1.getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup1() {
        return boundaryLine1.getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup1(String id) {
        return boundaryLine1.newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup1(String id) {
        boundaryLine1.setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup1(String id) {
        boundaryLine1.removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup1() {
        boundaryLine1.cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup1() {
        return boundaryLine1.getOrCreateSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup2() {
        return boundaryLine2.getOrCreateSelectedOperationalLimitsGroup();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups2() {
        return boundaryLine2.getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId2() {
        return boundaryLine2.getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id) {
        return boundaryLine2.getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup2() {
        return boundaryLine2.getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup2(String id) {
        return boundaryLine2.newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup2(String id) {
        boundaryLine2.setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup2(String id) {
        boundaryLine2.removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup2() {
        boundaryLine2.cancelSelectedOperationalLimitsGroup();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
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

    private static void updateBoundaryLine(BoundaryLine boundaryLine) {
        // Only update if we have values
        if (!Double.isNaN(boundaryLine.getBoundary().getP())) {
            boundaryLine.setP0(-boundaryLine.getBoundary().getP());
            if (boundaryLine.getGeneration() != null) {
                // We do not reset regulation if we only have computed a dc load flow
                boundaryLine.getGeneration().setTargetP(0.0);
            }
        }
        if (!Double.isNaN(boundaryLine.getBoundary().getQ())) {
            boundaryLine.setQ0(-boundaryLine.getBoundary().getQ());
            if (boundaryLine.getGeneration() != null) {
                // If q values are available a complete ac load flow has been computed, we reset regulation
                boundaryLine.getGeneration().setTargetQ(0.0).setVoltageRegulationOn(false).setTargetV(Double.NaN);
            }
        }
    }
}
