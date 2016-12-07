/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import eu.itesla_project.iidm.network.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBus extends IdentifiableImpl<Bus> {

    protected VoltageLevelExt voltageLevel;

    AbstractBus(String id, VoltageLevelExt voltageLevel) {
        super(id, id);
        this.voltageLevel = voltageLevel;
    }

    protected abstract ConnectedComponent getConnectedComponent();

    public boolean isInMainConnectedComponent() {
        ConnectedComponent cc = getConnectedComponent();
        return cc != null && cc.getNum() == ConnectedComponent.MAIN_CC_NUM;
    }

    public VoltageLevel getVoltageLevel() {
        return voltageLevel;
    }

    public abstract int getConnectedTerminalCount();

    public abstract Iterable<TerminalExt> getConnectedTerminals();

    public abstract Stream<TerminalExt> getConnectedTerminalsStream();

    public abstract Collection<? extends TerminalExt> getTerminals();

    public float getP() {
        if (getConnectedTerminalCount() == 0) {
            return Float.NaN;
        }
        float p = 0;
        for (TerminalExt terminal : getConnectedTerminals()) {
            ConnectableImpl connectable = terminal.getConnectable();
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
                    if (!Float.isNaN(terminal.getP())) {
                        p += terminal.getP();
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
        return p;
    }

    public float getQ() {
        if (getConnectedTerminalCount() == 0) {
            return Float.NaN;
        }
        float q = 0;
        for (TerminalExt terminal : getConnectedTerminals()) {
            ConnectableImpl connectable = terminal.getConnectable();
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
                    if (!Float.isNaN(terminal.getQ())) {
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

    private <C extends Connectable> Stream<C> getConnectablesStream(Class<C> clazz) {
        if (getConnectedTerminalCount() == 0) {
            return Stream.empty();
        }

        return getConnectedTerminalsStream()
                .map(TerminalExt::getConnectable)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    public Iterable<Line> getLines() {
        return getConnectables(Line.class);
    }

    public Stream<Line> getLinesStream() {
        return getConnectablesStream(Line.class);
    }

    public Iterable<TwoWindingsTransformer> getTwoWindingTransformers() {
        return getConnectables(TwoWindingsTransformer.class);
    }

    public Stream<TwoWindingsTransformer> getTwoWindingTransformersStream() {
        return getConnectablesStream(TwoWindingsTransformer.class);
    }


    public Iterable<ThreeWindingsTransformer> getThreeWindingTransformers() {
        return getConnectables(ThreeWindingsTransformer.class);
    }

    public Stream<ThreeWindingsTransformer> getThreeWindingTransformersStream() {
        return getConnectablesStream(ThreeWindingsTransformer.class);
    }

    public Iterable<Load> getLoads() {
        return getConnectables(Load.class);
    }

    public Stream<Load> getLoadsStream() {
        return getConnectablesStream(Load.class);
    }

    public Iterable<ShuntCompensator> getShunts() {
        return getConnectables(ShuntCompensator.class);
    }

    public Stream<ShuntCompensator> getShuntsStream() {
        return getConnectablesStream(ShuntCompensator.class);
    }

    public Iterable<Generator> getGenerators() {
        return getConnectables(Generator.class);
    }

    public Stream<Generator> getGeneratorsStream() {
        return getConnectablesStream(Generator.class);
    }

    public Iterable<DanglingLine> getDanglingLines() {
        return getConnectables(DanglingLine.class);
    }

    public Stream<DanglingLine> getDanglingLinesStream() {
        return getConnectablesStream(DanglingLine.class);
    }

    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getConnectables(StaticVarCompensator.class);
    }

    public Stream<StaticVarCompensator> getStaticVarCompensatorsStream() {
        return getConnectablesStream(StaticVarCompensator.class);
    }

    public Iterable<LccConverterStation> getLccConverterStations() {
        return getConnectables(LccConverterStation.class);
    }

    public Stream<LccConverterStation> getLccConverterStationsStream() {
        return getConnectablesStream(LccConverterStation.class);
    }

    public Iterable<VscConverterStation> getVscConverterStations() {
        return getConnectables(VscConverterStation.class);
    }

    public Stream<VscConverterStation> getVscConverterStationsStream() {
        return getConnectablesStream(VscConverterStation.class);
    }

    public void visitConnectedEquipments(TopologyVisitor visitor) {
        visitEquipments(getConnectedTerminals(), visitor);
    }

    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        visitEquipments(getTerminals(), visitor);
    }

    static <T extends Terminal> void visitEquipments(Iterable<T> terminals, TopologyVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor is null");
        }
        for (T terminal : terminals) {
            ConnectableImpl connectable = ((TerminalExt) terminal).getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION:
                    visitor.visitBusbarSection((BusbarSectionImpl) connectable);
                    break;

                case LINE:
                    {
                        LineImpl line = (LineImpl) connectable;
                        visitor.visitLine(line, line.getTerminal1() == terminal
                                ? TwoTerminalsConnectable.Side.ONE
                                : TwoTerminalsConnectable.Side.TWO);
                    }
                    break;

                case GENERATOR:
                    visitor.visitGenerator((GeneratorImpl) connectable);
                    break;

                case SHUNT_COMPENSATOR:
                    visitor.visitShuntCompensator((ShuntCompensatorImpl) connectable);
                    break;

                case TWO_WINDINGS_TRANSFORMER:
                    {
                        TwoWindingsTransformer transformer = (TwoWindingsTransformer) connectable;
                        visitor.visitTwoWindingsTransformer(transformer,
                                transformer.getTerminal1() == terminal
                                ? TwoTerminalsConnectable.Side.ONE
                                : TwoTerminalsConnectable.Side.TWO);
                    }
                    break;

                case THREE_WINDINGS_TRANSFORMER:
                    {
                        ThreeWindingsTransformer transformer = (ThreeWindingsTransformer) connectable;
                        ThreeWindingsTransformer.Side side;
                        if (transformer.getLeg1().getTerminal() == terminal) {
                            side = ThreeWindingsTransformer.Side.ONE;
                        } else if (transformer.getLeg2().getTerminal() == terminal) {
                            side = ThreeWindingsTransformer.Side.TWO;
                        } else {
                            side = ThreeWindingsTransformer.Side.THREE;
                        }
                        visitor.visitThreeWindingsTransformer(transformer, side);
                    }
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
