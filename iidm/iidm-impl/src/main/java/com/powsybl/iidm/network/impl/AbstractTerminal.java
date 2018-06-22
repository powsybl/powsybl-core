/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTerminal implements TerminalExt {

    protected final Ref<? extends MultiStateObject> network;

    protected AbstractConnectable connectable;

    protected VoltageLevelExt voltageLevel;

    protected int num = -1;

    // attributes depending on the state

    protected final TDoubleArrayList p;

    protected final TDoubleArrayList q;

    AbstractTerminal(Ref<? extends MultiStateObject> network) {
        this.network = network;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        p = new TDoubleArrayList(stateArraySize);
        q = new TDoubleArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            p.add(Double.NaN);
            q.add(Double.NaN);
        }
    }

    @Override
    public AbstractConnectable getConnectable() {
        return connectable;
    }

    @Override
    public void setConnectable(AbstractConnectable connectable) {
        this.connectable = connectable;
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public void setVoltageLevel(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public double getP() {
        return p.get(network.get().getStateIndex());
    }

    @Override
    public Terminal setP(double p) {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set active power on a busbar section");
        }
        if (!Double.isNaN(p) && connectable.getType() == ConnectableType.SHUNT_COMPENSATOR) {
            throw new ValidationException(connectable, "cannot set active power on a shunt compensator");
        }
        double oldValue = this.p.set(network.get().getStateIndex(), p);
        getConnectable().notifyUpdate("p" + (num != -1 ? num : ""), oldValue, p);
        return this;
    }

    @Override
    public double getQ() {
        return q.get(network.get().getStateIndex());
    }

    @Override
    public Terminal setQ(double q) {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set reactive power on a busbar section");
        }
        double oldValue = this.q.set(network.get().getStateIndex(), q);
        getConnectable().notifyUpdate("q" + (num != -1 ? num : ""), oldValue, q);
        return this;
    }

    protected abstract double getV();

    @Override
    public double getI() {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            return 0;
        }
        int stateIndex = network.get().getStateIndex();
        return Math.hypot(p.get(stateIndex), q.get(stateIndex))
                / (Math.sqrt(3.) * getV() / 1000);
    }

    @Override
    public boolean connect() {
        return voltageLevel.connect(this);
    }

    @Override
    public boolean disconnect() {
        return voltageLevel.disconnect(this);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        p.ensureCapacity(p.size() + number);
        q.ensureCapacity(q.size() + number);
        for (int i = 0; i < number; i++) {
            p.add(p.get(sourceIndex));
            q.add(q.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        p.remove(p.size() - number, number);
        q.remove(q.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p.set(index, p.get(sourceIndex));
            q.set(index, q.get(sourceIndex));
        }
    }

}
