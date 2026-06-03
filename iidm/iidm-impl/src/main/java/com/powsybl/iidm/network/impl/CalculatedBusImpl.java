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
import com.powsybl.iidm.network.util.Networks;
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
    private final List<Switch> openSwitches;

    private NodeTerminal terminalRef;

    CalculatedBusImpl(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel, TIntArrayList nodes, List<NodeTerminal> terminals) {
        super(id, name, fictitious, voltageLevel);
        this.terminals = Objects.requireNonNull(terminals);
        this.nodes = Objects.requireNonNull(nodes).toArray();
        this.switchesToTerminalRef = new ArrayList<>();
        this.openSwitches = new ArrayList<>();
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
        return (NodeTerminal) Networks.getEquivalentTerminal(voltageLevel, nodes.getQuick(0), switchesToTerminalRef, openSwitches);
    }

    private void checkValidityAndUpdateTerminalRefOnSwitchModification() {
        if (!valid) {
            throw new PowsyblException("Bus has been invalidated");
        }
        if (terminalRef != null) {
            // Check if a switch has been opened
            for (Switch sw : switchesToTerminalRef) {
                if (sw.isOpen()) {
                    this.terminalRef = findTerminal(new TIntArrayList(nodes));
                    return;
                }
            }
        } else {
            // Check if a switch has been closed
            for (Switch sw : openSwitches) {
                if (!sw.isOpen()) {
                    this.terminalRef = findTerminal(new TIntArrayList(nodes));
                    return;
                }
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
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getVoltageLevel();
    }

    @Override
    public int getConnectedTerminalCount() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return terminals.size();
    }

    @Override
    public Collection<TerminalExt> getConnectedTerminals() {
        return getTerminals();
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return terminals.stream().map(Function.identity());
    }

    @Override
    public Collection<TerminalExt> getTerminals() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return Collections.unmodifiableCollection(terminals);
    }

    @Override
    public BusExt setV(double v) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        for (NodeTerminal terminal : terminals) {
            terminal.setV(v);
        }
        return this;
    }

    @Override
    public double getV() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return terminalRef == null ? Double.NaN : terminalRef.getV();
    }

    @Override
    public BusExt setAngle(double angle) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        for (NodeTerminal terminal : terminals) {
            terminal.setAngle(angle);
        }
        return this;
    }

    @Override
    public double getAngle() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return terminalRef == null ? Double.NaN : terminalRef.getAngle();
    }

    @Override
    public double getP() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getP();
    }

    @Override
    public double getQ() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getQ();
    }

    private IntStream getCalculatedBusNodes() {
        return IntStream.of(nodes);
    }

    @Override
    public double getFictitiousP0() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousP0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousP0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousP0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousP0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousQ0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousQ0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousQ0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousQ0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")), q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        for (NodeTerminal terminal : terminals) {
            terminal.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return terminalRef == null ? null : ccm.getComponent(terminalRef.getConnectedComponentNumber());
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        for (NodeTerminal terminal : terminals) {
            terminal.setSynchronousComponentNumber(componentNumber);
        }
    }

    @Override
    public Component getSynchronousComponent() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return terminalRef == null ? null : scm.getComponent(terminalRef.getSynchronousComponentNumber());
    }

    @Override
    public Iterable<Line> getLines() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLineStream();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getTwoWindingsTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getTwoWindingsTransformerStream();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getThreeWindingsTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getThreeWindingsTransformerStream();
    }

    @Override
    public Iterable<Load> getLoads() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLoadStream();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getShuntCompensatorStream();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getGeneratorStream();
    }

    @Override
    public Iterable<BoundaryLine> getBoundaryLines(BoundaryLineFilter boundaryLineFilter) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getBoundaryLines(boundaryLineFilter);
    }

    @Override
    public Stream<BoundaryLine> getBoundaryLineStream(BoundaryLineFilter boundaryLineFilter) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getBoundaryLineStream(boundaryLineFilter);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getStaticVarCompensatorStream();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getLccConverterStationStream();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        return super.getVscConverterStationStream();
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        super.visitConnectedEquipments(visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        checkValidityAndUpdateTerminalRefOnSwitchModification();
        super.visitConnectedOrConnectableEquipments(visitor);
    }
}
