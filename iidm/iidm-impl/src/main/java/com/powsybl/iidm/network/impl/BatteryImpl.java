/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryImpl extends AbstractConnectable<Battery> implements Battery, ReactiveLimitsOwner {

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private TDoubleArrayList p0;

    private TDoubleArrayList q0;

    private double minP;

    private double maxP;

    BatteryImpl(Ref<? extends VariantManagerHolder> ref, String id, String name, boolean fictitious, double p0, double q0, double minP, double maxP) {
        super(id, name, fictitious);
        this.minP = minP;
        this.maxP = maxP;
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));

        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.p0 = new TDoubleArrayList(variantArraySize);
        this.q0 = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.p0.add(p0);
            this.q0.add(q0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectableType getType() {
        return ConnectableType.BATTERY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeDescription() {
        return "Battery";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getP0() {
        return p0.get(getNetwork().getVariantIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setP0(double p0) {
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP, p0);
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getQ0() {
        return q0.get(getNetwork().getVariantIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0);
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinP() {
        return minP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP, getP0());
        double oldValue = this.minP;
        this.minP = minP;
        notifyUpdate("minP", oldValue, minP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaxP() {
        return maxP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP, getP0());
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        p0.ensureCapacity(p0.size() + number);
        q0.ensureCapacity(q0.size() + number);
        for (int i = 0; i < number; i++) {
            p0.add(p0.get(sourceIndex));
            q0.add(q0.get(sourceIndex));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            p0.set(index, p0.get(sourceIndex));
            q0.set(index, q0.get(sourceIndex));
        }
    }
}
