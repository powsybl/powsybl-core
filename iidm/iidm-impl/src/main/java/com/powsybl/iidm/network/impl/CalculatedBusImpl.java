/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CalculatedBusImpl extends AbstractBus implements CalculatedBus {

    private boolean valid = true;

    private final List<NodeTerminal> terminals;
    private final int[] nodes;
    private final List<Switch> switchesToTerminalRef;

    private NodeTerminal terminalRef;

    CalculatedBusImpl(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel, TIntArrayList nodes, List<NodeTerminal> terminals) {
        super(id, name, fictitious, voltageLevel);
        this.terminals = Objects.requireNonNull(terminals);
        this.nodes = Objects.requireNonNull(nodes).toArray();
        this.switchesToTerminalRef = new ArrayList<>();
        this.terminalRef = findTerminal(nodes);
    }

    /**
     * Find a terminal to retrieve voltage, angle or connected and synchronous component numbers.
     * If the terminals list contains at least one element, we use the first terminal as reference.
     * Otherwise this method tries to find a terminal which does not belong to this bus, but to the same "electrical" bus
     * and therefore can be used as reference.
     *
     * @param nodes The nodes which belong to this bus
     * @return The first terminal of the {@code terminals} list, or a terminal which belongs to an equivalent "electrical" bus.
     */
    private NodeTerminal findTerminal(TIntArrayList nodes) {
        if (!terminals.isEmpty()) {
            return terminals.getFirst();
        }
        return (NodeTerminal) getEquivalentTerminal(voltageLevel, nodes.getQuick(0));
    }

    /**
     * Return a terminal for the specified node.
     * If a terminal is attached to the node, return this terminal. Otherwise, this method traverses the topology and return
     * the first equivalent terminal found.
     *
     * @param voltageLevel The voltage level to traverse
     * @param node The starting node
     * @return A terminal for the specified node or null.
     */
    public Terminal getEquivalentTerminal(VoltageLevel voltageLevel, int node) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + voltageLevel.getId() + " is not described in Node/Breaker topology");
        }
        switchesToTerminalRef.clear();

        Terminal[] equivalentTerminal = new Terminal[1];

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && sw.isOpen()) {
                return TraverseResult.TERMINATE_PATH;
            }
            if (sw != null && sw.isRetained()) {
                switchesToTerminalRef.add(sw);
            }
            Terminal t = voltageLevel.getNodeBreakerView().getTerminal(node2);
            if (t != null) {
                equivalentTerminal[0] = t;
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);

        return equivalentTerminal[0];
    }

    private void checkValidityAndUpdateTerminalRefOnSwitchOpen() {
        if (!valid) {
            throw new PowsyblException("Bus has been invalidated");
        }
        for (Switch sw : switchesToTerminalRef) {
            if (sw.isOpen()) {
                this.terminalRef = findTerminal(new TIntArrayList(nodes));
                break;
            }
        }
    }

    /**
     * To invalidate the bus when it is a result of calculation and the topology
     * of the substation is modified.
     */
    @Override
    public void invalidate() {
        valid = false;
        voltageLevel = null;
        terminals.clear();
        terminalRef = null;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getVoltageLevel();
    }

    @Override
    public int getConnectedTerminalCount() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return terminals.size();
    }

    @Override
    public Collection<TerminalExt> getConnectedTerminals() {
        return getTerminals();
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return terminals.stream().map(Function.identity());
    }

    @Override
    public Collection<TerminalExt> getTerminals() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return Collections.unmodifiableCollection(terminals);
    }

    @Override
    public BusExt setV(double v) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        for (NodeTerminal terminal : terminals) {
            terminal.setV(v);
        }
        return this;
    }

    @Override
    public double getV() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return terminalRef == null ? Double.NaN : terminalRef.getV();
    }

    @Override
    public BusExt setAngle(double angle) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        for (NodeTerminal terminal : terminals) {
            terminal.setAngle(angle);
        }
        return this;
    }

    @Override
    public double getAngle() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return terminalRef == null ? Double.NaN : terminalRef.getAngle();
    }

    @Override
    public double getP() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getP();
    }

    @Override
    public double getQ() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getQ();
    }

    private IntStream getCalculatedBusNodes() {
        return IntStream.of(nodes);
    }

    @Override
    public double getFictitiousP0() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousP0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousP0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousP0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousP0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousQ0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousQ0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousQ0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousQ0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")), q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        for (NodeTerminal terminal : terminals) {
            terminal.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return terminalRef == null ? null : ccm.getComponent(terminalRef.getConnectedComponentNumber());
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        for (NodeTerminal terminal : terminals) {
            terminal.setSynchronousComponentNumber(componentNumber);
        }
    }

    @Override
    public Component getSynchronousComponent() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return terminalRef == null ? null : scm.getComponent(terminalRef.getSynchronousComponentNumber());
    }

    @Override
    public Iterable<Line> getLines() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLineStream();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getTwoWindingsTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getTwoWindingsTransformerStream();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getThreeWindingsTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getThreeWindingsTransformerStream();
    }

    @Override
    public Iterable<Load> getLoads() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLoadStream();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getShuntCompensatorStream();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getGeneratorStream();
    }

    @Override
    public Iterable<BoundaryLine> getBoundaryLines(BoundaryLineFilter boundaryLineFilter) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getBoundaryLines(boundaryLineFilter);
    }

    @Override
    public Stream<BoundaryLine> getBoundaryLineStream(BoundaryLineFilter boundaryLineFilter) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getBoundaryLineStream(boundaryLineFilter);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getStaticVarCompensatorStream();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getLccConverterStationStream();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        return super.getVscConverterStationStream();
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        super.visitConnectedEquipments(visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        checkValidityAndUpdateTerminalRefOnSwitchOpen();
        super.visitConnectedOrConnectableEquipments(visitor);
    }
}
