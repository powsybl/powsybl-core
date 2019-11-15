/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Stream;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdapter extends AbstractIdentifiableAdapter<VoltageLevel> implements VoltageLevel {

    public VoltageLevelAdapter(final VoltageLevel delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public SubstationAdapter getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public <T extends Connectable> T getConnectable(final String id, final Class<T> aClass) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(final Class<T> clazz) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(final Class<T> clazz) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <T extends Connectable> int getConnectableCount(final Class<T> clazz) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getConnectableCount() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdder newGenerator() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BatteryAdder newBattery() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Battery> getBatteries() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LoadAdder newLoad() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Load> getLoads() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Load> getLoadStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getDanglingLineCount() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewAdapter getNodeBreakerView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewAdapter getBusBreakerView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusViewAdapter getBusView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getSwitchCount() {
        return getDelegate().getSwitchCount();
    }

    @Override
    public int getGeneratorCount() {
        return getDelegate().getGeneratorCount();
    }

    @Override
    public int getBatteryCount() {
        return getDelegate().getBatteryCount();
    }

    @Override
    public int getLoadCount() {
        return getDelegate().getLoadCount();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return getDelegate().getStaticVarCompensatorCount();
    }

    @Override
    public int getVscConverterStationCount() {
        return getDelegate().getVscConverterStationCount();
    }

    @Override
    public ContainerType getContainerType() {
        return getDelegate().getContainerType();
    }

    @Override
    public double getNominalV() {
        return getDelegate().getNominalV();
    }

    @Override
    public VoltageLevelAdapter setNominalV(final double nominalV) {
        getDelegate().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return getDelegate().getLowVoltageLimit();
    }

    @Override
    public VoltageLevelAdapter setLowVoltageLimit(final double lowVoltageLimit) {
        getDelegate().setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return getDelegate().getHighVoltageLimit();
    }

    @Override
    public VoltageLevelAdapter setHighVoltageLimit(final double highVoltageLimit) {
        getDelegate().setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public int getShuntCompensatorCount() {
        return getDelegate().getShuntCompensatorCount();
    }

    @Override
    public int getLccConverterStationCount() {
        return getDelegate().getLccConverterStationCount();
    }

    @Override
    public void visitEquipments(final TopologyVisitor visitor) {
        getDelegate().visitEquipments(visitor);
    }

    @Override
    public TopologyKind getTopologyKind() {
        return getDelegate().getTopologyKind();
    }

    @Override
    public void printTopology() {
        getDelegate().printTopology();
    }

    @Override
    public void printTopology(final PrintStream out, final ShortIdDictionary dict) {
        getDelegate().printTopology(out, dict);
    }

    @Override
    public void exportTopology(final Path file) throws IOException {
        getDelegate().exportTopology(file);
    }

    @Override
    public void exportTopology(final Writer writer, final Random random) throws IOException {
        getDelegate().exportTopology(writer, random);
    }

    @Override
    public void exportTopology(final Writer writer) throws IOException {
        getDelegate().exportTopology(writer);
    }
}
