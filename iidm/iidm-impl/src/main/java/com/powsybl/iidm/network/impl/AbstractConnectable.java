/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractConnectable<I extends Connectable<I>> extends AbstractIdentifiable<I> implements Connectable<I>, MultiVariantObject {

    protected final List<TerminalExt> terminals = new ArrayList<>();
    private Ref<NetworkImpl> networkRef;

    AbstractConnectable(Ref<NetworkImpl> ref, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
    }

    public I setName(String name) {
        String oldValue = this.name;
        this.name = name;
        notifyUpdate("name", oldValue, name);
        return (I) this;
    }

    void addTerminal(TerminalExt terminal) {
        terminals.add(terminal);
        terminal.setConnectable(this);
    }

    public List<TerminalExt> getTerminals() {
        return terminals;
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getIndex().remove(this);
        for (TerminalExt terminal : terminals) {
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.detach(terminal);
        }
        network.getListeners().notifyRemoval(this);
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        for (TerminalExt t : terminals) {
            t.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        for (TerminalExt t : terminals) {
            t.reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        for (TerminalExt t : terminals) {
            t.deleteVariantArrayElement(index);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (TerminalExt t : terminals) {
            t.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

    protected void move(TerminalExt oldTerminal, String oldConnectionInfo, int node, VoltageLevelExt voltageLevel) {
        Objects.requireNonNull(voltageLevel);

        // check bus topology
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            String msg = String.format(
                    "Trying to move connectable %s to node %d of voltage level %s, which is not in node breaker topology",
                    getId(), node, voltageLevel.getId());
            throw new PowsyblException(msg);
        }

        // create the new terminal and attach it to the given voltage level and to the connectable
        TerminalExt terminalExt = new TerminalBuilder(getNetwork().getRef(), this)
                .setNode(node)
                .build();

        // detach the terminal from its previous voltage level
        attachTerminal(oldTerminal, oldConnectionInfo, voltageLevel, terminalExt);
    }

    protected void move(TerminalExt oldTerminal, String oldConnectionInfo, Bus bus, boolean connected) {
        Objects.requireNonNull(bus);

        // check bus topology
        if (bus.getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER) {
            throw new PowsyblException(String.format("Trying to move connectable %s to bus %s which is not in bus breaker topology",
                    getId(), bus.getId()));
        }

        // create the new terminal and attach it to the voltage level of the given bus and links it to the connectable
        TerminalExt terminalExt = new TerminalBuilder(getNetwork().getRef(), this)
                .setBus(connected ? bus.getId() : null)
                .setConnectableBus(bus.getId())
                .build();

        // detach the terminal from its previous voltage level
        attachTerminal(oldTerminal, oldConnectionInfo, (VoltageLevelExt) bus.getVoltageLevel(), terminalExt);
    }

    private void attachTerminal(TerminalExt oldTerminal, String oldConnectionInfo, VoltageLevelExt voltageLevel, TerminalExt terminalExt) {
        oldTerminal.getVoltageLevel().detach(oldTerminal);

        int iSide = terminals.indexOf(oldTerminal);
        terminals.set(iSide, terminalExt);
        terminalExt.setConnectable(this);
        voltageLevel.attach(terminalExt, false);

        notifyUpdate("terminal" + (iSide + 1), oldConnectionInfo, terminalExt.getConnectionInfo());
    }
}
