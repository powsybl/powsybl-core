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
    private List<TerminalExt> connectableTerminals = null;
    private final int[] nodes;
    private final NodeBreakerTopologyModel topologyModel;

    CalculatedBusImpl(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel, TIntArrayList nodes, List<NodeTerminal> terminals,
                      NodeBreakerTopologyModel topologyModel) {
        super(id, name, fictitious, voltageLevel);
        this.terminals = Objects.requireNonNull(terminals);
        this.nodes = Objects.requireNonNull(nodes).toArray();
        this.topologyModel = Objects.requireNonNull(topologyModel);
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
        connectableTerminals = null;
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
        checkValidity();
        return Collections.unmodifiableCollection(terminals);
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        checkValidity();
        return terminals.stream().map(Function.identity());
    }

    @Override
    public Collection<TerminalExt> getTerminals() {
        checkValidity();
        if (connectableTerminals == null) {
            connectableTerminals = buildConnectableTerminalsCache();
        }
        return Collections.unmodifiableCollection(connectableTerminals);
    }

    @Override
    public BusExt setV(double v) {
        checkValidity();
        for (int node : nodes) {
            topologyModel.setV(node, v);
        }
        return this;
    }

    @Override
    public double getV() {
        checkValidity();
        return topologyModel.getV(nodes[0]);
    }

    @Override
    public BusExt setAngle(double angle) {
        checkValidity();
        for (int node : nodes) {
            topologyModel.setAngle(node, angle);
        }
        return this;
    }

    @Override
    public double getAngle() {
        checkValidity();
        return topologyModel.getAngle(nodes[0]);
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

    private IntStream getCalculatedBusNodes() {
        return IntStream.of(nodes);
    }

    @Override
    public double getFictitiousP0() {
        checkValidity();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousP0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousP0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        checkValidity();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousP0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousP0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        checkValidity();
        if (!voltageLevel.getNodeBreakerView().hasFictitiousQ0()) {
            return 0.0;
        }
        return getCalculatedBusNodes()
                .mapToDouble(n -> voltageLevel.getNodeBreakerView().getFictitiousQ0(n))
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        checkValidity();
        getCalculatedBusNodes().forEach(n -> voltageLevel.getNodeBreakerView().setFictitiousQ0(n, 0.0));
        voltageLevel.getNodeBreakerView().setFictitiousQ0(getCalculatedBusNodes()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")), q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidity();
        for (int node : nodes) {
            topologyModel.setConnectedComponentNumber(node, connectedComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidity();
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return ccm.getComponent(topologyModel.getConnectedComponentNumber(nodes[0]));
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        checkValidity();
        for (int node : nodes) {
            topologyModel.setSynchronousComponentNumber(node, componentNumber);
        }
    }

    @Override
    public Component getSynchronousComponent() {
        checkValidity();
        NetworkImpl.SynchronousComponentsManager scm = voltageLevel.getNetwork().getSynchronousComponentsManager();
        scm.update();
        return scm.getComponent(topologyModel.getSynchronousComponentNumber(nodes[0]));
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
    public Iterable<BoundaryLine> getBoundaryLines(BoundaryLineFilter boundaryLineFilter) {
        checkValidity();
        return super.getBoundaryLines(boundaryLineFilter);
    }

    @Override
    public Stream<BoundaryLine> getBoundaryLineStream(BoundaryLineFilter boundaryLineFilter) {
        checkValidity();
        return super.getBoundaryLineStream(boundaryLineFilter);
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

    private List<TerminalExt> buildConnectableTerminalsCache() {
        List<TerminalExt> connectableTerminalsList = new ArrayList<>(terminals);
        int[] vlNodes = voltageLevel.getNodeBreakerView().getNodes();
        for (int n : vlNodes) {
            Terminal t = voltageLevel.getNodeBreakerView().getTerminal(n);
            if (t instanceof TerminalExt te) {
                try {
                    Bus connectableBus = te.getBusView().getConnectableBus();
                    if (this.equals(connectableBus) && !connectableTerminalsList.contains(te)) {
                        connectableTerminalsList.add(te);
                    }
                } catch (PowsyblException e) {
                    // ignore removed equipments or inaccessible buses
                }
            }
        }
        return connectableTerminalsList;
    }

    @Override
    public CalculatedBus setId(String id) {
        // Calculated bus id should not be updated
        throw new PowsyblException("Updating the id of a calculated bus is not supported.");
    }

    public int[] getNodes() {
        return nodes;
    }
}
