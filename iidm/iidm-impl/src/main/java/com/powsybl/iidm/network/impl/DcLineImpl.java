/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineImpl extends AbstractDcConnectable<DcLine> implements DcLine {

    public static final String R_ATTRIBUTE = "r";

    private static final String LIMITS_ATTRIBUTE = "limits";

    private static final Map<LimitType, String> UNSUPPORTED_LIMITS = Map.of(
            LimitType.ACTIVE_POWER, "active power limits are not supported: the two DC terminals carry different active power",
            LimitType.APPARENT_POWER, "apparent power limits are not supported: a DC line carries no reactive power");

    private double r;

    private final OperationalLimitsGroupsImpl operationalLimitsGroups;

    DcLineImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double r) {
        super(ref, subnetworkRef, id, name, fictitious);
        this.r = r;
        this.operationalLimitsGroups = new OperationalLimitsGroupsImpl(this, LIMITS_ATTRIBUTE, UNSUPPORTED_LIMITS);
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line";
    }

    @Override
    public DcTerminal getDcTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal1");
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal2");
        return this.dcTerminals.get(1);
    }

    @Override
    public DcTerminal getDcTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal");
        if (side == TwoSides.ONE) {
            return this.dcTerminals.get(0);
        } else if (side == TwoSides.TWO) {
            return this.dcTerminals.get(1);
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public TwoSides getSide(DcTerminal dcTerminal) {
        Objects.requireNonNull(dcTerminal);
        if (getDcTerminal1() == dcTerminal) {
            return TwoSides.ONE;
        } else if (getDcTerminal2() == dcTerminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The DC terminal is not connected to this DC line");
        }
    }

    @Override
    public double getR() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, R_ATTRIBUTE);
        return this.r;
    }

    @Override
    public DcLine setR(double r) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, R_ATTRIBUTE);
        ValidationUtil.checkDoubleParamPositive(this, r, R_ATTRIBUTE);
        double oldValue = this.r;
        this.r = r;
        getNetwork().getListeners().notifyUpdate(this, R_ATTRIBUTE, oldValue, r);
        return this;
    }

    private void checkAccess() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, LIMITS_ATTRIBUTE);
    }

    private void checkModification() {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, LIMITS_ATTRIBUTE);
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
        checkAccess();
        return operationalLimitsGroups.getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId() {
        checkAccess();
        return operationalLimitsGroups.getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Collection<String> getAllSelectedOperationalLimitsGroupIds() {
        checkAccess();
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroupIds();
    }

    @Override
    public List<String> getAllSelectedOperationalLimitsGroupIdsOrdered() {
        checkAccess();
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroupIdsOrdered();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        checkAccess();
        return operationalLimitsGroups.getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
        checkAccess();
        return operationalLimitsGroups.getSelectedOperationalLimitsGroup();
    }

    @Override
    public List<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups() {
        checkAccess();
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroups();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
        checkModification();
        return operationalLimitsGroups.newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup(String id) {
        checkModification();
        operationalLimitsGroups.setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void addSelectedOperationalLimitsGroups(String... ids) {
        checkModification();
        operationalLimitsGroups.addSelectedOperationalLimitsGroups(ids);
    }

    @Override
    public void removeOperationalLimitsGroup(String id) {
        checkModification();
        operationalLimitsGroups.removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup() {
        checkModification();
        operationalLimitsGroups.cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public void deselectOperationalLimitsGroups(String... ids) {
        checkModification();
        operationalLimitsGroups.deselectOperationalLimitsGroups(ids);
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup() {
        checkModification();
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        checkModification();
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        checkModification();
        // before getOrCreate, or a refusal leaves an empty group behind
        operationalLimitsGroups.checkSupported(LimitType.ACTIVE_POWER);
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        checkModification();
        operationalLimitsGroups.checkSupported(LimitType.APPARENT_POWER);
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
    }
}
