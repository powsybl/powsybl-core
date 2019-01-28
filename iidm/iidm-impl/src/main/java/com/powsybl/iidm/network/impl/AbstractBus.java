/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBus extends AbstractIdentifiable<Bus> implements Bus {

    protected VoltageLevelExt voltageLevel;

    AbstractBus(String id, VoltageLevelExt voltageLevel) {
        super(id, id);
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
    public VoltageLevel getVoltageLevel() {
        return voltageLevel;
    }

    public abstract Iterable<TerminalExt> getConnectedTerminals();

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
                case BUSBAR_SECTION:
                case SHUNT_COMPENSATOR:
                case STATIC_VAR_COMPENSATOR:
                case LINE:
                case TWO_WINDINGS_TRANSFORMER:
                case THREE_WINDINGS_TRANSFORMER:
                case DANGLING_LINE:
                    // skip
                    break;
                case GENERATOR:
                case LOAD:
                case HVDC_CONVERTER_STATION:
                    if (!Double.isNaN(terminal.getP())) {
                        p += terminal.getP();
                    }
                    break;
                default:
                    throw new AssertionError();
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
                case BUSBAR_SECTION:
                case LINE:
                case TWO_WINDINGS_TRANSFORMER:
                case THREE_WINDINGS_TRANSFORMER:
                case DANGLING_LINE:
                    // skip
                    break;
                case GENERATOR:
                case LOAD:
                case SHUNT_COMPENSATOR:
                case STATIC_VAR_COMPENSATOR:
                case HVDC_CONVERTER_STATION:
                    if (!Double.isNaN(terminal.getQ())) {
                        q += terminal.getQ();
                    }
                    break;
                default:
                    throw new AssertionError();
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

    private <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
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
    public Iterable<DanglingLine> getDanglingLines() {
        return getConnectables(DanglingLine.class);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getConnectableStream(DanglingLine.class);
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
                case BUSBAR_SECTION:
                    visitor.visitBusbarSection((BusbarSectionImpl) connectable);
                    break;

                case LINE:
                    LineImpl line = (LineImpl) connectable;
                    visitor.visitLine(line, line.getTerminal1() == terminal ? Branch.Side.ONE
                                                                            : Branch.Side.TWO);
                    break;

                case GENERATOR:
                    visitor.visitGenerator((GeneratorImpl) connectable);
                    break;

                case SHUNT_COMPENSATOR:
                    visitor.visitShuntCompensator((ShuntCompensatorImpl) connectable);
                    break;

                case TWO_WINDINGS_TRANSFORMER:
                    TwoWindingsTransformer twt = (TwoWindingsTransformer) connectable;
                    visitor.visitTwoWindingsTransformer(twt,
                            twt.getTerminal1() == terminal
                            ? Branch.Side.ONE
                            : Branch.Side.TWO);
                    break;

                case THREE_WINDINGS_TRANSFORMER:
                    ThreeWindingsTransformer thwt = (ThreeWindingsTransformer) connectable;
                    ThreeWindingsTransformer.Side side;
                    if (thwt.getLeg1().getTerminal() == terminal) {
                        side = ThreeWindingsTransformer.Side.ONE;
                    } else if (thwt.getLeg2().getTerminal() == terminal) {
                        side = ThreeWindingsTransformer.Side.TWO;
                    } else {
                        side = ThreeWindingsTransformer.Side.THREE;
                    }
                    visitor.visitThreeWindingsTransformer(thwt, side);
                    break;

                case LOAD:
                    visitor.visitLoad((LoadImpl) connectable);
                    break;

                case DANGLING_LINE:
                    visitor.visitDanglingLine((DanglingLineImpl) connectable);
                    break;

                case STATIC_VAR_COMPENSATOR:
                    visitor.visitStaticVarCompensator((StaticVarCompensator) connectable);
                    break;

                case HVDC_CONVERTER_STATION:
                    visitor.visitHvdcConverterStation((HvdcConverterStation<?>) connectable);
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Bus";
    }
}
