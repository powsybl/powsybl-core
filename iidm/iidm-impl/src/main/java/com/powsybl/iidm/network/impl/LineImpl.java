/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineImpl extends AbstractBranch<Line> implements Line {

    private double r;

    private double x;

    private double g1;

    private double b1;

    private double g2;

    private double b2;

    LineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, double r, double x, double g1, double b1, double g2, double b2) {
        super(network, id, name, fictitious);
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.LINE;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public LineImpl setR(double r) {
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
    public LineImpl setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG1() {
        return g1;
    }

    @Override
    public LineImpl setG1(double g1) {
        ValidationUtil.checkG1(this, g1);
        double oldValue = this.g1;
        this.g1 = g1;
        notifyUpdate("g1", oldValue, g1);
        return this;
    }

    @Override
    public double getB1() {
        return b1;
    }

    @Override
    public LineImpl setB1(double b1) {
        ValidationUtil.checkB1(this, b1);
        double oldValue = this.b1;
        this.b1 = b1;
        notifyUpdate("b1", oldValue, b1);
        return this;
    }

    @Override
    public double getG2() {
        return g2;
    }

    @Override
    public LineImpl setG2(double g2) {
        ValidationUtil.checkG2(this, g2);
        double oldValue = this.g2;
        this.g2 = g2;
        notifyUpdate("g2", oldValue, g2);
        return this;
    }

    @Override
    public double getB2() {
        return b2;
    }

    @Override
    public LineImpl setB2(double b2) {
        ValidationUtil.checkB2(this, b2);
        double oldValue = this.b2;
        this.b2 = b2;
        notifyUpdate("b2", oldValue, b2);
        return this;
    }

    @Override
    public boolean isTieLine() {
        return false;
    }

    @Override
    public LineImpl move1(int node, VoltageLevel voltageLevel) {
        move(node, voltageLevel, 1);
        return this;
    }

    @Override
    public LineImpl move2(int node, VoltageLevel voltageLevel) {
        move(node, voltageLevel, 2);
        return this;
    }

    private void move(int node, VoltageLevel voltageLevel, int side) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER
                || getTerminals().stream().anyMatch(t -> t.getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER)) {
            throw new ValidationException(this, String.format("Inconsistent topology kind for terminals of Line %s. Use move1(Bus, boolean)," +
                    " move2(Bus, boolean) or move(Bus, boolean, Bus, boolean)", id));
        }
        TerminalExt oldTerminal = terminals.get(side - 1);
        move(side, oldTerminal, new TerminalBuilder(getNetwork().getRef(), this)
                .setNode(node)
                .build(), (VoltageLevelExt) voltageLevel);
        notifyUpdate("terminal" + side, String.format("node %d, Voltage level %s",
                oldTerminal.getNodeBreakerView().getNode(), oldTerminal.getVoltageLevel().getId()),
                String.format("node %d, Voltage level %s", node, voltageLevel.getId()));
    }

    @Override
    public LineImpl move(int node1, VoltageLevel voltageLevel1, int node2, VoltageLevel voltageLevel2) {
        move1(node1, voltageLevel1);
        move2(node2, voltageLevel2);
        return this;
    }

    @Override
    public LineImpl move1(Bus bus, boolean connected) {
        move(bus, connected, 1);
        return this;
    }

    @Override
    public LineImpl move2(Bus bus, boolean connected) {
        move(bus, connected, 2);
        return this;
    }

    private void move(Bus bus, boolean connected, int side) {
        VoltageLevelExt voltageLevelExt = (VoltageLevelExt) bus.getVoltageLevel();
        if (voltageLevelExt.getTopologyKind() != TopologyKind.BUS_BREAKER
                || getTerminals().stream().anyMatch(t -> t.getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER)) {
            throw new ValidationException(this, String.format("Inconsistent topology kind for terminals of Line %s. Use move1(Bus, boolean)," +
                    " move2(Bus, boolean) or move(Bus, boolean, Bus, boolean)", id));
        }
        TerminalExt oldTerminal = terminals.get(side - 1);
        move(side, oldTerminal, new TerminalBuilder(getNetwork().getRef(), this)
                .setBus(connected ? bus.getId() : null)
                .setConnectableBus(bus.getId())
                .build(), voltageLevelExt);
        notifyUpdate("terminal" + side, String.format("bus %s, %s",
                oldTerminal.getBusBreakerView().getConnectableBus().getId(), oldTerminal.getBusBreakerView().getBus() != null ? "connected" : "disconnected"),
                String.format("bus %s, %s", bus.getId(), connected ? "connected" : "disconnected"));
    }

    @Override
    public LineImpl move(Bus bus1, boolean connected1, Bus bus2, boolean connected2) {
        move1(bus1, connected1);
        move2(bus2, connected2);
        return this;
    }

    private void move(int side, TerminalExt oldTerminal, TerminalExt terminal, VoltageLevelExt voltageLevelExt) {
        VoltageLevelExt oldVoltageLevelExt = oldTerminal.getVoltageLevel();
        oldVoltageLevelExt.detach(oldTerminal);
        terminals.remove(side - 1);
        terminals.add(side - 1, terminal);
        terminal.setConnectable(this);
        voltageLevelExt.attach(terminal, false);
    }

    @Override
    protected String getTypeDescription() {
        return "AC line";
    }

}
