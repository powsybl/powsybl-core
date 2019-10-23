/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class BusAdapter extends AbstractIdentifiableAdapter<Bus> implements Bus {

    protected BusAdapter(final Bus delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getDelegate().getTwoWindingsTransformerStream().map(getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public ComponentAdapter getSynchronousComponent() {
        return getIndex().getComponent(getDelegate().getSynchronousComponent());
    }

    @Override
    public ComponentAdapter getConnectedComponent() {
        return getIndex().getComponent(getDelegate().getConnectedComponent());
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getDelegate().getThreeWindingsTransformerStream().map(getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getDelegate().getGeneratorStream().map(getIndex()::getGenerator);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getDelegate().getBatteryStream().map(getIndex()::getBattery);
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getDelegate().getLoadStream().map(getIndex()::getLoad);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getDelegate().getShuntCompensatorStream().map(getIndex()::getShuntCompensator);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getDelegate().getStaticVarCompensatorStream().map(getIndex()::getStaticVarCompensator);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getDelegate().getLccConverterStationStream().map(getIndex()::getLccConverterStation);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getDelegate().getVscConverterStationStream().map(getIndex()::getVscConverterStation);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VoltageLevelAdapter getVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
    }

    @Override
    public double getV() {
        return getDelegate().getV();
    }

    @Override
    public BusAdapter setV(final double v) {
        getDelegate().setV(v);
        return this;
    }

    @Override
    public double getAngle() {
        return getDelegate().getAngle();
    }

    @Override
    public BusAdapter setAngle(final double angle) {
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
    public boolean isInMainConnectedComponent() {
        return getDelegate().isInMainConnectedComponent();
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        return getDelegate().isInMainSynchronousComponent();
    }

    @Override
    public int getConnectedTerminalCount() {
        return getDelegate().getConnectedTerminalCount();
    }

    @Override
    public void visitConnectedEquipments(final TopologyVisitor visitor) {
        getDelegate().visitConnectedEquipments(visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(final TopologyVisitor visitor) {
        getDelegate().visitConnectedOrConnectableEquipments(visitor);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Iterable<Line> getLines() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Line> getLineStream() {
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
}
