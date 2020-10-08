/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class BusAdapter extends AbstractIdentifiableAdapter<Bus> implements Bus {

    private final List<TerminalAdapter> terminals = new ArrayList<>();
    private final TerminalAdapter terminalRef;

    BusAdapter(final Bus delegate, final MergingViewIndex index) {
        super(delegate, index);

        delegate.getConnectedTerminalStream()
                .map(t -> getIndex().getTerminal(t))
                .forEach(terminals::add);
        if (terminals.isEmpty() && delegate.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            // If we are in this case, the bus in a calculated Bus-Breaker bus which is between two retained switches
            terminalRef = findTerminalRef(delegate, getIndex(), (s, v) -> v.getBusBreakerView().getBus1(s.getId()), (s, v) -> v.getNodeBreakerView().getNode1(s.getId()))
                    .orElseGet(() ->
                            findTerminalRef(delegate, getIndex(), (s, v) -> v.getBusBreakerView().getBus2(s.getId()), (s, v) -> v.getNodeBreakerView().getNode2(s.getId()))
                                    .orElse(null));
        } else {
            terminalRef = terminals.isEmpty() ? null : terminals.get(0);
        }
    }

    private static Optional<TerminalAdapter> findTerminalRef(Bus delegate, MergingViewIndex index,
                                                             BiFunction<Switch, VoltageLevel, Bus> busGetter, BiFunction<Switch, VoltageLevel, Integer> nodeGetter) {
        VoltageLevel vl = delegate.getVoltageLevel();
        return vl.getNodeBreakerView().getSwitchStream()
                .filter(Switch::isRetained)
                .filter(s -> busGetter.apply(s, vl).equals(delegate))
                .map(s -> nodeGetter.apply(s, vl))
                .map(i -> Networks.getEquivalentTerminal(vl, i))
                .filter(Objects::nonNull)
                .map(index::getTerminal)
                .findFirst();
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
    public Iterable<DanglingLine> getDanglingLines() {
        return MergingViewUtil.getDanglingLines(getDelegate().getDanglingLines(), getIndex());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return MergingViewUtil.getDanglingLineStream(getDelegate().getDanglingLineStream(), getIndex());
    }

    @Override
    public Iterable<Line> getLines() {
        return MergingViewUtil.getLines(getDelegate().getLines(), getDelegate().getDanglingLines(), getIndex());
    }

    @Override
    public Stream<Line> getLineStream() {
        return MergingViewUtil.getLineStream(getDelegate().getLineStream(), getDelegate().getDanglingLineStream(), getIndex());
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

    @Override
    public Component getConnectedComponent() {
        ConnectedComponentsManager ccm = getIndex().getView().getConnectedComponentsManager();
        ccm.update();

        if (terminals.isEmpty()) {
            if (terminalRef == null) {
                return null;
            }
            return ccm.getComponent(terminalRef.getConnectedComponentNumber());
        }
        return ccm.getComponent(terminals.get(0).getConnectedComponentNumber());
    }

    @Override
    public Iterable<Terminal> getConnectedTerminals() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Stream<Terminal> getConnectedTerminalStream() {
        throw MergingView.createNotImplementedException();
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        terminals.forEach(t -> t.setConnectedComponentNumber(connectedComponentNumber));
    }

    @Override
    public boolean isInMainConnectedComponent() {
        Component cc = getConnectedComponent();
        return cc != null && cc.getNum() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Component getSynchronousComponent() {
        SynchronousComponentsManager ccm = getIndex().getView().getSynchronousComponentsManager();
        ccm.update();

        if (terminals.isEmpty()) {
            if (terminalRef == null) {
                return null;
            }
            return ccm.getComponent(terminalRef.getSynchronousComponentNumber());
        }
        return ccm.getComponent(terminals.get(0).getSynchronousComponentNumber());
    }

    void setSynchronousComponentNumber(int synchronousComponentNumber) {
        terminals.forEach(t -> t.setSynchronousComponentNumber(synchronousComponentNumber));
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        Component sc = getSynchronousComponent();
        return sc != null && sc.getNum() == ComponentConstants.MAIN_NUM;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public int getConnectedTerminalCount() {
        throw MergingView.createNotImplementedException();
    }
}
