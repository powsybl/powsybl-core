/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineImpl extends AbstractConnectable<DanglingLine> implements DanglingLine, CurrentLimitsOwner<Void>, ReactiveLimitsOwner {

    private final Ref<? extends VariantManagerHolder> network;

    private double r;

    private double x;

    private double g;

    private double b;

    private final String ucteXnodeCode;

    private CurrentLimitsImpl limits;

    private final ReactiveLimitsHolderImpl reactiveLimits;

    // attributes depending on the variant

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    private final TDoubleArrayList generatorTargetP;

    private final TDoubleArrayList generatorTargetQ;

    private final TBooleanArrayList generatorVoltageRegulationOn;

    private final TDoubleArrayList generatorTargetV;

    DanglingLineImpl(Ref<? extends VariantManagerHolder> network, String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b,
                     double generatorTargetP, double generatorTargetQ, boolean generatorVoltageRegulationOn, double generatorTargetV, String ucteXnodeCode) {
        super(id, name, fictitious);
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.p0 = new TDoubleArrayList(variantArraySize);
        this.q0 = new TDoubleArrayList(variantArraySize);
        this.generatorTargetP = new TDoubleArrayList(variantArraySize);
        this.generatorTargetQ = new TDoubleArrayList(variantArraySize);
        this.generatorVoltageRegulationOn = new TBooleanArrayList(variantArraySize);
        this.generatorTargetV = new TDoubleArrayList(variantArraySize);

        for (int i = 0; i < variantArraySize; i++) {
            this.p0.add(p0);
            this.q0.add(q0);
            this.generatorTargetP.add(generatorTargetP);
            this.generatorTargetQ.add(generatorTargetQ);
            this.generatorVoltageRegulationOn.add(generatorVoltageRegulationOn);
            this.generatorTargetV.add(generatorTargetV);
        }
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.ucteXnodeCode = ucteXnodeCode;
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));

    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.DANGLING_LINE;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return "Dangling line";
    }

    @Override
    public double getP0() {
        return p0.get(network.get().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setP0(double p0) {
        ValidationUtil.checkP0(this, p0);
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0.get(network.get().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0);
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public DanglingLineImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public DanglingLineImpl setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return g;
    }

    @Override
    public DanglingLineImpl setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = this.g;
        this.g = g;
        notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return b;
    }

    @Override
    public DanglingLineImpl setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public double getGeneratorTargetP() {
        return generatorTargetP.get(getNetwork().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setGeneratorTargetP(double generatorTargetP) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.generatorTargetP.set(variantIndex, generatorTargetP);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("generatorTargetP", variantId, oldValue, generatorTargetP);
        return this;
    }

    @Override
    public double getGeneratorTargetQ() {
        return generatorTargetQ.get(getNetwork().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setGeneratorTargetQ(double generatorTargetQ) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.generatorTargetQ.set(variantIndex, generatorTargetQ);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("generatorTargetQ", variantId, oldValue, generatorTargetQ);
        return this;
    }

    @Override
    public boolean isGeneratorVoltageRegulationOn() {
        return generatorVoltageRegulationOn.get(getNetwork().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setGeneratorVoltageRegulationOn(boolean generatorVoltageRegulationOn) {
        int variantIndex = getNetwork().getVariantIndex();
        boolean oldValue = this.generatorVoltageRegulationOn.get(variantIndex);
        this.generatorVoltageRegulationOn.set(variantIndex, generatorVoltageRegulationOn);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("generatorVoltageRegulationOn", variantId, oldValue, generatorVoltageRegulationOn);
        return this;
    }

    @Override
    public double getGeneratorTargetV() {
        return this.generatorTargetV.get(getNetwork().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setGeneratorTargetV(double generatorTargetV) {
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.generatorTargetV.set(variantIndex, generatorTargetV);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("generatorTargetV", variantId, oldValue, generatorTargetV);
        return this;
    }

    @Override
    public String getUcteXnodeCode() {
        return ucteXnodeCode;
    }

    @Override
    public void setCurrentLimits(Void side, CurrentLimitsImpl limits) {
        CurrentLimitsImpl oldValue = limits;
        this.limits = limits;
        notifyUpdate("currentlimits", oldValue, limits);
    }

    @Override
    public CurrentLimitsImpl getCurrentLimits() {
        return limits;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl<>(null, this);
    }

    @Override
    public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdderImpl newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    @Override
    public <RL extends ReactiveLimits> RL getReactiveLimits(Class<RL> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        p0.ensureCapacity(p0.size() + number);
        q0.ensureCapacity(q0.size() + number);
        generatorTargetP.ensureCapacity(generatorTargetP.size() + number);
        generatorTargetQ.ensureCapacity(generatorTargetQ.size() + number);
        generatorVoltageRegulationOn.ensureCapacity(generatorVoltageRegulationOn.size() + number);
        generatorTargetV.ensureCapacity(generatorTargetV.size() + number);
        for (int i = 0; i < number; i++) {
            p0.add(p0.get(sourceIndex));
            q0.add(q0.get(sourceIndex));
            generatorTargetP.add(generatorTargetP.get(sourceIndex));
            generatorTargetQ.add(generatorTargetQ.get(sourceIndex));
            generatorVoltageRegulationOn.add(generatorVoltageRegulationOn.get(sourceIndex));
            generatorTargetV.add(generatorTargetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
        generatorTargetP.remove(generatorTargetP.size() - number, number);
        generatorTargetQ.remove(generatorTargetQ.size() - number, number);
        generatorVoltageRegulationOn.remove(generatorVoltageRegulationOn.size() - number, number);
        generatorTargetV.remove(generatorTargetV.size() - number, number);
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
            p0.set(index, p0.get(sourceIndex));
            q0.set(index, q0.get(sourceIndex));
            generatorTargetP.set(index, generatorTargetP.get(sourceIndex));
            generatorTargetQ.set(index, generatorTargetQ.get(sourceIndex));
            generatorVoltageRegulationOn.set(index, generatorVoltageRegulationOn.get(sourceIndex));
            generatorTargetV.set(index, generatorTargetV.get(sourceIndex));
        }
    }

}
