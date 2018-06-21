/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ConfiguredBusImpl extends AbstractBus implements ConfiguredBus, Stateful {

    private final Ref<NetworkImpl> network;

    private final ArrayList<List<BusTerminal>> terminals;

    private final TDoubleArrayList v;

    private final TDoubleArrayList angle;

    private final TIntArrayList connectedComponentNumber;

    private final TIntArrayList synchronousComponentNumber;

    ConfiguredBusImpl(String id, VoltageLevelExt voltageLevel) {
        super(id, voltageLevel);
        network = voltageLevel.getNetwork().getRef();
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        terminals = new ArrayList<>(stateArraySize);
        v = new TDoubleArrayList(stateArraySize);
        angle = new TDoubleArrayList(stateArraySize);
        connectedComponentNumber = new TIntArrayList(stateArraySize);
        synchronousComponentNumber = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            terminals.add(new ArrayList<>());
            v.add(Double.NaN);
            angle.add(Double.NaN);
            connectedComponentNumber.add(-1);
            synchronousComponentNumber.add(-1);
        }
    }

    @Override
    public int getConnectedTerminalCount() {
        return (int) getTerminals().stream().filter(BusTerminal::isConnected).count();
    }

    @Override
    public List<TerminalExt> getConnectedTerminals() {
        return getConnectedTerminalStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        return getTerminals().stream().filter(Terminal::isConnected).map(Function.identity());
    }

    @Override
    public int getTerminalCount() {
        return terminals.get(network.get().getStateIndex()).size();
    }

    @Override
    public List<BusTerminal> getTerminals() {
        return terminals.get(network.get().getStateIndex());
    }

    @Override
    public void addTerminal(BusTerminal t) {
        terminals.get(network.get().getStateIndex()).add(t);
    }

    @Override
    public void removeTerminal(BusTerminal t) {
        if (!terminals.get(network.get().getStateIndex()).remove(t)) {
            throw new IllegalStateException("Terminal " + t + " not found");
        }
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        network.get().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public double getV() {
        return v.get(network.get().getStateIndex());
    }

    @Override
    public BusExt setV(double v) {
        if (v < 0) {
            throw new ValidationException(this, "voltage cannot be < 0");
        }
        double oldValue = this.v.set(network.get().getStateIndex(), v);
        notifyUpdate("v", oldValue, v);
        return this;
    }

    @Override
    public double getAngle() {
        return angle.get(network.get().getStateIndex());
    }

    @Override
    public BusExt setAngle(double angle) {
        double oldValue = this.angle.set(network.get().getStateIndex(), angle);
        notifyUpdate("angle", oldValue, angle);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        this.connectedComponentNumber.set(network.get().getStateIndex(), connectedComponentNumber);
    }

    @Override
    public Component getConnectedComponent() {
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return ccm.getComponent(connectedComponentNumber.get(network.get().getStateIndex()));
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        this.synchronousComponentNumber.set(network.get().getStateIndex(), componentNumber);
    }

    @Override
    public Component getSynchronousComponent() {
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return scm.getComponent(synchronousComponentNumber.get(network.get().getStateIndex()));
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        terminals.ensureCapacity(terminals.size() + number);
        v.ensureCapacity(v.size() + number);
        angle.ensureCapacity(angle.size() + number);
        connectedComponentNumber.ensureCapacity(connectedComponentNumber.size() + number);
        synchronousComponentNumber.ensureCapacity(synchronousComponentNumber.size() + number);
        for (int i = 0; i < number; i++) {
            terminals.add(new ArrayList<>(terminals.get(sourceIndex)));
            v.add(v.get(sourceIndex));
            angle.add(angle.get(sourceIndex));
            connectedComponentNumber.add(connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.add(synchronousComponentNumber.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        for (int i = 0; i < number; i++) {
            terminals.remove(terminals.size() - 1);
        }
        v.remove(v.size() - number, number);
        angle.remove(angle.size() - number, number);
        connectedComponentNumber.remove(connectedComponentNumber.size() - number, number);
        synchronousComponentNumber.remove(synchronousComponentNumber.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        terminals.set(index, null);
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            terminals.set(index, new ArrayList<>(terminals.get(sourceIndex)));
            v.set(index, v.get(sourceIndex));
            angle.set(index, angle.get(sourceIndex));
            connectedComponentNumber.set(index, connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.set(index, synchronousComponentNumber.get(sourceIndex));
        }
    }

}
