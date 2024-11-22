/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.powsybl.iidm.network.TopologyKind.NODE_BREAKER;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractConnectable<I extends Connectable<I>> extends AbstractIdentifiable<I> implements Connectable<I>, MultiVariantObject {

    protected final List<TerminalExt> terminals = new ArrayList<>();
    private final Ref<NetworkImpl> networkRef;
    protected boolean removed = false;

    AbstractConnectable(Ref<NetworkImpl> ref, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
    }

    void addTerminal(TerminalExt terminal) {
        terminals.add(terminal);
        terminal.setConnectable(this);
    }

    public List<TerminalExt> getTerminals() {
        return terminals;
    }

    @Override
    public NetworkExt getParentNetwork() {
        // the parent network is the network that contains all terminals of the connectable.
        List<NetworkExt> subnetworks = terminals.stream().map(t -> t.getVoltageLevel().getParentNetwork()).distinct().toList();
        if (subnetworks.size() == 1) {
            return subnetworks.get(0);
        }
        return getNetwork();
    }

    @Override
    public NetworkImpl getNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access network of removed equipment " + id);
        }
        return networkRef.get();
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);
        for (TerminalExt terminal : terminals) {
            terminal.getDependentContainer().notifyDependentOfRemoval();
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.getTopologyModel().detach(terminal);
        }

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
        terminals.forEach(TerminalExt::remove);
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

    protected void move(TerminalExt oldTerminal, TopologyPoint oldTopologyPoint, int node, String voltageLevelId) {
        VoltageLevelExt voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new PowsyblException("Voltage level '" + voltageLevelId + "' not found");
        }

        // check bus topology
        if (voltageLevel.getTopologyKind() != NODE_BREAKER) {
            String msg = String.format(
                    "Trying to move connectable %s to node %d of voltage level %s, which is a bus breaker voltage level",
                    getId(), node, voltageLevel.getId());
            throw new PowsyblException(msg);
        }

        // create the new terminal and attach it to the given voltage level and to the connectable
        TerminalExt terminalExt = new TerminalBuilder(voltageLevel.getNetworkRef(), this, oldTerminal.getSide())
                .setNode(node)
                .build();

        // detach the terminal from its previous voltage level
        attachTerminal(oldTerminal, oldTopologyPoint, voltageLevel, terminalExt);
    }

    protected void move(TerminalExt oldTerminal, TopologyPoint oldTopologyPoint, String busId, boolean connected) {
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
        TerminalExt terminalExt = new TerminalBuilder(((VoltageLevelExt) bus.getVoltageLevel()).getNetworkRef(), this, oldTerminal.getSide())
                .setBus(connected ? bus.getId() : null)
                .setConnectableBus(bus.getId())
                .build();

        // detach the terminal from its previous voltage level
        attachTerminal(oldTerminal, oldTopologyPoint, (VoltageLevelExt) bus.getVoltageLevel(), terminalExt);
    }

    private void attachTerminal(TerminalExt oldTerminal, TopologyPoint oldTopologyPoint, VoltageLevelExt voltageLevel, TerminalExt terminalExt) {
        // first, attach new terminal to connectable and to voltage level of destination, to ensure that the new terminal is valid
        terminalExt.setConnectable(this);
        voltageLevel.getTopologyModel().attach(terminalExt, false);

        // then we can detach the old terminal, as we now know that the new terminal is valid
        oldTerminal.getVoltageLevel().getTopologyModel().detach(oldTerminal);

        // replace the old terminal by the new terminal in the connectable
        int iSide = terminals.indexOf(oldTerminal);
        terminals.set(iSide, terminalExt);

        notifyUpdate("terminal" + (iSide + 1), oldTopologyPoint, terminalExt.getTopologyPoint());
    }

    @Override
    public boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return connect(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side) {

        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminals(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnect() {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return disconnect(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminals(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    public List<TerminalExt> getTerminals(ThreeSides side) {
        if (side == null) {
            return terminals;
        } else {
            return terminals.stream().filter(terminal -> terminal.getSide().equals(side)).toList();
        }
    }
}
