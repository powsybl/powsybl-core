/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CalculatedBusImpl extends AbstractBus implements CalculatedBus {

    private boolean valid = true;

    private final List<NodeTerminal> terminals;

    CalculatedBusImpl(String id, VoltageLevelExt voltageLevel, List<NodeTerminal> terminals) {
        super(id, voltageLevel);
//        if (terminals.isEmpty()) {
//            throw new IllegalArgumentException("Calculated bus without any terminals");
//        }
        this.terminals = terminals;
    }

    private void checkValidity() {
        if (!valid) {
            throw new ITeslaException("Bus has been invalidated");
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
    public Stream<TerminalExt> getConnectedTerminalsStream() {
        checkValidity();
        return terminals.stream().map(t -> t);
    }

    @Override
    public Collection<TerminalExt> getTerminals() {
        checkValidity();
        return Collections.unmodifiableCollection(terminals);
    }

    @Override
    public BusExt setV(float v) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setV(v);
        }
        return this;
    }

    @Override
    public float getV() {
        checkValidity();
        if (terminals.isEmpty()) return Float.NaN;
        return terminals.get(0).getV();
    }

    @Override
    public BusExt setAngle(float angle) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setAngle(angle);
        }
        return this;
    }

    @Override
    public float getAngle() {
        checkValidity();
        if (terminals.isEmpty()) return Float.NaN;
        return terminals.get(0).getAngle();
    }

    @Override
    public float getP() {
        checkValidity();
        return super.getP();
    }

    @Override
    public float getQ() {
        checkValidity();
        return super.getQ();
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidity();
        for (NodeTerminal terminal : terminals) {
            terminal.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    @Override
    public ConnectedComponent getConnectedComponent() {
        checkValidity();
        NetworkImpl.ConnectedComponentsManager ccm = voltageLevel.getNetwork().getConnectedComponentsManager();
        ccm.update();
        return ccm.getConnectedComponent(terminals.get(0).getConnectedComponentNumber());
    }

    @Override
    public Iterable<Line> getLines() {
        checkValidity();
        return super.getLines();
    }

    @Override
    public Stream<Line> getLinesStream() {
        checkValidity();
        return super.getLinesStream();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingTransformers() {
        checkValidity();
        return super.getTwoWindingTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingTransformersStream() {
        checkValidity();
        return super.getTwoWindingTransformersStream();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingTransformers() {
        checkValidity();
        return super.getThreeWindingTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingTransformersStream() {
        checkValidity();
        return super.getThreeWindingTransformersStream();
    }

    @Override
    public Iterable<Load> getLoads() {
        checkValidity();
        return super.getLoads();
    }

    @Override
    public Stream<Load> getLoadsStream() {
        checkValidity();
        return super.getLoadsStream();
    }

    @Override
    public Iterable<ShuntCompensator> getShunts() {
        checkValidity();
        return super.getShunts();
    }

    @Override
    public Stream<ShuntCompensator> getShuntsStream() {
        checkValidity();
        return super.getShuntsStream();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        checkValidity();
        return super.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorsStream() {
        checkValidity();
        return super.getGeneratorsStream();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        checkValidity();
        return super.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLinesStream() {
        checkValidity();
        return super.getDanglingLinesStream();
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        checkValidity();
        return super.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorsStream() {
        checkValidity();
        return super.getStaticVarCompensatorsStream();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        checkValidity();
        return super.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationsStream() {
        checkValidity();
        return super.getLccConverterStationsStream();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        checkValidity();
        return super.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationsStream() {
        checkValidity();
        return super.getVscConverterStationsStream();
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
