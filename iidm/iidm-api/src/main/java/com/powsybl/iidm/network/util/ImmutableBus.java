/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableBus extends AbstractImmutableIdentifiable<Bus> implements Bus {

    public ImmutableBus(Bus identifiable) {
        super(identifiable);
    }

    public static ImmutableBus ofNullable(Bus bus) {
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
        return identifiable.getConnectedComponent();
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
        return null;
    }

    @Override
    public Stream<Line> getLineStream() {
        return null;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return null;
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return null;
    }

    @Override
    public Iterable<Load> getLoads() {
        return null;
    }

    @Override
    public Stream<Load> getLoadStream() {
        return null;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return null;
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return null;
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return null;
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return null;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return null;
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return null;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return null;
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
//        return identifiable.getVscConverterStationStream().map();
        return null;
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedEquipments(visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        identifiable.visitConnectedOrConnectableEquipments(visitor);
    }

    public Bus getBus() {
        return identifiable;
    }

    @Override
    public int hashCode() {
        return identifiable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO an immutable bus equals noraml bus???
        if (obj instanceof ImmutableBus) {
            return identifiable.equals(((ImmutableBus) obj).getBus());
        }
        return false;
    }

    protected ImmutableBus(ImmutableBus identifiable) {
        super(identifiable);
    }
}
