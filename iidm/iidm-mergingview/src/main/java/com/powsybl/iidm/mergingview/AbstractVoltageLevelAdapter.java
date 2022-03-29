/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
abstract class AbstractVoltageLevelAdapter extends AbstractIdentifiableAdapter<VoltageLevel> implements VoltageLevelAdapter {

    static class BusCache<T> extends AbstractAdapter<T> {

        private final Map<Bus, BusAdapter> busCache = new HashMap<>();

        public BusCache(T delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        void invalidateCache() {
            busCache.clear();
        }

        public Bus getBus(Bus bus) {
            return bus == null ? null : busCache.computeIfAbsent(bus, key -> new BusAdapter(key, getIndex()));
        }

        public int size() {
            return busCache.size();
        }
    }

    AbstractVoltageLevelAdapter(final VoltageLevel delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ContainerType getContainerType() {
        return getDelegate().getContainerType();
    }

    @Override
    public Optional<Substation> getSubstation() {
        return getDelegate().getSubstation().map(s -> getIndex().getSubstation(s));
    }

    @Override
    public double getNominalV() {
        return getDelegate().getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(double nominalV) {
        getDelegate().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return getDelegate().getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        getDelegate().setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return getDelegate().getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        getDelegate().setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> clazz) {
        T connectable = getDelegate().getConnectable(id, clazz);
        if (clazz == DanglingLine.class) {
            DanglingLine dl = (DanglingLine) connectable;
            return dl == null || getIndex().isMerged(dl) ? null : clazz.cast(getIndex().getDanglingLine(dl));
        } else if (clazz == Line.class) {
            Line line = (Line) connectable;
            if (line != null) {
                return clazz.cast(getIndex().getLine(line));
            }

            line = getIndex().getMergedLine(id);
            if (line.getTerminal1().getVoltageLevel() == this || line.getTerminal2().getVoltageLevel() == this) {
                return clazz.cast(line);
            }

            return null;
        }

        return connectable == null ? null : clazz.cast(getIndex().getConnectable(connectable));
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toList());
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        if (clazz == Line.class) {
            Stream<T> lines = getDelegate().getConnectableStream(Line.class).map(l -> clazz.cast(getIndex().getLine(l)));
            Stream<T> mergedLines = getDelegate().getConnectableStream(DanglingLine.class)
                    .filter(getIndex()::isMerged)
                    .map(dl -> clazz.cast(getIndex().getMergedLine(dl.getId())));

            return Stream.concat(lines, mergedLines);
        } else if (clazz == DanglingLine.class) {
            return getDelegate().getConnectableStream(DanglingLine.class)
                    .filter(dl -> !getIndex().isMerged(dl))
                    .map(dl -> clazz.cast(getIndex().getDanglingLine(dl)));
        } else {
            return getDelegate().getConnectableStream(clazz)
                    .map(c -> clazz.cast(getIndex().getConnectable(c)));
        }
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return Iterables.transform(getDelegate().getConnectables(),
                getIndex()::getConnectable);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getDelegate().getConnectableStream()
                .map(getIndex()::getConnectable);
    }

    @Override
    public int getConnectableCount() {
        return getDelegate().getConnectableCount();
    }

    @Override
    public GeneratorAdder newGenerator() {
        return new GeneratorAdderAdapter(getDelegate().newGenerator(), getIndex());
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(getDelegate().getGenerators(),
                getIndex()::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getDelegate().getGeneratorStream().map(getIndex()::getGenerator);
    }

    @Override
    public int getGeneratorCount() {
        return getDelegate().getGeneratorCount();
    }

    @Override
    public BatteryAdder newBattery() {
        return new BatteryAdderAdapter(getDelegate().newBattery(), getIndex());
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return Iterables.transform(getDelegate().getBatteries(),
                getIndex()::getBattery);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getDelegate().getBatteryStream().map(getIndex()::getBattery);
    }

    @Override
    public int getBatteryCount() {
        return getDelegate().getBatteryCount();
    }

    @Override
    public LoadAdder newLoad() {
        return new LoadAdderAdapter(getDelegate().newLoad(), getIndex());
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(getDelegate().getLoads(),
                getIndex()::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getDelegate().getLoadStream().map(getIndex()::getLoad);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(getDelegate().getSwitches(),
                getIndex()::getSwitch);
    }

    @Override
    public int getSwitchCount() {
        return getDelegate().getSwitchCount();
    }

    @Override
    public int getLoadCount() {
        return getDelegate().getLoadCount();
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        return new ShuntCompensatorAdderAdapter(getDelegate().newShuntCompensator(), getIndex());
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(getDelegate().getShuntCompensators(),
                getIndex()::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getDelegate().getShuntCompensatorStream().map(getIndex()::getShuntCompensator);
    }

    @Override
    public int getShuntCompensatorCount() {
        return getDelegate().getShuntCompensatorCount();
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        return new DanglingLineAdderAdapter(getDelegate().newDanglingLine(), getIndex());
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getConnectables(DanglingLine.class);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getConnectableStream(DanglingLine.class);
    }

    @Override
    public int getDanglingLineCount() {
        return Ints.checkedCast(getConnectableStream(DanglingLine.class).count());
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        return new StaticVarCompensatorAdderAdapter(getDelegate().newStaticVarCompensator(), getIndex());
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(getDelegate().getStaticVarCompensators(),
                getIndex()::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getDelegate().getStaticVarCompensatorStream().map(getIndex()::getStaticVarCompensator);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return getDelegate().getStaticVarCompensatorCount();
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        return new VscConverterStationAdderAdapter(getDelegate().newVscConverterStation(), getIndex());
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(getDelegate().getVscConverterStations(),
                getIndex()::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getDelegate().getVscConverterStationStream().map(getIndex()::getVscConverterStation);
    }

    @Override
    public int getVscConverterStationCount() {
        return getDelegate().getVscConverterStationCount();
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        return new LccConverterStationAdderAdapter(getDelegate().newLccConverterStation(), getIndex());
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(getDelegate().getLccConverterStations(),
                getIndex()::getLccConverterStation);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getDelegate().getLccConverterStationStream().map(getIndex()::getLccConverterStation);
    }

    @Override
    public int getLccConverterStationCount() {
        return getDelegate().getLccConverterStationCount();
    }

    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(getDelegate().getLines(),
                getIndex()::getLine);
    }

    @Override
    public Stream<Line> getLineStream() {
        return getDelegate().getLineStream().map(getIndex()::getLine);
    }

    @Override
    public int getLineCount() {
        return getDelegate().getLineCount();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(getDelegate().getTwoWindingsTransformers(),
                getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getDelegate().getTwoWindingsTransformerStream().map(getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getDelegate().getTwoWindingsTransformerCount();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(getDelegate().getThreeWindingsTransformers(),
                getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getDelegate().getThreeWindingsTransformerStream().map(getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getDelegate().getThreeWindingsTransformerCount();
    }

    @Override
    public void remove() {
        // TODO(mathbagu)
        throw  MergingView.createNotImplementedException();
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        getDelegate().visitEquipments(new TopologyVisitorAdapter(visitor, getIndex()));
    }

    @Override
    public TopologyKind getTopologyKind() {
        return getDelegate().getTopologyKind();
    }

    @Override
    public void printTopology() {
        // TODO(mathbagu)
        throw MergingView.createNotImplementedException();
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        // TODO(mathbagu)
        throw MergingView.createNotImplementedException();
    }

    @Override
    public void exportTopology(Path file) throws IOException {
        // TODO(mathbagu)
        throw MergingView.createNotImplementedException();
    }

    @Override
    public void exportTopology(Writer writer, Random random) throws IOException {
        // TODO(mathbagu)
        throw MergingView.createNotImplementedException();
    }

    @Override
    public void exportTopology(Writer writer) throws IOException {
        // TODO(mathbagu)
        throw MergingView.createNotImplementedException();
    }
}
