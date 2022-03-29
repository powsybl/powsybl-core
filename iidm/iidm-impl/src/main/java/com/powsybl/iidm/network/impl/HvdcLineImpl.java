/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineImpl extends AbstractIdentifiable<HvdcLine> implements HvdcLine {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private double r;

    private double nominalV;

    private double maxP;

    // attributes depending on the variant

    private final TIntArrayList convertersMode;

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
        this.convertersMode = new TIntArrayList(variantArraySize);
        this.convertersMode.fill(0, variantArraySize, convertersMode != null ? convertersMode.ordinal() : -1);
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

    @Override
    public ConvertersMode getConvertersMode() {
        int variantIndex = networkRef.get().getVariantIndex();
        return convertersMode.get(variantIndex) != -1 ? ConvertersMode.values()[convertersMode.get(variantIndex)] : null;
    }

    @Override
    public HvdcLineImpl setConvertersMode(ConvertersMode convertersMode) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkConvertersMode(this, convertersMode, n.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0);
        int variantIndex = n.getVariantIndex();
        ConvertersMode oldValue = this.convertersMode.get(variantIndex) != -1 ? ConvertersMode.values()[this.convertersMode.get(variantIndex)] : null;
        this.convertersMode.set(variantIndex, convertersMode != null ? convertersMode.ordinal() : -1);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("convertersMode", variantId, oldValue, convertersMode);
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
        NetworkImpl n = getNetwork();
        ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint,
                n.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0);
        int variantIndex = n.getVariantIndex();
        double oldValue = this.activePowerSetpoint.set(variantIndex, activePowerSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
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
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        convertersMode.ensureCapacity(convertersMode.size() + number);
        convertersMode.fill(initVariantArraySize, initVariantArraySize + number, convertersMode.get(sourceIndex));

        activePowerSetpoint.ensureCapacity(activePowerSetpoint.size() + number);
        activePowerSetpoint.fill(initVariantArraySize, initVariantArraySize + number, activePowerSetpoint.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        activePowerSetpoint.remove(activePowerSetpoint.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            activePowerSetpoint.set(index, activePowerSetpoint.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        // Detach converter stations
        converterStation1.setHvdcLine(null);
        converterStation2.setHvdcLine(null);
        converterStation1 = null;
        converterStation2 = null;

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }
}
