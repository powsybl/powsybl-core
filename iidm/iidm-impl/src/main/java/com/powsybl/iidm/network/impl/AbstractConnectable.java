/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.powsybl.iidm.network.TopologyKind.BUS_BREAKER;
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
            terminal.removeAsRegulationPoint();
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.detach(terminal);
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
        voltageLevel.attach(terminalExt, false);

        // then we can detach the old terminal, as we now know that the new terminal is valid
        oldTerminal.getVoltageLevel().detach(oldTerminal);

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
        // ReportNode
        ReportNode reportNode = this.getNetwork().getReportNodeContext().getReportNode();

        // Booleans
        boolean isAlreadyConnected = true;
        boolean isNowConnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForDisconnection = new HashSet<>();

        // We try to connect each terminal
        for (TerminalExt terminal : getTerminals()) {
            // Check if the terminal is already connected
            if (terminal.isConnected()) {
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyConnectedTerminal", "A terminal of connectable ${connectable} is already connected.")
                    .withUntypedValue("connectable", this.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            } else {
                isAlreadyConnected = false;
            }

            // If it's a node-breaker terminal, the switches to connect are added to a set
            if (terminal.getVoltageLevel() instanceof NodeBreakerVoltageLevel nodeBreakerVoltageLevel) {
                isNowConnected = nodeBreakerVoltageLevel.getConnectingSwitches(terminal, isTypeSwitchToOperate, switchForDisconnection);
            }
            // If it's a bus-breaker terminal, there is nothing to do

            // Exit if the terminal cannot be connected
            if (!isNowConnected) {
                return false;
            }
        }

        // Exit if the connectable is already fully connected
        if (isAlreadyConnected) {
            return false;
        }

        // Connect all bus-breaker terminals
        for (TerminalExt terminal : getTerminals()) {
            if (!terminal.isConnected()
                && terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // At this point, isNowConnected should always stay true but let's be careful
                isNowConnected = isNowConnected && terminal.connect(isTypeSwitchToOperate);
            }
        }

        // Disconnect all switches on node-breaker terminals
        switchForDisconnection.forEach(sw -> sw.setOpen(false));
        return isNowConnected;
    }

    @Override
    public boolean disconnect() {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        // ReportNode
        ReportNode reportNode = this.getNetwork().getReportNodeContext().getReportNode();

        // Booleans
        boolean isAlreadyDisconnected = true;
        boolean isNowDisconnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForDisconnection = new HashSet<>();

        // We try to disconnect each terminal
        for (TerminalExt terminal : getTerminals()) {
            // Check if the terminal is already disconnected
            if (!terminal.isConnected()) {
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyDisconnectedTerminal", "A terminal of connectable ${connectable} is already disconnected.")
                    .withUntypedValue("connectable", this.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            }
            // The terminal is connected
            isAlreadyDisconnected = false;

            // If it's a node-breaker terminal, the switches to disconnect are added to a set
            if (terminal.getVoltageLevel() instanceof NodeBreakerVoltageLevel nodeBreakerVoltageLevel
                && !nodeBreakerVoltageLevel.getDisconnectingSwitches(terminal, isSwitchOpenable, switchForDisconnection)) {
                // Exit if the terminal cannot be disconnected
                return false;
            }
            // If it's a bus-breaker terminal, there is nothing to do
        }

        // Exit if the connectable is already fully disconnected
        if (isAlreadyDisconnected) {
            return false;
        }

        // Disconnect all bus-breaker terminals
        for (TerminalExt terminal : getTerminals()) {
            if (terminal.isConnected()
                && terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // At this point, isNowDisconnected should always stay true but let's be careful
                isNowDisconnected = isNowDisconnected && terminal.disconnect(isSwitchOpenable);
            }
        }
        // Disconnect all switches on node-breaker terminals
        switchForDisconnection.forEach(sw -> sw.setOpen(true));
        return isNowDisconnected;
    }
}
