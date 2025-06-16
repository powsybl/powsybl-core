/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractBus extends AbstractIdentifiable<Bus> implements Bus {

    protected VoltageLevelExt voltageLevel;

    AbstractBus(String id, boolean fictitious, VoltageLevelExt voltageLevel) {
        this(id, id, fictitious, voltageLevel);
    }

    AbstractBus(String id, String name, boolean fictitious, VoltageLevelExt voltageLevel) {
        super(id, name, fictitious);
        this.voltageLevel = voltageLevel;
    }

    @Override
    public boolean isInMainConnectedComponent() {
        Component cc = getConnectedComponent();
        return cc != null && cc.getNum() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        Component sc = getSynchronousComponent();
        return sc != null && sc.getNum() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    public Network getParentNetwork() {
        return voltageLevel.getParentNetwork();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public abstract Iterable<TerminalExt> getConnectedTerminals();

    @Override
    public abstract Stream<TerminalExt> getConnectedTerminalStream();

    public abstract Collection<? extends TerminalExt> getTerminals();

    @Override
    public double getP() {
        if (getConnectedTerminalCount() == 0) {
            return Double.NaN;
        }
        double p = 0;
        for (TerminalExt terminal : getConnectedTerminals()) {
            AbstractConnectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, LINE, TWO_WINDINGS_TRANSFORMER,
                     THREE_WINDINGS_TRANSFORMER, DANGLING_LINE, GROUND -> {
                    // skip
                }
                case GENERATOR, BATTERY, LOAD, HVDC_CONVERTER_STATION -> {
                    if (!Double.isNaN(terminal.getP())) {
                        p += terminal.getP();
                    }
                }
                default -> throw new IllegalStateException();
            }
        }
        return p;
    }

    @Override
    public double getQ() {
        if (getConnectedTerminalCount() == 0) {
            return Double.NaN;
        }
        double q = 0;
        for (TerminalExt terminal : getConnectedTerminals()) {
            AbstractConnectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, DANGLING_LINE, GROUND -> {
                    // skip
                }
                case GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, HVDC_CONVERTER_STATION -> {
                    if (!Double.isNaN(terminal.getQ())) {
                        q += terminal.getQ();
                    }
                }
                default -> throw new IllegalStateException();
            }
        }
        return q;
    }

    private <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        if (getConnectedTerminalCount() == 0) {
            return Collections.emptyList();
        }
        return FluentIterable.from(getConnectedTerminals())
                             .transform(Terminal::getConnectable)
                             .filter(clazz);
    }

    protected <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        if (getConnectedTerminalCount() == 0) {
            return Stream.empty();
        }

        return getConnectedTerminalStream()
                .map(TerminalExt::getConnectable)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    @Override
    public Iterable<Line> getLines() {
        return getConnectables(Line.class);
    }

    @Override
    public Stream<Line> getLineStream() {
        return getConnectableStream(Line.class);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getConnectables(TwoWindingsTransformer.class);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getConnectableStream(TwoWindingsTransformer.class);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getConnectables(ThreeWindingsTransformer.class);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getConnectableStream(ThreeWindingsTransformer.class);
    }

    @Override
    public Iterable<Load> getLoads() {
        return getConnectables(Load.class);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getConnectableStream(Load.class);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getConnectables(ShuntCompensator.class);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getConnectableStream(ShuntCompensator.class);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getConnectables(Generator.class);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getConnectableStream(Generator.class);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getConnectables(Battery.class);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getConnectableStream(Battery.class);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return getConnectableStream(DanglingLine.class).filter(danglingLineFilter.getPredicate());
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getConnectables(StaticVarCompensator.class);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getConnectableStream(StaticVarCompensator.class);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getConnectables(LccConverterStation.class);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getConnectableStream(LccConverterStation.class);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getConnectables(VscConverterStation.class);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getConnectableStream(VscConverterStation.class);
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        visitEquipments(getConnectedTerminals(), visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        visitEquipments(getTerminals(), visitor);
    }

    static <T extends Terminal> void visitEquipments(Iterable<T> terminals, TopologyVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor is null");
        }
        for (T terminal : terminals) {
            AbstractConnectable connectable = ((TerminalExt) terminal).getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION -> visitor.visitBusbarSection((BusbarSectionImpl) connectable);
                case LINE -> {
                    Line line = (Line) connectable;
                    visitor.visitLine(line, line.getTerminal1() == terminal ? TwoSides.ONE
                        : TwoSides.TWO);
                }
                case GENERATOR -> visitor.visitGenerator((GeneratorImpl) connectable);
                case BATTERY -> visitor.visitBattery((BatteryImpl) connectable);
                case SHUNT_COMPENSATOR -> visitor.visitShuntCompensator((ShuntCompensatorImpl) connectable);
                case TWO_WINDINGS_TRANSFORMER -> {
                    TwoWindingsTransformer twt = (TwoWindingsTransformer) connectable;
                    visitor.visitTwoWindingsTransformer(twt,
                        twt.getTerminal1() == terminal
                            ? TwoSides.ONE
                            : TwoSides.TWO);
                }
                case THREE_WINDINGS_TRANSFORMER -> {
                    ThreeWindingsTransformer thwt = (ThreeWindingsTransformer) connectable;
                    ThreeSides side;
                    if (thwt.getLeg1().getTerminal() == terminal) {
                        side = ThreeSides.ONE;
                    } else if (thwt.getLeg2().getTerminal() == terminal) {
                        side = ThreeSides.TWO;
                    } else {
                        side = ThreeSides.THREE;
                    }
                    visitor.visitThreeWindingsTransformer(thwt, side);
                }
                case LOAD -> visitor.visitLoad((LoadImpl) connectable);
                case DANGLING_LINE -> visitor.visitDanglingLine((DanglingLineImpl) connectable);
                case STATIC_VAR_COMPENSATOR -> visitor.visitStaticVarCompensator((StaticVarCompensator) connectable);
                case HVDC_CONVERTER_STATION -> visitor.visitHvdcConverterStation((HvdcConverterStation<?>) connectable);
                case GROUND -> visitor.visitGround((GroundImpl) connectable);
                case DC_LINE_COMMUTATED_CONVERTER, DC_VOLTAGE_SOURCE_CONVERTER -> {
                    AcDcConverter<?> converter = (AcDcConverter<?>) connectable;
                    visitor.visitAcDcConverter(converter, converter.getSide(terminal));
                }
                default -> throw new IllegalStateException();
            }
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Bus";
    }
}
