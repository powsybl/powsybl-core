/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableBus extends AbstractImmutableIdentifiable<Bus> implements Bus {

    ImmutableBus(Bus identifiable) {
        super(identifiable);
    }

    static ImmutableBus ofNullable(Bus bus) {
        return bus == null ? null : new ImmutableBus(bus);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return ImmutableVoltageLevel.ofNullable(identifiable.getVoltageLevel());
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
        return ImmutableComponent.ofNullable(identifiable.getConnectedComponent());
    }

    @Override
    public boolean isInMainConnectedComponent() {
        return identifiable.isInMainConnectedComponent();
    }

    @Override
    public Component getSynchronousComponent() {
        return ImmutableComponent.ofNullable(identifiable.getSynchronousComponent());
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
        return Iterables.transform(identifiable.getLines(), ImmutableLine::new);
    }

    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(ImmutableLine::new);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), ImmutableGenerator::new);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(ImmutableGenerator::new);
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), ImmutableLoad::new);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(ImmutableLoad::new);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), ImmutableDanglingLine::new);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(ImmutableDanglingLine::new);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), ImmutableStaticVarCompensator::new);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(ImmutableStaticVarCompensator::new);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), ImmutableLccConverterStation::new);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(ImmutableLccConverterStation::new);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), ImmutableVscConverterStation::new);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(ImmutableVscConverterStation::new);
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        // TO REVIEW
        identifiable.visitConnectedEquipments(visitor);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), ImmutableTwoWindingsTransformer::new);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(ImmutableTwoWindingsTransformer::new);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), ImmutableThreeWindingsTransformer::new);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(ImmutableThreeWindingsTransformer::new);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), ImmutableShuntCompensator::new);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(ImmutableShuntCompensator::new);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedOrConnectableEquipments(visitor);
    }

    Bus getBus() {
        return identifiable;
    }

    @Override
    public int hashCode() {
        return identifiable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // TO REVIEW
        // TODO an immutable bus equals noraml bus???
        if (obj instanceof ImmutableBus) {
            return identifiable.equals(((ImmutableBus) obj).getBus());
        }
        return false;
    }

}
