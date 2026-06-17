/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedFloatArrayList;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

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
    private final ExtendedFloatArrayList p0;

    /**
     * Droop in MW/degree
     */
    private final ExtendedFloatArrayList droop;

    /**
     * Enables or disables this active power control mode.
     * If this active power control mode is disabled, use the setpoint mode by default.
     */
    private final ExtendedBooleanArrayList enabled;

    public HvdcAngleDroopActivePowerControlImpl(HvdcLine hvdcLine, float p0, float droop, boolean enabled) {
        super(hvdcLine);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        checkP0(p0, hvdcLine);
        checkDroop(droop, hvdcLine);
        this.p0 = new ExtendedFloatArrayList(variantArraySize, p0);
        this.droop = new ExtendedFloatArrayList(variantArraySize, droop);
        this.enabled = new ExtendedBooleanArrayList(variantArraySize, enabled);
    }

    @Override
    public float getP0() {
        return p0.getFloat(getVariantIndex());
    }

    @Override
    public float getDroop() {
        return droop.getFloat(getVariantIndex());
    }

    @Override
    public boolean isEnabled() {
        return enabled.getBoolean(getVariantIndex());
    }

    @Override
    public HvdcAngleDroopActivePowerControl setP0(float p0) {
        this.p0.set(getVariantIndex(), checkP0(p0, this.getExtendable()));
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setDroop(float droop) {
        this.droop.set(getVariantIndex(), checkDroop(droop, this.getExtendable()));
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setEnabled(boolean enabled) {
        this.enabled.set(getVariantIndex(), enabled);
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
        p0.growAndFill(number, p0.getFloat(sourceIndex));
        droop.growAndFill(number, droop.getFloat(sourceIndex));
        enabled.growAndFill(number, enabled.getBoolean(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        p0.removeElements(number);
        droop.removeElements(number);
        enabled.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p0.set(index, p0.getFloat(sourceIndex));
            droop.set(index, droop.getFloat(sourceIndex));
            enabled.set(index, enabled.getBoolean(sourceIndex));
        }
    }
}
