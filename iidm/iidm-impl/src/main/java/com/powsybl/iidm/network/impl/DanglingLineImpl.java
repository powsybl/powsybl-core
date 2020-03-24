/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineImpl extends AbstractConnectable<DanglingLine> implements DanglingLine, CurrentLimitsOwner<Void> {

    private final Ref<? extends VariantManagerHolder> network;

    private double r;

    private double x;

    private double g;

    private double b;

    private final String ucteXnodeCode;

    private CurrentLimitsImpl limits;

    // attributes depending on the variant

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    DanglingLineImpl(Ref<? extends VariantManagerHolder> network, String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode) {
        super(id, name, fictitious);
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.p0 = new TDoubleArrayList(variantArraySize);
        this.q0 = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.p0.add(p0);
            this.q0.add(q0);
        }
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.ucteXnodeCode = ucteXnodeCode;
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
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        p0.ensureCapacity(p0.size() + number);
        q0.ensureCapacity(q0.size() + number);
        for (int i = 0; i < number; i++) {
            p0.add(p0.get(sourceIndex));
            q0.add(q0.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
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
        }
    }

}
