/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * An immutable {@link Bus}.
 * It is a read-only object, all kinds of modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableBus extends AbstractImmutableIdentifiable<Bus> implements Bus {

    ImmutableBus(Bus identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableVoltageLevel}
     */
    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(identifiable.getVoltageLevel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getV() {
        return identifiable.getV();
    }

    /**
     * Mutative operations is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Bus setV(double v) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAngle() {
        return identifiable.getAngle();
    }

    /**
     * Mutative operations is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Bus setAngle(double angle) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getP() {
        return identifiable.getP();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getQ() {
        return identifiable.getQ();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableComponent}
     */
    @Override
    public Component getConnectedComponent() {
        return cache.getComponent(identifiable.getConnectedComponent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInMainConnectedComponent() {
        return identifiable.isInMainConnectedComponent();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableComponent}
     */
    @Override
    public Component getSynchronousComponent() {
        return cache.getComponent(identifiable.getSynchronousComponent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInMainSynchronousComponent() {
        return identifiable.isInMainSynchronousComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getConnectedTerminalCount() {
        return identifiable.getConnectedTerminalCount();
    }

    /**
     * {@inheritDoc}
     * Lines are wrapped in {@link ImmutableLine}.
     */
    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(identifiable.getLines(), cache::getLine);
    }

    /**
     * {@inheritDoc}
     * Lines are wrapped in {@link ImmutableLine}.
     */
    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(cache::getLine);
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in {@link ImmutableLccConverterStation}.
     */
    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in {@link ImmutableLccConverterStation}.
     */
    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in {@link ImmutableVscConverterStation}.
     */
    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in {@link ImmutableVscConverterStation}.
     */
    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedEquipments(visitor);
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedOrConnectableEquipments(visitor);
    }

    /**
     * @return Returns the mutable bus which this instance wrapped around
     */
    Bus getBus() {
        return identifiable;
    }

}
