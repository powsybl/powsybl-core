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
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CalculatedBusImpl extends AbstractBus implements CalculatedBus {

    private boolean valid = true;

    private final List<NodeTerminal> terminals;
    private final Function<Terminal, Bus> getBusFromTerminal;

    private NodeTerminal terminalRef;

    CalculatedBusImpl(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel, TIntArrayList nodes, List<NodeTerminal> terminals, Function<Terminal, Bus> getBusFromTerminal) {
        super(id, name, fictitious, voltageLevel);
        this.terminals = Objects.requireNonNull(terminals);
        this.getBusFromTerminal = Objects.requireNonNull(getBusFromTerminal);
        this.terminalRef = findTerminal(voltageLevel, nodes, terminals);
    }

    /**
     * Find a terminal to retrieve voltage, angle or connected and synchronous component numbers.
     * If the terminals list contains at least one element, we use the first terminal as reference.
     * Otherwise this method tries to find a terminal which does not belong to this bus, but to the same "electrical" bus
     * and therefore can be used as reference.
     *
     * @param voltageLevel The {@literal VoltageLevel} instance to traverse
     * @param nodes The nodes which belong to this bus
     * @param terminals The terminals belong to this bus
     * @return The first terminal of the {@code terminals} list, or a terminal which belongs to an equivalent "electrical" bus.
     */
    private static NodeTerminal findTerminal(VoltageLevelExt voltageLevel, TIntArrayList nodes, List<NodeTerminal> terminals) {
        if (!terminals.isEmpty()) {
            return terminals.get(0);
        }
        return (NodeTerminal) Networks.getEquivalentTerminal(voltageLevel, nodes.getQuick(0));
    }

    private void checkValidity() {
        if (!valid) {
            throw new PowsyblException("Bus has been invalidated");
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
        checkValidity();
        return super.getVoltageLevel();
    }

    @Override
    public int getConnectedTerminalCount() {
        checkValidity();
        return terminals.size();
    }

    @Override
    public Collection<TerminalExt> getConnectedTerminals() {
        return getTerminals();
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        checkValidity();
        return terminals.stream().map(Function.identity());
    }

    @Override
    public Collection<TerminalExt> getTerminals() {
        checkValidity();
        return Collections.unmodifiableCollection(terminals);
    }

    @Override
    public BusExt setV(double v) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setV(v);
        }
        return this;
    }

    @Override
    public double getV() {
        checkValidity();
        return terminalRef == null ? Double.NaN : terminalRef.getV();
    }

    @Override
    public BusExt setAngle(double angle) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setAngle(angle);
        }
        return this;
    }

    @Override
    public double getAngle() {
        checkValidity();
        return terminalRef == null ? Double.NaN : terminalRef.getAngle();
    }

    @Override
    public double getP() {
        checkValidity();
        return super.getP();
    }

    @Override
    public double getQ() {
        checkValidity();
        return super.getQ();
    }

    @Override
    public double getFictitiousP0() {
        checkValidity();
        return Networks.getNodes(id, voltageLevel, getBusFromTerminal)
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousP0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        checkValidity();
        Networks.getNodes(id, voltageLevel, getBusFromTerminal).forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousP0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousP0(Networks.getNodes(id, voltageLevel, getBusFromTerminal)
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        checkValidity();
        return Networks.getNodes(id, voltageLevel, getBusFromTerminal)
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousQ0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        checkValidity();
        Networks.getNodes(id, voltageLevel, getBusFromTerminal).forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousQ0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousQ0(Networks.getNodes(id, voltageLevel, getBusFromTerminal)
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")), q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidity();
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return terminalRef == null ? null : ccm.getComponent(terminalRef.getConnectedComponentNumber());
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setSynchronousComponentNumber(componentNumber);
        }
    }

    @Override
    public Component getSynchronousComponent() {
        checkValidity();
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return terminalRef == null ? null : scm.getComponent(terminalRef.getSynchronousComponentNumber());
    }

    @Override
    public Iterable<Line> getLines() {
        checkValidity();
        return super.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        checkValidity();
        return super.getLineStream();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        checkValidity();
        return super.getTwoWindingsTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        checkValidity();
        return super.getTwoWindingsTransformerStream();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        checkValidity();
        return super.getThreeWindingsTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        checkValidity();
        return super.getThreeWindingsTransformerStream();
    }

    @Override
    public Iterable<Load> getLoads() {
        checkValidity();
        return super.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        checkValidity();
        return super.getLoadStream();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        checkValidity();
        return super.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        checkValidity();
        return super.getShuntCompensatorStream();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        checkValidity();
        return super.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        checkValidity();
        return super.getGeneratorStream();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        checkValidity();
        return super.getDanglingLines(danglingLineFilter);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        checkValidity();
        return super.getDanglingLineStream(danglingLineFilter);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        checkValidity();
        return super.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        checkValidity();
        return super.getStaticVarCompensatorStream();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        checkValidity();
        return super.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        checkValidity();
        return super.getLccConverterStationStream();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        checkValidity();
        return super.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        checkValidity();
        return super.getVscConverterStationStream();
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        checkValidity();
        super.visitConnectedEquipments(visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        checkValidity();
        super.visitConnectedOrConnectableEquipments(visitor);
    }
}
