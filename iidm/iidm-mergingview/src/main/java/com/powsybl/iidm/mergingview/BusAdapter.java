/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class BusAdapter extends AbstractIdentifiableAdapter<Bus> implements Bus {

    BusAdapter(final Bus delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(getDelegate().getTwoWindingsTransformers(),
                                   getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getDelegate().getTwoWindingsTransformerStream()
                            .map(getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(getDelegate().getThreeWindingsTransformers(),
                getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getDelegate().getThreeWindingsTransformerStream()
                            .map(getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(getDelegate().getGenerators(),
                                   getIndex()::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getDelegate().getGeneratorStream()
                            .map(getIndex()::getGenerator);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return Iterables.transform(getDelegate().getBatteries(),
                                   getIndex()::getBattery);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getDelegate().getBatteryStream()
                            .map(getIndex()::getBattery);
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(getDelegate().getLoads(),
                                   getIndex()::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getDelegate().getLoadStream()
                            .map(getIndex()::getLoad);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(getDelegate().getShuntCompensators(),
                                   getIndex()::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getDelegate().getShuntCompensatorStream()
                            .map(getIndex()::getShuntCompensator);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(getDelegate().getStaticVarCompensators(),
                                   getIndex()::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getDelegate().getStaticVarCompensatorStream()
                            .map(getIndex()::getStaticVarCompensator);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(getDelegate().getLccConverterStations(),
                                   getIndex()::getLccConverterStation);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getDelegate().getLccConverterStationStream()
                            .map(getIndex()::getLccConverterStation);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(getDelegate().getVscConverterStations(),
                                   getIndex()::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getDelegate().getVscConverterStationStream()
                            .map(getIndex()::getVscConverterStation);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getV() {
        return getDelegate().getV();
    }

    @Override
    public Bus setV(final double v) {
        getDelegate().setV(v);
        return this;
    }

    @Override
    public double getAngle() {
        return getDelegate().getAngle();
    }

    @Override
    public Bus setAngle(final double angle) {
        getDelegate().setAngle(angle);
        return this;
    }

    @Override
    public double getP() {
        return getDelegate().getP();
    }

    @Override
    public double getQ() {
        return getDelegate().getQ();
    }

    @Override
    public void visitConnectedEquipments(final TopologyVisitor visitor) {
        getDelegate().visitConnectedEquipments(new TopologyVisitorAdapter(visitor, getIndex()));
    }

    @Override
    public void visitConnectedOrConnectableEquipments(final TopologyVisitor visitor) {
        getDelegate().visitConnectedOrConnectableEquipments(new TopologyVisitorAdapter(visitor, getIndex()));
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public int getConnectedTerminalCount() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Iterable<Line> getLines() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Stream<Line> getLineStream() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Component getSynchronousComponent() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Component getConnectedComponent() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public boolean isInMainConnectedComponent() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        throw MergingView.createNotImplementedException();
    }
}
