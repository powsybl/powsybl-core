/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ConfiguredBusImpl extends AbstractBus implements ConfiguredBus {

    private final Ref<NetworkImpl> network;

    private final ArrayList<List<BusTerminal>> terminals;

    private final TDoubleArrayList v;

    private final TDoubleArrayList angle;

    private final TDoubleArrayList fictitiousP0;

    private final TDoubleArrayList fictitiousQ0;

    private final TIntArrayList connectedComponentNumber;

    private final TIntArrayList synchronousComponentNumber;

    ConfiguredBusImpl(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel) {
        super(id, name, fictitious, voltageLevel);
        network = voltageLevel.getNetworkRef();
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        terminals = new ArrayList<>(variantArraySize);
        v = new TDoubleArrayList(variantArraySize);
        angle = new TDoubleArrayList(variantArraySize);
        fictitiousP0 = new TDoubleArrayList(variantArraySize);
        fictitiousQ0 = new TDoubleArrayList(variantArraySize);
        connectedComponentNumber = new TIntArrayList(variantArraySize);
        synchronousComponentNumber = new TIntArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            terminals.add(new ArrayList<>());
            v.add(Double.NaN);
            angle.add(Double.NaN);
            fictitiousP0.add(0.0);
            fictitiousQ0.add(0.0);
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
        return terminals.get(network.get().getVariantIndex()).size();
    }

    @Override
    public List<BusTerminal> getTerminals() {
        return terminals.get(network.get().getVariantIndex());
    }

    @Override
    public void addTerminal(BusTerminal t) {
        terminals.get(network.get().getVariantIndex()).add(t);
    }

    @Override
    public void removeTerminal(BusTerminal t) {
        if (!terminals.get(network.get().getVariantIndex()).remove(t)) {
            throw new IllegalStateException("Terminal " + t + " not found");
        }
    }

    protected <S, T extends S> void notifyUpdate(String attribute, String variantId, S oldValue, T newValue) {
        network.get().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public double getV() {
        return v.get(network.get().getVariantIndex());
    }

    @Override
    public BusExt setV(double v) {
        if (v < 0) {
            throw new ValidationException(this, "voltage cannot be < 0");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.v.set(variantIndex, v);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("v", variantId, oldValue, v);
        return this;
    }

    @Override
    public double getAngle() {
        return angle.get(network.get().getVariantIndex());
    }

    @Override
    public BusExt setAngle(double angle) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.angle.set(variantIndex, angle);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("angle", variantId, oldValue, angle);
        return this;
    }

    @Override
    public double getFictitiousP0() {
        return fictitiousP0.get(network.get().getVariantIndex());
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        if (Double.isNaN(p0)) {
            throw new ValidationException(this, "undefined value cannot be set as fictitious p0");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.fictitiousP0.set(variantIndex, p0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("fictitiousP0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        return fictitiousQ0.get(network.get().getVariantIndex());
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        if (Double.isNaN(q0)) {
            throw new ValidationException(this, "undefined value cannot be set as fictitious q0");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.fictitiousQ0.set(variantIndex, q0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("fictitiousQ0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.connectedComponentNumber.set(variantIndex, connectedComponentNumber);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("connectedComponentNumber", variantId, oldValue, connectedComponentNumber);
    }

    @Override
    public Component getConnectedComponent() {
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return ccm.getComponent(connectedComponentNumber.get(network.get().getVariantIndex()));
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.synchronousComponentNumber.set(variantIndex, componentNumber);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("synchronousComponentNumber", variantId, oldValue, componentNumber);
    }

    @Override
    public Component getSynchronousComponent() {
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return scm.getComponent(synchronousComponentNumber.get(network.get().getVariantIndex()));
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        terminals.ensureCapacity(terminals.size() + number);
        v.ensureCapacity(v.size() + number);
        angle.ensureCapacity(angle.size() + number);
        fictitiousP0.ensureCapacity(fictitiousP0.size() + number);
        fictitiousQ0.ensureCapacity(fictitiousQ0.size() + number);
        connectedComponentNumber.ensureCapacity(connectedComponentNumber.size() + number);
        synchronousComponentNumber.ensureCapacity(synchronousComponentNumber.size() + number);
        for (int i = 0; i < number; i++) {
            terminals.add(new ArrayList<>(terminals.get(sourceIndex)));
            v.add(v.get(sourceIndex));
            angle.add(angle.get(sourceIndex));
            fictitiousP0.add(fictitiousP0.get(sourceIndex));
            fictitiousQ0.add(fictitiousQ0.get(sourceIndex));
            connectedComponentNumber.add(connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.add(synchronousComponentNumber.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        for (int i = 0; i < number; i++) {
            terminals.remove(terminals.size() - 1);
        }
        v.remove(v.size() - number, number);
        angle.remove(angle.size() - number, number);
        fictitiousP0.remove(fictitiousP0.size() - number, number);
        fictitiousQ0.remove(fictitiousQ0.size() - number, number);
        connectedComponentNumber.remove(connectedComponentNumber.size() - number, number);
        synchronousComponentNumber.remove(synchronousComponentNumber.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        terminals.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (int index : indexes) {
            terminals.set(index, new ArrayList<>(terminals.get(sourceIndex)));
            v.set(index, v.get(sourceIndex));
            angle.set(index, angle.get(sourceIndex));
            fictitiousP0.set(index, fictitiousP0.get(sourceIndex));
            fictitiousQ0.set(index, fictitiousQ0.get(sourceIndex));
            connectedComponentNumber.set(index, connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.set(index, synchronousComponentNumber.get(sourceIndex));
        }
    }

}
