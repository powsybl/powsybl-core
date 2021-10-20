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
    public void remove(boolean cleanDanglingSwitches) {
        NetworkImpl network = getNetwork();
        network.getIndex().remove(this);
        for (TerminalExt terminal : terminals) {
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.detach(terminal, cleanDanglingSwitches);
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

    protected void move(TerminalExt oldTerminal, String oldConnectionInfo, int node, String voltageLevelId) {
        VoltageLevelExt voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new PowsyblException("Voltage level '" + voltageLevelId + "' not found");
        }

        // check bus topology
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            String msg = String.format(
                    "Trying to move connectable %s to node %d of voltage level %s, which is a bus breaker voltage level",
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

    protected void move(TerminalExt oldTerminal, String oldConnectionInfo, String busId, boolean connected) {
        Bus bus = getNetwork().getBusBreakerView().getBus(busId);
        if (bus == null) {
            throw new PowsyblException("Bus '" + busId + "' not found");
        }

        // check bus topology
        if (bus.getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER) {
            throw new PowsyblException(String.format(
                    "Trying to move connectable %s to bus %s of voltage level %s, which is a node breaker voltage level",
                    getId(), bus.getId(), bus.getVoltageLevel().getId()));
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
        // first, attach new terminal to connectable and to voltage level of destination, to ensure that the new terminal is valid
        terminalExt.setConnectable(this);
        voltageLevel.attach(terminalExt, false);

        // then we can detach the old terminal, as we now know that the new terminal is valid
        oldTerminal.getVoltageLevel().detach(oldTerminal, false);

        // replace the old terminal by the new terminal in the connectable
        int iSide = terminals.indexOf(oldTerminal);
        terminals.set(iSide, terminalExt);

        notifyUpdate("terminal" + (iSide + 1), oldConnectionInfo, terminalExt.getConnectionInfo());
    }
}
