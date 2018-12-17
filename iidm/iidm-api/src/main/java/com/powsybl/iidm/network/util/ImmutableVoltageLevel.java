/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImmutableVoltageLevel extends AbstractImmutableIdentifiable<VoltageLevel> implements VoltageLevel {

    public ImmutableVoltageLevel(VoltageLevel identifiable) {
        super(identifiable);
    }

    public static ImmutableVoltageLevel ofNullable(VoltageLevel identifiable) {
        return identifiable == null ? null : new ImmutableVoltageLevel(identifiable);
    }

    @Override
    public Substation getSubstation() {
        return new ImmutableSubstation(identifiable.getSubstation());
    }

    @Override
    public double getNominalV() {
        return identifiable.getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(double nominalV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getLowVoltageLimit() {
        return identifiable.getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getHighVoltageLimit() {
        return identifiable.getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        return identifiable.getConnectable(id, aClass);
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return identifiable.getConnectables(clazz);
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return identifiable.getConnectableStream(clazz);
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return identifiable.getConnectableCount(clazz);
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return identifiable.getConnectables();
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return identifiable.getConnectableStream();
    }

    @Override
    public int getConnectableCount() {
        return identifiable.getConnectableCount();
    }

    @Override
    public GeneratorAdder newGenerator() {
        return identifiable.newGenerator();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return identifiable.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream();
    }

    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    @Override
    public LoadAdder newLoad() {
        return identifiable.newLoad();
    }

    @Override
    public Iterable<Load> getLoads() {
        return identifiable.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream();
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return identifiable.getSwitches();
    }

    @Override
    public int getSwitchCount() {
        return identifiable.getSwitchCount();
    }

    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return identifiable.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream();
    }

    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return identifiable.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream();
    }

    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return identifiable.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return identifiable.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream();
    }

    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return identifiable.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream();
    }

    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        identifiable.visitEquipments(visitor);
    }

    @Override
    public TopologyKind getTopologyKind() {
        return identifiable.getTopologyKind();
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return identifiable.getNodeBreakerView();
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return identifiable.getBusBreakerView();
    }

    @Override
    public BusView getBusView() {
        return identifiable.getBusView();
    }

    @Override
    public void printTopology() {
        identifiable.printTopology();
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        identifiable.printTopology(out, dict);
    }

    @Override
    public void exportTopology(String filename) throws IOException {
        identifiable.exportTopology(filename);
    }

    @Override
    public void exportTopology(OutputStream outputStream) throws IOException {
        identifiable.exportTopology(outputStream);
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
