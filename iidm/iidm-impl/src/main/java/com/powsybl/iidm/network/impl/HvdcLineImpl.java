/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineImpl extends AbstractIdentifiable<HvdcLine> implements HvdcLine, MultiVariantObject {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private double r;

    private double nominalV;

    private double maxP;

    // attributes depending on the variant

    private final TBooleanArrayList convertersMode;

    private final TDoubleArrayList activePowerSetpoint;

    //

    private final Ref<NetworkImpl> networkRef;

    private AbstractHvdcConverterStation<?> converterStation1;

    private AbstractHvdcConverterStation<?> converterStation2;

    HvdcLineImpl(String id, String name, boolean fictitious, double r, double nominalV, double maxP, ConvertersMode convertersMode, double activePowerSetpoint,
                 AbstractHvdcConverterStation<?> converterStation1, AbstractHvdcConverterStation<?> converterStation2,
                 Ref<NetworkImpl> networkRef) {
        super(id, name, fictitious);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int variantArraySize = networkRef.get().getVariantManager().getVariantArraySize();
        this.convertersMode = new TBooleanArrayList(variantArraySize);
        this.convertersMode.fill(0, variantArraySize, convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        this.activePowerSetpoint = new TDoubleArrayList(variantArraySize);
        this.activePowerSetpoint.fill(0, variantArraySize, activePowerSetpoint);
        this.converterStation1 = attach(converterStation1);
        this.converterStation2 = attach(converterStation2);
        this.networkRef = networkRef;
    }

    private AbstractHvdcConverterStation<?> attach(AbstractHvdcConverterStation<?> converterStation) {
        converterStation.setHvdcLine(this);
        return converterStation;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    private static ConvertersMode toEnum(boolean convertersMode) {
        return convertersMode ? ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER : ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
    }

    private static boolean fromEnum(ConvertersMode convertersMode) {
        return convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return toEnum(convertersMode.get(getNetwork().getVariantIndex()));
    }

    @Override
    public HvdcLineImpl setConvertersMode(ConvertersMode convertersMode) {
        ValidationUtil.checkConvertersMode(this, convertersMode);
        int variantIndex = getNetwork().getVariantIndex();
        boolean oldValue = this.convertersMode.get(variantIndex);
        this.convertersMode.set(variantIndex, fromEnum(Objects.requireNonNull(convertersMode)));
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("convertersMode", variantId, toEnum(oldValue), convertersMode);
        return this;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public HvdcLineImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getNominalV() {
        return nominalV;
    }

    @Override
    public HvdcLineImpl setNominalV(double nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public double getMaxP() {
        return maxP;
    }

    @Override
    public HvdcLineImpl setMaxP(double maxP) {
        ValidationUtil.checkHvdcMaxP(this, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return activePowerSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public HvdcLineImpl setActivePowerSetpoint(double activePowerSetpoint) {
        ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint);
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.activePowerSetpoint.set(variantIndex, activePowerSetpoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("activePowerSetpoint", variantId, oldValue, activePowerSetpoint);
        return this;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation(Side side) {
        return (side == Side.ONE) ? getConverterStation1() : getConverterStation2();
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation1() {
        return converterStation1;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation2() {
        return converterStation2;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        convertersMode.ensureCapacity(convertersMode.size() + number);
        convertersMode.fill(initVariantArraySize, initVariantArraySize + number, convertersMode.get(sourceIndex));

        activePowerSetpoint.ensureCapacity(activePowerSetpoint.size() + number);
        activePowerSetpoint.fill(initVariantArraySize, initVariantArraySize + number, activePowerSetpoint.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        activePowerSetpoint.remove(activePowerSetpoint.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            activePowerSetpoint.set(index, activePowerSetpoint.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        // Detach converter stations
        converterStation1.setHvdcLine(null);
        converterStation2.setHvdcLine(null);
        converterStation1 = null;
        converterStation2 = null;

        NetworkImpl network = getNetwork();
        network.getIndex().remove(this);
        network.getListeners().notifyRemoval(this);
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }
}
