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
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineImpl extends AbstractConnectable<DanglingLine> implements DanglingLine, CurrentLimitsOwner<Void> {

    private final Ref<? extends MultiStateObject> network;

    private double r;

    private double x;

    private double g;

    private double b;

    private String ucteXnodeCode;

    private CurrentLimitsImpl limits;

    // attributes depending on the state

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    DanglingLineImpl(Ref<? extends MultiStateObject> network, String id, String name, double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode) {
        super(id, name);
        this.network = network;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.p0 = new TDoubleArrayList(stateArraySize);
        this.q0 = new TDoubleArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
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
        return p0.get(network.get().getStateIndex());
    }

    @Override
    public DanglingLineImpl setP0(double p0) {
        ValidationUtil.checkP0(this, p0);
        double oldValue = this.p0.set(network.get().getStateIndex(), p0);
        notifyUpdate("p0", oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0.get(network.get().getStateIndex());
    }

    @Override
    public DanglingLineImpl setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0);
        double oldValue = this.q0.set(network.get().getStateIndex(), q0);
        notifyUpdate("q0", oldValue, q0);
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
        this.limits = limits;
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
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        p0.ensureCapacity(p0.size() + number);
        q0.ensureCapacity(q0.size() + number);
        for (int i = 0; i < number; i++) {
            p0.add(p0.get(sourceIndex));
            q0.add(q0.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            p0.set(index, p0.get(sourceIndex));
            q0.set(index, q0.get(sourceIndex));
        }
    }

}
