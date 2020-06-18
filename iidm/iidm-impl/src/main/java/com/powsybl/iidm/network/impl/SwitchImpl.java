/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchImpl extends AbstractIdentifiable<Switch> implements Switch, MultiVariantObject {

    private final OperationalLimitsHolderImpl operationalLimitsHolder;

    private final VoltageLevelExt voltageLevel;

    private final SwitchKind kind;

    private final TBooleanArrayList open;

    private final TBooleanArrayList retained;

    SwitchImpl(VoltageLevelExt voltageLevel,
               String id, String name, boolean fictitious, SwitchKind kind, final boolean open, boolean retained) {
        super(id, name, fictitious);
        this.voltageLevel = voltageLevel;
        this.kind = kind;
        int variantArraySize = voltageLevel.getNetwork().getVariantManager().getVariantArraySize();
        this.open = new TBooleanArrayList(variantArraySize);
        this.retained = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.open.add(open);
            this.retained.add(retained);
        }
        operationalLimitsHolder = new OperationalLimitsHolderImpl(this, "limits");
    }

    @Override
    public NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public SwitchKind getKind() {
        return kind;
    }

    @Override
    public boolean isOpen() {
        return open.get(getNetwork().getVariantIndex());
    }

    @Override
    public void setOpen(boolean open) {
        NetworkImpl network = getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.open.get(index);
        if (oldValue != open) {
            this.open.set(index, open);
            String variantId = network.getVariantManager().getVariantId(index);
            network.getListeners().notifyUpdate(this, "open", variantId, oldValue, open);
            voltageLevel.invalidateCache();
        }
    }

    @Override
    public boolean isRetained() {
        return retained.get(getNetwork().getVariantIndex());
    }

    @Override
    public void setRetained(boolean retained) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        NetworkImpl network = getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.retained.get(index);
        if (oldValue != retained) {
            this.retained.set(index, retained);
            String variantId = network.getVariantManager().getVariantId(index);
            network.getListeners().notifyUpdate(this, "retained", variantId, oldValue, retained);
            voltageLevel.invalidateCache();
        }
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        if (oldValue != fictitious) {
            this.fictitious = fictitious;
            voltageLevel.invalidateCache();
            NetworkImpl network = getNetwork();
            network.getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        open.ensureCapacity(open.size() + number);
        open.fill(initVariantArraySize, initVariantArraySize + number, open.get(sourceIndex));
        retained.ensureCapacity(retained.size() + number);
        retained.fill(initVariantArraySize, initVariantArraySize + number, retained.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        open.remove(open.size() - number, number);
        retained.remove(retained.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
            retained.set(index, retained.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Switch";
    }

    @Override
    public List<OperationalLimits> getOperationalLimits() {
        return operationalLimitsHolder.getOperationalLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return operationalLimitsHolder.newCurrentLimits();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return operationalLimitsHolder.newApparentPowerLimits();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return operationalLimitsHolder.newActivePowerLimitsAdder();
    }

    @Override
    public VoltageLimits getVoltageLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.VOLTAGE, VoltageLimits.class);
    }

    @Override
    public VoltageLimitsAdder newVoltageLimits() {
        return operationalLimitsHolder.newVoltageLimits();
    }
}
