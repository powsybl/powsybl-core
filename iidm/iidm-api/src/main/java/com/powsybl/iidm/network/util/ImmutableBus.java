/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableBus extends AbstractImmutableIdentifiable<Bus> implements Bus {

    private final ImmutableCacheIndex cache;

    ImmutableBus(Bus identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(identifiable.getVoltageLevel());
    }

    @Override
    public double getV() {
        return identifiable.getV();
    }

    @Override
    public Bus setV(double v) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getAngle() {
        return identifiable.getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getP() {
        return identifiable.getP();
    }

    @Override
    public double getQ() {
        return identifiable.getQ();
    }

    @Override
    public Component getConnectedComponent() {
        return cache.getComponent(identifiable.getConnectedComponent());
    }

    @Override
    public boolean isInMainConnectedComponent() {
        return identifiable.isInMainConnectedComponent();
    }

    @Override
    public Component getSynchronousComponent() {
        return cache.getComponent(identifiable.getSynchronousComponent());
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        return identifiable.isInMainSynchronousComponent();
    }

    @Override
    public int getConnectedTerminalCount() {
        return identifiable.getConnectedTerminalCount();
    }

    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(identifiable.getLines(), cache::getLine);
    }

    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(cache::getLine);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedEquipments(visitor);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedOrConnectableEquipments(visitor);
    }

    Bus getBus() {
        return identifiable;
    }

}
