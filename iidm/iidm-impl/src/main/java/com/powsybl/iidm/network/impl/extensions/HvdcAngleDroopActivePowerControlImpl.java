/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TFloatArrayList;

import java.util.Objects;

/**
 * Active power control mode based on an offset in MW and a droop in MW/degree
 * ActivePowerSetpoint = p0 + droop * (angle1 - angle2)
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
public class HvdcAngleDroopActivePowerControlImpl extends AbstractMultiVariantIdentifiableExtension<HvdcLine> implements HvdcAngleDroopActivePowerControl {

    /**
     * Active power offset in MW
     */
    private TFloatArrayList p0;

    /**
     * Droop in MW/degree
     */
    private TFloatArrayList droop;

    /**
     * Enables or disables this active power control mode.
     * If this active power control mode is disabled, use the setpoint mode by default.
     */
    private TBooleanArrayList enabled;

    public HvdcAngleDroopActivePowerControlImpl(HvdcLine hvdcLine, float p0, float droop, boolean enabled) {
        super(hvdcLine);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.p0 = new TFloatArrayList(variantArraySize);
        this.droop = new TFloatArrayList(variantArraySize);
        this.enabled = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.p0.add(checkP0(p0, hvdcLine));
            this.droop.add(checkDroop(droop, hvdcLine));
            this.enabled.add(enabled);
        }
    }

    @Override
    public float getP0() {
        return p0.get(getVariantIndex());
    }

    @Override
    public float getDroop() {
        return droop.get(getVariantIndex());
    }

    @Override
    public boolean isEnabled() {
        return enabled.get(getVariantIndex());
    }

    @Override
    public HvdcAngleDroopActivePowerControl setP0(float p0, boolean dryRun) {
        float p = checkP0(p0, this.getExtendable());
        if (!dryRun) {
            this.p0.set(getVariantIndex(), p);
        }
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setDroop(float droop, boolean dryRun) {
        float d = checkDroop(droop, this.getExtendable());
        if (!dryRun) {
            this.droop.set(getVariantIndex(), d);
        }
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setEnabled(boolean enabled, boolean dryRun) {
        if (!dryRun) {
            this.enabled.set(getVariantIndex(), enabled);
        }
        return this;
    }

    private float checkP0(float p0, HvdcLine hvdcLine) {
        if (Float.isNaN(p0)) {
            throw new IllegalArgumentException(String.format("p0 value (%s) is invalid for HVDC line %s",
                p0,
                hvdcLine.getId()));
        }

        return p0;
    }

    private float checkDroop(float droop, HvdcLine hvdcLine) {
        if (Float.isNaN(droop)) {
            throw new IllegalArgumentException(String.format("droop value (%s) is invalid for HVDC line %s",
                droop,
                hvdcLine.getId()));
        }

        return droop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HvdcAngleDroopActivePowerControlImpl that = (HvdcAngleDroopActivePowerControlImpl) o;
        return Float.compare(that.getP0(), getP0()) == 0 &&
                Float.compare(that.getDroop(), getDroop()) == 0 &&
                isEnabled() == that.isEnabled();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getP0(), getDroop(), isEnabled());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        p0.ensureCapacity(p0.size() + number);
        droop.ensureCapacity(droop.size() + number);
        enabled.ensureCapacity(enabled.size() + number);
        for (int i = 0; i < number; ++i) {
            p0.add(p0.get(sourceIndex));
            droop.add(droop.get(sourceIndex));
            enabled.add(enabled.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        p0.remove(p0.size() - number, number);
        droop.remove(droop.size() - number, number);
        enabled.remove(enabled.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p0.set(index, p0.get(sourceIndex));
            droop.set(index, droop.get(sourceIndex));
            enabled.set(index, enabled.get(sourceIndex));
        }
    }
}
