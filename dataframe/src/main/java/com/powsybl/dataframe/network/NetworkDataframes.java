/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.*;
import com.powsybl.dataframe.DoubleSeriesMapper.DoubleUpdater;
import com.powsybl.dataframe.network.extensions.ExtensionDataframeKey;
import com.powsybl.dataframe.network.extensions.NetworkExtensions;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static com.powsybl.dataframe.MappingUtils.ifExistsDouble;
import static com.powsybl.dataframe.MappingUtils.ifExistsInt;

/**
 * Main user entry point of the package :
 * defines the mappings for all elements of the network.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class NetworkDataframes {

    private static final Map<DataframeElementType, NetworkDataframeMapper> MAPPERS = createMappers();

    private static final Map<ExtensionDataframeKey, NetworkDataframeMapper> EXTENSIONS_MAPPERS = NetworkExtensions.createExtensionsMappers();

    private NetworkDataframes() {
    }

    public static NetworkDataframeMapper getDataframeMapper(DataframeElementType type) {
        return MAPPERS.get(type);
    }

    private static Map<DataframeElementType, NetworkDataframeMapper> createMappers() {
        Map<DataframeElementType, NetworkDataframeMapper> mappers = new EnumMap<>(DataframeElementType.class);
        mappers.put(DataframeElementType.BUS, buses());
        mappers.put(DataframeElementType.LINE, lines());
        mappers.put(DataframeElementType.TWO_WINDINGS_TRANSFORMER, twoWindingTransformers());
        mappers.put(DataframeElementType.THREE_WINDINGS_TRANSFORMER, threeWindingTransformers());
        mappers.put(DataframeElementType.GENERATOR, generators());
        mappers.put(DataframeElementType.LOAD, loads());
        mappers.put(DataframeElementType.BATTERY, batteries());
        mappers.put(DataframeElementType.SHUNT_COMPENSATOR, shunts());
        mappers.put(DataframeElementType.NON_LINEAR_SHUNT_COMPENSATOR_SECTION, shuntsNonLinear());
        mappers.put(DataframeElementType.LINEAR_SHUNT_COMPENSATOR_SECTION, linearShuntsSections());
        mappers.put(DataframeElementType.DANGLING_LINE, danglingLines());
        mappers.put(DataframeElementType.TIE_LINE, tieLines());
        mappers.put(DataframeElementType.LCC_CONVERTER_STATION, lccs());
        mappers.put(DataframeElementType.VSC_CONVERTER_STATION, vscs());
        mappers.put(DataframeElementType.STATIC_VAR_COMPENSATOR, svcs());
        mappers.put(DataframeElementType.SWITCH, switches());
        mappers.put(DataframeElementType.VOLTAGE_LEVEL, voltageLevels());
        mappers.put(DataframeElementType.SUBSTATION, substations());
        mappers.put(DataframeElementType.BUSBAR_SECTION, busBars());
        mappers.put(DataframeElementType.HVDC_LINE, hvdcs());
        mappers.put(DataframeElementType.RATIO_TAP_CHANGER_STEP, rtcSteps());
        mappers.put(DataframeElementType.PHASE_TAP_CHANGER_STEP, ptcSteps());
        mappers.put(DataframeElementType.RATIO_TAP_CHANGER, rtcs());
        mappers.put(DataframeElementType.PHASE_TAP_CHANGER, ptcs());
        mappers.put(DataframeElementType.REACTIVE_CAPABILITY_CURVE_POINT, reactiveCapabilityCurves());
        mappers.put(DataframeElementType.OPERATIONAL_LIMITS, operationalLimits());
        mappers.put(DataframeElementType.ALIAS, aliases());
        mappers.put(DataframeElementType.IDENTIFIABLE, identifiables());
        mappers.put(DataframeElementType.INJECTION, injections());
        mappers.put(DataframeElementType.BRANCH, branches());
        mappers.put(DataframeElementType.TERMINAL, terminals());
        return Collections.unmodifiableMap(mappers);
    }

    static <U extends Injection<U>> ToDoubleFunction<U> getP() {
        return inj -> inj.getTerminal().getP();
    }

    static <U extends Injection> ToDoubleFunction<U> getOppositeP() {
        return inj -> -inj.getTerminal().getP();
    }

    static <U extends Injection<U>> ToDoubleFunction<U> getQ() {
        return inj -> inj.getTerminal().getQ();
    }

    static <U extends Injection<U>> DoubleUpdater<U> setP() {
        return (inj, p) -> inj.getTerminal().setP(p);
    }

    static <U extends Injection<U>> DoubleUpdater<U> setQ() {
        return (inj, q) -> inj.getTerminal().setQ(q);
    }

    static <U extends Branch<U>> ToDoubleFunction<U> getP1() {
        return b -> b.getTerminal1().getP();
    }

    static <U extends Branch<U>> ToDoubleFunction<U> getQ1() {
        return b -> b.getTerminal1().getQ();
    }

    static <U extends Branch<U>> DoubleUpdater<U> setP1() {
        return (b, p) -> b.getTerminal1().setP(p);
    }

    static <U extends Branch<U>> DoubleUpdater<U> setQ1() {
        return (b, q) -> b.getTerminal1().setQ(q);
    }

    static <U extends Branch<U>> ToDoubleFunction<U> getP2() {
        return b -> b.getTerminal2().getP();
    }

    static <U extends Branch<U>> ToDoubleFunction<U> getQ2() {
        return b -> b.getTerminal2().getQ();
    }

    static <U extends Branch<U>> DoubleUpdater<U> setP2() {
        return (b, p) -> b.getTerminal2().setP(p);
    }

    static <U extends Branch<U>> DoubleUpdater<U> setQ2() {
        return (b, q) -> b.getTerminal2().setQ(q);
    }

    static <U extends Injection<U>> Function<U, String> getVoltageLevelId() {
        return inj -> inj.getTerminal().getVoltageLevel().getId();
    }

    private static MinMaxReactiveLimits getMinMaxReactiveLimits(ReactiveLimitsHolder holder) {
        ReactiveLimits reactiveLimits = holder.getReactiveLimits();
        return reactiveLimits instanceof MinMaxReactiveLimits ? (MinMaxReactiveLimits) reactiveLimits : null;
    }

    static <U extends ReactiveLimitsHolder> ToDoubleFunction<U> getMinQ(ToDoubleFunction<U> pGetter) {
        return g -> {
            ReactiveLimits reactiveLimits = g.getReactiveLimits();
            return (reactiveLimits == null) ? Double.NaN : reactiveLimits.getMinQ(pGetter.applyAsDouble(g));
        };
    }

    static <U extends ReactiveLimitsHolder> ToDoubleFunction<U> getMaxQ(ToDoubleFunction<U> pGetter) {
        return g -> {
            ReactiveLimits reactiveLimits = g.getReactiveLimits();
            return (reactiveLimits == null) ? Double.NaN : reactiveLimits.getMaxQ(pGetter.applyAsDouble(g));
        };
    }

    static <U extends ReactiveLimitsHolder> DoubleUpdater<U> setMinQ() {
        return (g, minQ) -> {
            MinMaxReactiveLimits minMaxReactiveLimits = getMinMaxReactiveLimits(g);
            if (minMaxReactiveLimits != null) {
                g.newMinMaxReactiveLimits().setMinQ(minQ).setMaxQ(minMaxReactiveLimits.getMaxQ()).add();
            } else {
                throw new UnsupportedOperationException("Cannot update minQ to " + minQ +
                    ": Min-Max reactive limits do not exist.");
            }
        };
    }

    static <U extends ReactiveLimitsHolder> DoubleUpdater<U> setMaxQ() {
        return (g, maxQ) -> {
            MinMaxReactiveLimits minMaxReactiveLimits = getMinMaxReactiveLimits(g);
            if (minMaxReactiveLimits != null) {
                g.newMinMaxReactiveLimits().setMaxQ(maxQ).setMinQ(minMaxReactiveLimits.getMinQ()).add();
            } else {
                throw new UnsupportedOperationException("Cannot update maxQ to " + maxQ +
                    ": Min-Max reactive limits do not exist.");
            }
        };
    }

    private static String getReactiveLimitsKind(ReactiveLimitsHolder holder) {
        ReactiveLimits reactiveLimits = holder.getReactiveLimits();
        return (reactiveLimits == null) ? "NONE"
            : reactiveLimits.getKind().name();
    }

    private static <U extends Injection<U>> BooleanSeriesMapper.BooleanUpdater<U> connectInjection() {
        return (g, b) -> {
            Boolean res = b ? g.getTerminal().connect() : g.getTerminal().disconnect();
        };
    }

    private static <U extends Branch<U>> BooleanSeriesMapper.BooleanUpdater<U> connectBranchSide1() {
        return (g, b) -> {
            Boolean res = b ? g.getTerminal1().connect() : g.getTerminal1().disconnect();
        };
    }

    private static <U extends Branch<U>> BooleanSeriesMapper.BooleanUpdater<U> connectBranchSide2() {
        return (g, b) -> {
            Boolean res = b ? g.getTerminal2().connect() : g.getTerminal2().disconnect();
        };
    }

    private static <U extends ThreeWindingsTransformer> BooleanSeriesMapper.BooleanUpdater<U> connectLeg1() {
        return (g, b) -> {
            Boolean res = b ? g.getLeg1().getTerminal().connect() : g.getLeg1().getTerminal().disconnect();
        };
    }

    private static <U extends ThreeWindingsTransformer> BooleanSeriesMapper.BooleanUpdater<U> connectLeg2() {
        return (g, b) -> {
            Boolean res = b ? g.getLeg2().getTerminal().connect() : g.getLeg2().getTerminal().disconnect();
        };
    }

    private static <U extends ThreeWindingsTransformer> BooleanSeriesMapper.BooleanUpdater<U> connectLeg3() {
        return (g, b) -> {
            Boolean res = b ? g.getLeg3().getTerminal().connect() : g.getLeg3().getTerminal().disconnect();
        };
    }

    private static BooleanSeriesMapper.BooleanUpdater<HvdcLine> connectHvdcStation1() {
        return (g, b) -> {
            Boolean res = b ? g.getConverterStation1().getTerminal().connect() : g.getConverterStation1()
                .getTerminal()
                .disconnect();

        };
    }

    private static BooleanSeriesMapper.BooleanUpdater<HvdcLine> connectHvdcStation2() {
        return (g, b) -> {
            Boolean res = b ? g.getConverterStation2().getTerminal().connect() : g.getConverterStation2()
                .getTerminal()
                .disconnect();
        };
    }

    static NetworkDataframeMapper generators() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getGeneratorStream,
                getOrThrow(Network::getGenerator, "Generator"))
            .stringsIndex("id", Generator::getId)
            .strings("name", g -> g.getOptionalName().orElse(""))
            .enums("energy_source", EnergySource.class, Generator::getEnergySource)
            .doubles("target_p", Generator::getTargetP, Generator::setTargetP)
            .doubles("min_p", Generator::getMinP, Generator::setMinP)
            .doubles("max_p", Generator::getMaxP, Generator::setMaxP)
            .doubles("min_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMinQ),
                setMinQ())
            .doubles("max_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMaxQ),
                setMaxQ())
            .doubles("min_q_at_target_p", getMinQ(Generator::getTargetP), false)
            .doubles("max_q_at_target_p", getMaxQ(Generator::getTargetP), false)
            .doubles("min_q_at_p", getMinQ(getOppositeP()), false)
            .doubles("max_q_at_p", getMaxQ(getOppositeP()), false)
            .strings("reactive_limits_kind", NetworkDataframes::getReactiveLimitsKind)
            .doubles("target_v", Generator::getTargetV, Generator::setTargetV)
            .doubles("target_q", Generator::getTargetQ, Generator::setTargetQ)
            .booleans("voltage_regulator_on", Generator::isVoltageRegulatorOn, Generator::setVoltageRegulatorOn)
            .strings("regulated_element_id", NetworkDataframes::getRegulatedElementId,
                NetworkDataframes::setRegulatedElement)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", g -> g.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", g -> getBusId(g.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", g -> getNode(g.getTerminal()), false)
            .booleans("connected", g -> g.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static String getRegulatedElementId(Injection injection) {
        Terminal terminal;
        if (injection instanceof Generator) {
            terminal = ((Generator) injection).getRegulatingTerminal();
        } else if (injection instanceof VscConverterStation) {
            terminal = ((VscConverterStation) injection).getRegulatingTerminal();
        } else if (injection instanceof StaticVarCompensator) {
            terminal = ((StaticVarCompensator) injection).getRegulatingTerminal();
        } else {
            throw new UnsupportedOperationException(
                String.format("%s is neither a generator, a vsc station or a var static compensator",
                    injection.getId()));
        }
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            //Not supported for the moment
            return null;
        }
        return terminal.getConnectable() != null ? terminal.getConnectable().getId() : null;
    }

    private static void setRegulatedElement(Injection injection, String elementId) {
        Network network = injection.getNetwork();
        Identifiable<?> identifiable = network.getIdentifiable(elementId);
        if (identifiable instanceof Injection) {
            Terminal terminal = ((Injection<?>) identifiable).getTerminal();
            if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
                throw new UnsupportedOperationException("Cannot set regulated element to " + elementId +
                    ": not currently supported for bus breaker topologies.");
            }
            if (injection instanceof Generator) {
                ((Generator) injection).setRegulatingTerminal(((Injection<?>) identifiable).getTerminal());
            } else if (injection instanceof VscConverterStation) {
                ((VscConverterStation) injection).setRegulatingTerminal(((Injection<?>) identifiable).getTerminal());
            } else if (injection instanceof StaticVarCompensator) {
                ((StaticVarCompensator) injection).setRegulatingTerminal(((Injection<?>) identifiable).getTerminal());
            } else {
                throw new UnsupportedOperationException(
                    String.format("%s is neither a generator, a vsc station or a var static compensator",
                        injection.getId()));
            }
        } else {
            throw new UnsupportedOperationException("Cannot set regulated element to " + elementId +
                ": the regulated element may only be a busbar section or an injection.");
        }
    }

    static NetworkDataframeMapper buses() {
        return NetworkDataframeMapperBuilder.ofStream(n -> n.getBusView().getBusStream(),
                getOrThrow((b, id) -> b.getBusView().getBus(id), "Bus"))
            .stringsIndex("id", Bus::getId)
            .strings("name", b -> b.getOptionalName().orElse(""))
            .doubles("v_mag", Bus::getV, Bus::setV)
            .doubles("v_angle", Bus::getAngle, Bus::setAngle)
            .ints("connected_component", ifExistsInt(Bus::getConnectedComponent, Component::getNum))
            .ints("synchronous_component", ifExistsInt(Bus::getSynchronousComponent, Component::getNum))
            .strings("voltage_level_id", b -> b.getVoltageLevel().getId())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper loads() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getLoadStream, getOrThrow(Network::getLoad, "Load"))
            .stringsIndex("id", Load::getId)
            .strings("name", l -> l.getOptionalName().orElse(""))
            .enums("type", LoadType.class, Load::getLoadType)
            .doubles("p0", Load::getP0, Load::setP0)
            .doubles("q0", Load::getQ0, Load::setQ0)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", l -> l.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", l -> getBusId(l.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", l -> getNode(l.getTerminal()), false)
            .booleans("connected", l -> l.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper batteries() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getBatteryStream,
                getOrThrow(Network::getBattery, "Battery"))
            .stringsIndex("id", Battery::getId)
            .strings("name", b -> b.getOptionalName().orElse(""))
            .doubles("max_p", Battery::getMaxP, Battery::setMaxP)
            .doubles("min_p", Battery::getMinP, Battery::setMinP)
            .doubles("min_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMinQ),
                setMinQ())
            .doubles("max_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMaxQ),
                setMaxQ())
            .strings("reactive_limits_kind", NetworkDataframes::getReactiveLimitsKind)
            .doubles("target_p", Battery::getTargetP, Battery::setTargetP)
            .doubles("target_q", Battery::getTargetQ, Battery::setTargetQ)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", b -> b.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", b -> getBusId(b.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", b -> getNode(b.getTerminal()), false)
            .booleans("connected", b -> b.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper shunts() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getShuntCompensatorStream,
                getOrThrow(Network::getShuntCompensator, "Shunt compensator"))
            .stringsIndex("id", ShuntCompensator::getId)
            .strings("name", sc -> sc.getOptionalName().orElse(""))
            .doubles("g", ShuntCompensator::getG)
            .doubles("b", ShuntCompensator::getB)
            .enums("model_type", ShuntCompensatorModelType.class, ShuntCompensator::getModelType)
            .ints("max_section_count", ShuntCompensator::getMaximumSectionCount)
            .ints("section_count", ShuntCompensator::getSectionCount, ShuntCompensator::setSectionCount)
            .booleans("voltage_regulation_on", ShuntCompensator::isVoltageRegulatorOn,
                ShuntCompensator::setVoltageRegulatorOn)
            .doubles("target_v", ShuntCompensator::getTargetV, ShuntCompensator::setTargetV)
            .doubles("target_deadband", ShuntCompensator::getTargetDeadband, ShuntCompensator::setTargetDeadband)
            .strings("regulating_bus_id", sc -> getBusId(sc.getRegulatingTerminal()))
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", sc -> sc.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", sc -> getBusId(sc.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", sc -> getNode(sc.getTerminal()), false)
            .booleans("connected", sc -> sc.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper shuntsNonLinear() {
        Function<Network, Stream<Triple<String, ShuntCompensatorNonLinearModel.Section, Integer>>> nonLinearShunts = network ->
            network.getShuntCompensatorStream()
                .filter(sc -> sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR)
                .flatMap(shuntCompensator -> {
                    ShuntCompensatorNonLinearModel model = (ShuntCompensatorNonLinearModel) shuntCompensator.getModel();
                    return model.getAllSections()
                        .stream()
                        .map(section -> Triple.of(shuntCompensator.getId(), section,
                            model.getAllSections().indexOf(section)));
                });
        return NetworkDataframeMapperBuilder.ofStream(nonLinearShunts, NetworkDataframes::getShuntSectionNonlinear)
            .stringsIndex("id", Triple::getLeft)
            .intsIndex("section", Triple::getRight)
            .doubles("g", p -> p.getMiddle().getG(), (p, g) -> p.getMiddle().setG(g))
            .doubles("b", p -> p.getMiddle().getB(), (p, b) -> p.getMiddle().setB(b))
            .build();
    }

    static Triple<String, ShuntCompensatorNonLinearModel.Section, Integer> getShuntSectionNonlinear(Network network,
                                                                                                    UpdatingDataframe dataframe,
                                                                                                    int index) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(dataframe.getStringValue("id", index)
            .orElseThrow(() -> new PowsyblException("id is missing")));
        if (!(shuntCompensator.getModel() instanceof ShuntCompensatorNonLinearModel)) {
            throw new PowsyblException("shunt with id " + shuntCompensator.getId() + "has not a non linear model");
        } else {
            ShuntCompensatorNonLinearModel shuntNonLinear = (ShuntCompensatorNonLinearModel) shuntCompensator.getModel();
            int section = dataframe.getIntValue("section", index)
                .orElseThrow(() -> new PowsyblException("section is missing"));
            return Triple.of(shuntCompensator.getId(), shuntNonLinear.getAllSections().get(section), section);
        }
    }

    static NetworkDataframeMapper linearShuntsSections() {
        Function<Network, Stream<Pair<ShuntCompensator, ShuntCompensatorLinearModel>>> linearShunts = network ->
            network.getShuntCompensatorStream()
                .filter(sc -> sc.getModelType() == ShuntCompensatorModelType.LINEAR)
                .map(shuntCompensator -> Pair.of(shuntCompensator,
                    (ShuntCompensatorLinearModel) shuntCompensator.getModel()));
        return NetworkDataframeMapperBuilder.ofStream(linearShunts,
                (net, s) -> Pair.of(checkShuntNonNull(net, s), checkLinearModel(net, s)))
            .stringsIndex("id", p -> p.getLeft().getId())
            .doubles("g_per_section", p -> p.getRight().getGPerSection(), (p, g) -> p.getRight().setGPerSection(g))
            .doubles("b_per_section", p -> p.getRight().getBPerSection(), (p, b) -> p.getRight().setBPerSection(b))
            .ints("max_section_count", p -> p.getLeft().getMaximumSectionCount(),
                (p, s) -> p.getRight().setMaximumSectionCount(s))
            .build();
    }

    private static ShuntCompensator checkShuntNonNull(Network network, String id) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(id);
        if (shuntCompensator == null) {
            throw new PowsyblException("ShuntCompensator '" + id + "' not found");
        }
        return shuntCompensator;
    }

    private static ShuntCompensatorLinearModel checkLinearModel(Network network, String id) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(id);
        if (shuntCompensator.getModelType() != ShuntCompensatorModelType.LINEAR) {
            throw new PowsyblException("ShuntCompensator '" + id + "' is not linear");
        }
        return (ShuntCompensatorLinearModel) shuntCompensator.getModel();
    }

    static NetworkDataframeMapper lines() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getLineStream, getOrThrow(Network::getLine, "Line"))
            .stringsIndex("id", Line::getId)
            .strings("name", l -> l.getOptionalName().orElse(""))
            .doubles("r", Line::getR, Line::setR)
            .doubles("x", Line::getX, Line::setX)
            .doubles("g1", Line::getG1, Line::setG1)
            .doubles("b1", Line::getB1, Line::setB1)
            .doubles("g2", Line::getG2, Line::setG2)
            .doubles("b2", Line::getB2, Line::setB2)
            .doubles("p1", getP1(), setP1())
            .doubles("q1", getQ1(), setQ1())
            .doubles("i1", l -> l.getTerminal1().getI())
            .doubles("p2", getP2(), setP2())
            .doubles("q2", getQ2(), setQ2())
            .doubles("i2", l -> l.getTerminal2().getI())
            .strings("voltage_level1_id", l -> l.getTerminal1().getVoltageLevel().getId())
            .strings("voltage_level2_id", l -> l.getTerminal2().getVoltageLevel().getId())
            .strings("bus1_id", l -> getBusId(l.getTerminal1()))
            .strings("bus_breaker_bus1_id", l -> getBusBreakerViewBusId(l.getTerminal1()), false)
            .ints("node1", l -> getNode(l.getTerminal1()), false)
            .strings("bus2_id", l -> getBusId(l.getTerminal2()))
            .strings("bus_breaker_bus2_id", l -> getBusBreakerViewBusId(l.getTerminal2()), false)
            .ints("node2", l -> getNode(l.getTerminal2()), false)
            .booleans("connected1", l -> l.getTerminal1().isConnected(), connectBranchSide1())
            .booleans("connected2", l -> l.getTerminal2().isConnected(), connectBranchSide2())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper twoWindingTransformers() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getTwoWindingsTransformerStream,
                getOrThrow(Network::getTwoWindingsTransformer, "Two windings transformer"))
            .stringsIndex("id", TwoWindingsTransformer::getId)
            .strings("name", twt -> twt.getOptionalName().orElse(""))
            .doubles("r", TwoWindingsTransformer::getR, TwoWindingsTransformer::setR)
            .doubles("x", TwoWindingsTransformer::getX, TwoWindingsTransformer::setX)
            .doubles("g", TwoWindingsTransformer::getG, TwoWindingsTransformer::setG)
            .doubles("b", TwoWindingsTransformer::getB, TwoWindingsTransformer::setB)
            .doubles("rated_u1", TwoWindingsTransformer::getRatedU1, TwoWindingsTransformer::setRatedU1)
            .doubles("rated_u2", TwoWindingsTransformer::getRatedU2, TwoWindingsTransformer::setRatedU2)
            .doubles("rated_s", TwoWindingsTransformer::getRatedS, TwoWindingsTransformer::setRatedS)
            .doubles("p1", getP1(), setP1())
            .doubles("q1", getQ1(), setQ1())
            .doubles("i1", twt -> twt.getTerminal1().getI())
            .doubles("p2", getP2(), setP2())
            .doubles("q2", getQ2(), setQ2())
            .doubles("i2", twt -> twt.getTerminal2().getI())
            .strings("voltage_level1_id", twt -> twt.getTerminal1().getVoltageLevel().getId())
            .strings("voltage_level2_id", twt -> twt.getTerminal2().getVoltageLevel().getId())
            .strings("bus1_id", twt -> getBusId(twt.getTerminal1()))
            .strings("bus_breaker_bus1_id", twt -> getBusBreakerViewBusId(twt.getTerminal1()), false)
            .ints("node1", twt -> getNode(twt.getTerminal1()), false)
            .strings("bus2_id", twt -> getBusId(twt.getTerminal2()))
            .strings("bus_breaker_bus2_id", twt -> getBusBreakerViewBusId(twt.getTerminal2()), false)
            .ints("node2", twt -> getNode(twt.getTerminal2()), false)
            .booleans("connected1", twt -> twt.getTerminal1().isConnected(), connectBranchSide1())
            .booleans("connected2", twt -> twt.getTerminal2().isConnected(), connectBranchSide2())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper threeWindingTransformers() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getThreeWindingsTransformerStream,
                getOrThrow(Network::getThreeWindingsTransformer, "Three windings transformer"))
            .stringsIndex("id", ThreeWindingsTransformer::getId)
            .strings("name", twt -> twt.getOptionalName().orElse(""))
            .doubles("rated_u0", ThreeWindingsTransformer::getRatedU0)
            .doubles("r1", twt -> twt.getLeg1().getR(), (twt, v) -> twt.getLeg1().setR(v))
            .doubles("x1", twt -> twt.getLeg1().getX(), (twt, v) -> twt.getLeg1().setX(v))
            .doubles("g1", twt -> twt.getLeg1().getG(), (twt, v) -> twt.getLeg1().setG(v))
            .doubles("b1", twt -> twt.getLeg1().getB(), (twt, v) -> twt.getLeg1().setB(v))
            .doubles("rated_u1", twt -> twt.getLeg1().getRatedU(), (twt, v) -> twt.getLeg1().setRatedU(v))
            .doubles("rated_s1", twt -> twt.getLeg1().getRatedS(), (twt, v) -> twt.getLeg1().setRatedS(v))
            .ints("ratio_tap_position1", getRatioTapPosition(ThreeWindingsTransformer::getLeg1),
                (t, v) -> setTapPosition(t.getLeg1().getRatioTapChanger(), v))
            .ints("phase_tap_position1", getPhaseTapPosition(ThreeWindingsTransformer::getLeg1),
                (t, v) -> setTapPosition(t.getLeg1().getPhaseTapChanger(), v))
            .doubles("p1", twt -> twt.getLeg1().getTerminal().getP(), (twt, v) -> twt.getLeg1().getTerminal().setP(v))
            .doubles("q1", twt -> twt.getLeg1().getTerminal().getQ(), (twt, v) -> twt.getLeg1().getTerminal().setQ(v))
            .doubles("i1", twt -> twt.getLeg1().getTerminal().getI())
            .strings("voltage_level1_id", twt -> twt.getLeg1().getTerminal().getVoltageLevel().getId())
            .strings("bus1_id", twt -> getBusId(twt.getLeg1().getTerminal()))
            .strings("bus_breaker_bus1_id", twt -> getBusBreakerViewBusId(twt.getLeg1().getTerminal()), false)
            .ints("node1", twt -> getNode(twt.getLeg1().getTerminal()), false)
            .booleans("connected1", g -> g.getLeg1().getTerminal().isConnected(), connectLeg1())
            .doubles("r2", twt -> twt.getLeg2().getR(), (twt, v) -> twt.getLeg2().setR(v))
            .doubles("x2", twt -> twt.getLeg2().getX(), (twt, v) -> twt.getLeg2().setX(v))
            .doubles("g2", twt -> twt.getLeg2().getG(), (twt, v) -> twt.getLeg2().setG(v))
            .doubles("b2", twt -> twt.getLeg2().getB(), (twt, v) -> twt.getLeg2().setB(v))
            .doubles("rated_u2", twt -> twt.getLeg2().getRatedU(), (twt, v) -> twt.getLeg2().setRatedU(v))
            .doubles("rated_s2", twt -> twt.getLeg2().getRatedS(), (twt, v) -> twt.getLeg2().setRatedS(v))
            .ints("ratio_tap_position2", getRatioTapPosition(ThreeWindingsTransformer::getLeg2),
                (t, v) -> setTapPosition(t.getLeg2().getRatioTapChanger(), v))
            .ints("phase_tap_position2", getPhaseTapPosition(ThreeWindingsTransformer::getLeg2),
                (t, v) -> setTapPosition(t.getLeg2().getPhaseTapChanger(), v))
            .doubles("p2", twt -> twt.getLeg2().getTerminal().getP(), (twt, v) -> twt.getLeg2().getTerminal().setP(v))
            .doubles("q2", twt -> twt.getLeg2().getTerminal().getQ(), (twt, v) -> twt.getLeg2().getTerminal().setQ(v))
            .doubles("i2", twt -> twt.getLeg2().getTerminal().getI())
            .strings("voltage_level2_id", twt -> twt.getLeg2().getTerminal().getVoltageLevel().getId())
            .strings("bus2_id", twt -> getBusId(twt.getLeg2().getTerminal()))
            .strings("bus_breaker_bus2_id", twt -> getBusBreakerViewBusId(twt.getLeg2().getTerminal()), false)
            .ints("node2", twt -> getNode(twt.getLeg2().getTerminal()), false)
            .booleans("connected2", g -> g.getLeg2().getTerminal().isConnected(), connectLeg2())
            .doubles("r3", twt -> twt.getLeg3().getR(), (twt, v) -> twt.getLeg3().setR(v))
            .doubles("x3", twt -> twt.getLeg3().getX(), (twt, v) -> twt.getLeg3().setX(v))
            .doubles("g3", twt -> twt.getLeg3().getG(), (twt, v) -> twt.getLeg3().setG(v))
            .doubles("b3", twt -> twt.getLeg3().getB(), (twt, v) -> twt.getLeg3().setB(v))
            .doubles("rated_u3", twt -> twt.getLeg3().getRatedU(), (twt, v) -> twt.getLeg3().setRatedU(v))
            .doubles("rated_s3", twt -> twt.getLeg3().getRatedS(), (twt, v) -> twt.getLeg3().setRatedS(v))
            .ints("ratio_tap_position3", getRatioTapPosition(ThreeWindingsTransformer::getLeg3),
                (t, v) -> setTapPosition(t.getLeg3().getRatioTapChanger(), v))
            .ints("phase_tap_position3", getPhaseTapPosition(ThreeWindingsTransformer::getLeg3),
                (t, v) -> setTapPosition(t.getLeg3().getPhaseTapChanger(), v))
            .doubles("p3", twt -> twt.getLeg3().getTerminal().getP(), (twt, v) -> twt.getLeg3().getTerminal().setP(v))
            .doubles("q3", twt -> twt.getLeg3().getTerminal().getQ(), (twt, v) -> twt.getLeg3().getTerminal().setQ(v))
            .doubles("i3", twt -> twt.getLeg3().getTerminal().getI())
            .strings("voltage_level3_id", twt -> twt.getLeg3().getTerminal().getVoltageLevel().getId())
            .strings("bus3_id", twt -> getBusId(twt.getLeg3().getTerminal()))
            .strings("bus_breaker_bus3_id", twt -> getBusBreakerViewBusId(twt.getLeg3().getTerminal()), false)
            .ints("node3", twt -> getNode(twt.getLeg3().getTerminal()), false)
            .booleans("connected3", twt -> twt.getLeg3().getTerminal().isConnected(), connectLeg3())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper danglingLines() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getDanglingLineStream,
                getOrThrow(Network::getDanglingLine, "Dangling line"))
            .stringsIndex("id", DanglingLine::getId)
            .strings("name", dl -> dl.getOptionalName().orElse(""))
            .doubles("r", DanglingLine::getR, DanglingLine::setR)
            .doubles("x", DanglingLine::getX, DanglingLine::setX)
            .doubles("g", DanglingLine::getG, DanglingLine::setG)
            .doubles("b", DanglingLine::getB, DanglingLine::setB)
            .doubles("p0", DanglingLine::getP0, DanglingLine::setP0)
            .doubles("q0", DanglingLine::getQ0, DanglingLine::setQ0)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", dl -> dl.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", dl -> getBusId(dl.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", dl -> getNode(dl.getTerminal()), false)
            .booleans("connected", dl -> dl.getTerminal().isConnected(), connectInjection())
            .strings("ucte-x-node-code", dl -> Objects.toString(dl.getUcteXnodeCode(), ""))
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .strings("tie_line_id", dl -> dl.getTieLine().map(Identifiable::getId).orElse(""))
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper tieLines() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getTieLineStream,
                getOrThrow(Network::getTieLine, "Tie line"))
            .stringsIndex("id", TieLine::getId)
            .strings("name", tl -> tl.getOptionalName().orElse(""))
            .strings("dangling_line1_id", tl -> tl.getDanglingLine1().getId())
            .strings("dangling_line2_id", tl -> tl.getDanglingLine2().getId())
            .strings("ucte_xnode_code", tl -> Objects.toString(tl.getUcteXnodeCode(), ""))
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper lccs() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getLccConverterStationStream,
                getOrThrow(Network::getLccConverterStation, "LCC converter station"))
            .stringsIndex("id", LccConverterStation::getId)
            .strings("name", st -> st.getOptionalName().orElse(""))
            .doubles("power_factor", LccConverterStation::getPowerFactor, (lcc, v) -> lcc.setPowerFactor((float) v))
            .doubles("loss_factor", LccConverterStation::getLossFactor, (lcc, v) -> lcc.setLossFactor((float) v))
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", st -> st.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", st -> getBusId(st.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", st -> getNode(st.getTerminal()), false)
            .booleans("connected", st -> st.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    static NetworkDataframeMapper vscs() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getVscConverterStationStream,
                getOrThrow(Network::getVscConverterStation, "VSC converter station"))
            .stringsIndex("id", VscConverterStation::getId)
            .strings("name", st -> st.getOptionalName().orElse(""))
            .doubles("loss_factor", VscConverterStation::getLossFactor,
                (vscConverterStation, lf) -> vscConverterStation.setLossFactor((float) lf))
            .doubles("min_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMinQ),
                setMinQ())
            .doubles("max_q", ifExistsDouble(NetworkDataframes::getMinMaxReactiveLimits, MinMaxReactiveLimits::getMaxQ),
                setMaxQ())
            .doubles("min_q_at_p", getMinQ(getOppositeP()), false)
            .doubles("max_q_at_p", getMaxQ(getOppositeP()), false)
            .strings("reactive_limits_kind", NetworkDataframes::getReactiveLimitsKind)
            .doubles("target_v", VscConverterStation::getVoltageSetpoint, VscConverterStation::setVoltageSetpoint)
            .doubles("target_q", VscConverterStation::getReactivePowerSetpoint,
                VscConverterStation::setReactivePowerSetpoint)
            .booleans("voltage_regulator_on", VscConverterStation::isVoltageRegulatorOn,
                VscConverterStation::setVoltageRegulatorOn)
            .strings("regulated_element_id", NetworkDataframes::getRegulatedElementId,
                NetworkDataframes::setRegulatedElement)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", st -> st.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", st -> getBusId(st.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", st -> getNode(st.getTerminal()), false)
            .booleans("connected", st -> st.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper svcs() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getStaticVarCompensatorStream,
                getOrThrow(Network::getStaticVarCompensator, "Static var compensator"))
            .stringsIndex("id", StaticVarCompensator::getId)
            .strings("name", svc -> svc.getOptionalName().orElse(""))
            .doubles("b_min", StaticVarCompensator::getBmin, StaticVarCompensator::setBmin)
            .doubles("b_max", StaticVarCompensator::getBmax, StaticVarCompensator::setBmax)
            .doubles("target_v", StaticVarCompensator::getVoltageSetpoint, StaticVarCompensator::setVoltageSetpoint)
            .doubles("target_q", StaticVarCompensator::getReactivePowerSetpoint,
                StaticVarCompensator::setReactivePowerSetpoint)
            .enums("regulation_mode", StaticVarCompensator.RegulationMode.class,
                StaticVarCompensator::getRegulationMode, StaticVarCompensator::setRegulationMode)
            .strings("regulated_element_id", NetworkDataframes::getRegulatedElementId,
                NetworkDataframes::setRegulatedElement)
            .doubles("p", getP(), setP())
            .doubles("q", getQ(), setQ())
            .doubles("i", st -> st.getTerminal().getI())
            .strings("voltage_level_id", getVoltageLevelId())
            .strings("bus_id", svc -> getBusId(svc.getTerminal()))
            .strings("bus_breaker_bus_id", busBreakerViewBusId(), false)
            .ints("node", svc -> getNode(svc.getTerminal()), false)
            .booleans("connected", svc -> svc.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static String getBusBreakerBus1Id(Switch s) {
        VoltageLevel vl = s.getVoltageLevel();
        if (!s.isRetained()) {
            return "";
        }
        Bus bus = vl.getBusBreakerView().getBus1(s.getId());
        return bus != null ? bus.getId() : "";
    }

    private static String getBusBreakerBus2Id(Switch s) {
        VoltageLevel vl = s.getVoltageLevel();
        if (!s.isRetained()) {
            return "";
        }
        Bus bus = vl.getBusBreakerView().getBus2(s.getId());
        return bus != null ? bus.getId() : "";
    }

    private static int getNode1(Switch s) {
        VoltageLevel vl = s.getVoltageLevel();
        if (vl.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            return -1;
        }
        return vl.getNodeBreakerView().getNode1(s.getId());
    }

    private static int getNode2(Switch s) {
        VoltageLevel vl = s.getVoltageLevel();
        if (vl.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            return -1;
        }
        return vl.getNodeBreakerView().getNode2(s.getId());
    }

    private static NetworkDataframeMapper switches() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getSwitchStream,
                getOrThrow(Network::getSwitch, "Switch"))
            .stringsIndex("id", Switch::getId)
            .strings("name", s -> s.getOptionalName().orElse(""))
            .enums("kind", SwitchKind.class, Switch::getKind)
            .booleans("open", Switch::isOpen, Switch::setOpen)
            .booleans("retained", Switch::isRetained, Switch::setRetained)
            .strings("voltage_level_id", s -> s.getVoltageLevel().getId())
            .strings("bus_breaker_bus1_id", NetworkDataframes::getBusBreakerBus1Id, false)
            .strings("bus_breaker_bus2_id", NetworkDataframes::getBusBreakerBus2Id, false)
            .ints("node1", NetworkDataframes::getNode1, false)
            .ints("node2", NetworkDataframes::getNode2, false)
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper voltageLevels() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getVoltageLevelStream,
                getOrThrow(Network::getVoltageLevel, "Voltage level"))
            .stringsIndex("id", VoltageLevel::getId)
            .strings("name", vl -> vl.getOptionalName().orElse(""))
            .strings("substation_id", vl -> vl.getSubstation().map(Identifiable::getId).orElse(""))
            .doubles("nominal_v", VoltageLevel::getNominalV, VoltageLevel::setNominalV)
            .doubles("high_voltage_limit", VoltageLevel::getHighVoltageLimit, VoltageLevel::setHighVoltageLimit)
            .doubles("low_voltage_limit", VoltageLevel::getLowVoltageLimit, VoltageLevel::setLowVoltageLimit)
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .strings("topology_kind", vl -> vl.getTopologyKind().toString(), false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper substations() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getSubstationStream,
                getOrThrow(Network::getSubstation, "Substation"))
            .stringsIndex("id", Identifiable::getId)
            .strings("name", s -> s.getOptionalName().orElse(""))
            .strings("TSO", Substation::getTso, Substation::setTso)
            .strings("geo_tags", substation -> String.join(",", substation.getGeographicalTags()))
            .enums("country", Country.class, s -> s.getCountry().orElse(null), Substation::setCountry)
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper busBars() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getBusbarSectionStream,
                getOrThrow(Network::getBusbarSection, "Bus bar section"))
            .stringsIndex("id", BusbarSection::getId)
            .strings("name", bbs -> bbs.getOptionalName().orElse(""))
            .doubles("v", BusbarSection::getV)
            .doubles("angle", BusbarSection::getAngle)
            .strings("voltage_level_id", bbs -> bbs.getTerminal().getVoltageLevel().getId())
            .strings("bus_id", bbs -> getBusId(bbs.getTerminal()))
            .booleans("connected", bbs -> bbs.getTerminal().isConnected(), connectInjection())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper hvdcs() {

        return NetworkDataframeMapperBuilder.ofStream(Network::getHvdcLineStream,
                getOrThrow(Network::getHvdcLine, "HVDC line"))
            .stringsIndex("id", HvdcLine::getId)
            .strings("name", l -> l.getOptionalName().orElse(""))
            .enums("converters_mode", HvdcLine.ConvertersMode.class, HvdcLine::getConvertersMode,
                HvdcLine::setConvertersMode)
            .doubles("target_p", HvdcLine::getActivePowerSetpoint, HvdcLine::setActivePowerSetpoint)
            .doubles("max_p", HvdcLine::getMaxP, HvdcLine::setMaxP)
            .doubles("nominal_v", HvdcLine::getNominalV, HvdcLine::setNominalV)
            .doubles("r", HvdcLine::getR, HvdcLine::setR)
            .strings("converter_station1_id", l -> l.getConverterStation1().getId())
            .strings("converter_station2_id", l -> l.getConverterStation2().getId())
            .booleans("connected1", l -> l.getConverterStation1().getTerminal().isConnected(), connectHvdcStation1())
            .booleans("connected2", l -> l.getConverterStation2().getTerminal().isConnected(), connectHvdcStation2())
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .addProperties()
            .build();
    }

    private static NetworkDataframeMapper rtcSteps() {
        Function<Network, Stream<Triple<String, RatioTapChanger, Integer>>> ratioTapChangerSteps = network ->
            network.getTwoWindingsTransformerStream()
                .filter(twt -> twt.getRatioTapChanger() != null)
                .flatMap(twt -> twt.getRatioTapChanger()
                    .getAllSteps()
                    .keySet()
                    .stream()
                    .map(position -> Triple.of(twt.getId(), twt.getRatioTapChanger(), position)));
        return NetworkDataframeMapperBuilder.ofStream(ratioTapChangerSteps, NetworkDataframes::getRatioTapChangers)
            .stringsIndex("id", Triple::getLeft)
            .intsIndex("position", Triple::getRight)
            .doubles("rho", p -> p.getMiddle().getStep(p.getRight()).getRho(),
                (p, rho) -> p.getMiddle().getStep(p.getRight()).setRho(rho))
            .doubles("r", p -> p.getMiddle().getStep(p.getRight()).getR(),
                (p, r) -> p.getMiddle().getStep(p.getRight()).setR(r))
            .doubles("x", p -> p.getMiddle().getStep(p.getRight()).getX(),
                (p, x) -> p.getMiddle().getStep(p.getRight()).setX(x))
            .doubles("g", p -> p.getMiddle().getStep(p.getRight()).getG(),
                (p, g) -> p.getMiddle().getStep(p.getRight()).setG(g))
            .doubles("b", p -> p.getMiddle().getStep(p.getRight()).getB(),
                (p, b) -> p.getMiddle().getStep(p.getRight()).setB(b))
            .build();
    }

    static Triple<String, RatioTapChanger, Integer> getRatioTapChangers(Network network, UpdatingDataframe dataframe,
                                                                        int index) {
        String id = dataframe.getStringValue("id", index)
            .orElseThrow(() -> new IllegalArgumentException("id column is missing"));
        int position = dataframe.getIntValue("position", index)
            .orElseThrow(() -> new IllegalArgumentException("position column is missing"));
        return Triple.of(id, network.getTwoWindingsTransformer(id).getRatioTapChanger(), position);
    }

    private static NetworkDataframeMapper ptcSteps() {
        Function<Network, Stream<Triple<String, PhaseTapChanger, Integer>>> phaseTapChangerSteps = network ->
            network.getTwoWindingsTransformerStream()
                .filter(twt -> twt.getPhaseTapChanger() != null)
                .flatMap(twt -> twt.getPhaseTapChanger()
                    .getAllSteps()
                    .keySet()
                    .stream()
                    .map(position -> Triple.of(twt.getId(), twt.getPhaseTapChanger(), position)));
        return NetworkDataframeMapperBuilder.ofStream(phaseTapChangerSteps, NetworkDataframes::getPhaseTapChangers)
            .stringsIndex("id", Triple::getLeft)
            .intsIndex("position", Triple::getRight)
            .doubles("rho", p -> p.getMiddle().getStep(p.getRight()).getRho(),
                (p, rho) -> p.getMiddle().getStep(p.getRight()).setRho(rho))
            .doubles("alpha", p -> p.getMiddle().getStep(p.getRight()).getAlpha(),
                (p, alpha) -> p.getMiddle().getStep(p.getRight()).setAlpha(alpha))
            .doubles("r", p -> p.getMiddle().getStep(p.getRight()).getR(),
                (p, r) -> p.getMiddle().getStep(p.getRight()).setR(r))
            .doubles("x", p -> p.getMiddle().getStep(p.getRight()).getX(),
                (p, x) -> p.getMiddle().getStep(p.getRight()).setX(x))
            .doubles("g", p -> p.getMiddle().getStep(p.getRight()).getG(),
                (p, g) -> p.getMiddle().getStep(p.getRight()).setG(g))
            .doubles("b", p -> p.getMiddle().getStep(p.getRight()).getB(),
                (p, b) -> p.getMiddle().getStep(p.getRight()).setB(b))
            .build();
    }

    static Triple<String, PhaseTapChanger, Integer> getPhaseTapChangers(Network network, UpdatingDataframe dataframe,
                                                                        int index) {
        String id = dataframe.getStringValue("id", index)
            .orElseThrow(() -> new IllegalArgumentException("id column is missing"));
        int position = dataframe.getIntValue("position", index)
            .orElseThrow(() -> new IllegalArgumentException("position column is missing"));
        return Triple.of(id, network.getTwoWindingsTransformer(id).getPhaseTapChanger(), position);
    }

    private static NetworkDataframeMapper rtcs() {
        return NetworkDataframeMapperBuilder.ofStream(network -> network.getTwoWindingsTransformerStream()
                .filter(t -> t.getRatioTapChanger() != null), NetworkDataframes::getT2OrThrow)
            .stringsIndex("id", TwoWindingsTransformer::getId)
            .ints("tap", t -> t.getRatioTapChanger().getTapPosition(),
                (t, p) -> t.getRatioTapChanger().setTapPosition(p))
            .ints("low_tap", t -> t.getRatioTapChanger().getLowTapPosition())
            .ints("high_tap", t -> t.getRatioTapChanger().getHighTapPosition())
            .ints("step_count", t -> t.getRatioTapChanger().getStepCount())
            .booleans("on_load", t -> t.getRatioTapChanger().hasLoadTapChangingCapabilities(),
                (t, v) -> t.getRatioTapChanger().setLoadTapChangingCapabilities(v))
            .booleans("regulating", t -> t.getRatioTapChanger().isRegulating(),
                (t, v) -> t.getRatioTapChanger().setRegulating(v))
            .doubles("target_v", t -> t.getRatioTapChanger().getTargetV(),
                (t, v) -> t.getRatioTapChanger().setTargetV(v))
            .doubles("target_deadband", t -> t.getRatioTapChanger().getTargetDeadband(),
                (t, v) -> t.getRatioTapChanger().setTargetDeadband(v))
            .strings("regulating_bus_id", t -> getBusId(t.getRatioTapChanger().getRegulationTerminal()))
            .doubles("rho", NetworkDataframes::computeRho)
            .doubles("alpha",
                ifExistsDouble(TwoWindingsTransformer::getPhaseTapChanger, pc -> pc.getCurrentStep().getAlpha()))
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .strings("regulated_side", NetworkDataframes::getRatioTapChangerRegulatedSide,
                NetworkDataframes::setRatioTapChangerRegulatedSide, false)
            .build();
    }

    private static String getRatioTapChangerRegulatedSide(TwoWindingsTransformer transformer) {
        return getTerminalSideStr(transformer, transformer.getRatioTapChanger().getRegulationTerminal());
    }

    private static void setRatioTapChangerRegulatedSide(TwoWindingsTransformer transformer, String side) {
        transformer.getRatioTapChanger().setRegulationTerminal(getBranchTerminal(transformer, side));
    }

    private static double computeRho(TwoWindingsTransformer twoWindingsTransformer) {
        return twoWindingsTransformer.getRatedU2() / twoWindingsTransformer.getRatedU1()
            * (twoWindingsTransformer.getRatioTapChanger() != null ? twoWindingsTransformer.getRatioTapChanger()
            .getCurrentStep()
            .getRho() : 1)
            * (twoWindingsTransformer.getPhaseTapChanger() != null ? twoWindingsTransformer.getPhaseTapChanger()
            .getCurrentStep()
            .getRho() : 1);
    }

    private static NetworkDataframeMapper ptcs() {
        return NetworkDataframeMapperBuilder.ofStream(network -> network.getTwoWindingsTransformerStream()
                .filter(t -> t.getPhaseTapChanger() != null), NetworkDataframes::getT2OrThrow)
            .stringsIndex("id", TwoWindingsTransformer::getId)
            .ints("tap", t -> t.getPhaseTapChanger().getTapPosition(),
                (t, v) -> t.getPhaseTapChanger().setTapPosition(v))
            .ints("low_tap", t -> t.getPhaseTapChanger().getLowTapPosition())
            .ints("high_tap", t -> t.getPhaseTapChanger().getHighTapPosition())
            .ints("step_count", t -> t.getPhaseTapChanger().getStepCount())
            .booleans("regulating", t -> t.getPhaseTapChanger().isRegulating(),
                (t, v) -> t.getPhaseTapChanger().setRegulating(v))
            .enums("regulation_mode", PhaseTapChanger.RegulationMode.class,
                t -> t.getPhaseTapChanger().getRegulationMode(), (t, v) -> t.getPhaseTapChanger().setRegulationMode(v))
            .doubles("regulation_value", t -> t.getPhaseTapChanger().getRegulationValue(),
                (t, v) -> t.getPhaseTapChanger().setRegulationValue(v))
            .doubles("target_deadband", t -> t.getPhaseTapChanger().getTargetDeadband(),
                (t, v) -> t.getPhaseTapChanger().setTargetDeadband(v))
            .strings("regulating_bus_id", t -> getBusId(t.getPhaseTapChanger().getRegulationTerminal()))
            .strings("regulated_side", NetworkDataframes::getPhaseTapChangerRegulatedSide,
                NetworkDataframes::setPhaseTapChangerRegulatedSide, false)
            .booleans("fictitious", Identifiable::isFictitious, Identifiable::setFictitious, false)
            .build();
    }

    static String getPhaseTapChangerRegulatedSide(TwoWindingsTransformer transformer) {
        return getTerminalSideStr(transformer, transformer.getPhaseTapChanger().getRegulationTerminal());
    }

    static NetworkDataframeMapper identifiables() {
        return NetworkDataframeMapperBuilder.ofStream(network -> network.getIdentifiables().stream(),
                getOrThrow(Network::getIdentifiable, "Identifiable"))
            .stringsIndex("id", Identifiable::getId)
            .strings("type", identifiable -> identifiable.getType().toString())
            .build();
    }

    static NetworkDataframeMapper injections() {
        return NetworkDataframeMapperBuilder.ofStream(network -> network.getConnectableStream()
                    .filter(connectable -> connectable instanceof Injection),
                NetworkDataframes::getT2OrThrow)
            .stringsIndex("id", Connectable::getId)
            .strings("type", connectable -> connectable.getType().toString())
            .strings("voltage_level_id",
                connectable -> ((Injection<?>) connectable).getTerminal().getVoltageLevel().getId())
            .strings("bus_id",
                connectable -> ((Injection<?>) connectable).getTerminal().getBusView().getBus() == null ? "" :
                    ((Injection<?>) connectable).getTerminal().getBusView().getBus().getId())
            .build();
    }

    static NetworkDataframeMapper branches() {
        return NetworkDataframeMapperBuilder.ofStream(Network::getBranchStream,
                getOrThrow(Network::getBranch, "Branch"))
            .stringsIndex("id", Branch::getId)
            .strings("type", branch -> branch.getType().toString())
            .strings("voltage_level1_id", branch -> branch.getTerminal1().getVoltageLevel().getId())
            .strings("bus1_id", branch -> branch.getTerminal1().getBusView().getBus() == null ? "" :
                branch.getTerminal1().getBusView().getBus().getId())
            .booleans("connected1", branch -> branch.getTerminal1().isConnected(),
                (branch, connected) -> setConnected(branch.getTerminal1(), connected))
            .strings("voltage_level2_id", branch -> branch.getTerminal2().getVoltageLevel().getId())
            .strings("bus2_id", branch -> branch.getTerminal2().getBusView().getBus() == null ? "" :
                branch.getTerminal2().getBusView().getBus().getId())
            .booleans("connected2", branch -> branch.getTerminal2().isConnected(),
                (branch, connected) -> setConnected(branch.getTerminal2(), connected))
            .build();
    }

    static NetworkDataframeMapper terminals() {
        return NetworkDataframeMapperBuilder.ofStream(network -> network.getConnectableStream()
                    .flatMap(connectable -> (Stream<Terminal>) connectable.getTerminals().stream()),
                NetworkDataframes::getTerminal)
            .stringsIndex("element_id", terminal -> terminal.getConnectable().getId())
            .strings("voltage_level_id", terminal -> terminal.getVoltageLevel().getId())
            .strings("bus_id", terminal -> terminal.getBusView().getBus() == null ? "" :
                terminal.getBusView().getBus().getId())
            .strings("element_side", terminal -> terminal.getConnectable() instanceof Branch ?
                    ((Branch<?>) terminal.getConnectable()).getSide(terminal).toString() : "",
                (terminal, element_side) -> Function.identity())
            .booleans("connected", Terminal::isConnected, NetworkDataframes::setConnected)
            .build();
    }

    private static Terminal getTerminal(Network network, UpdatingDataframe dataframe, int index) {
        String id = dataframe.getStringValue("element_id", index)
            .orElseThrow(() -> new IllegalArgumentException("element_id column is missing"));
        Connectable<?> connectable = network.getConnectable(id);
        if (connectable == null) {
            throw new PowsyblException("connectable " + id + " not found");
        }
        String sideStr = dataframe.getStringValue("element_side", index).orElse(null);
        if (sideStr == null) {
            if (connectable instanceof Branch || connectable instanceof ThreeWindingsTransformer) {
                throw new PowsyblException("side must be provided for this element : " + id);
            }
            return connectable.getTerminals().get(0);
        }
        SideEnum side = SideEnum.valueOf(sideStr);
        switch (side) {
            case ONE:
                if (connectable instanceof Branch) {
                    return ((Branch<?>) connectable).getTerminal(Branch.Side.ONE);
                } else if (connectable instanceof ThreeWindingsTransformer) {
                    return ((ThreeWindingsTransformer) connectable).getTerminal(ThreeWindingsTransformer.Side.ONE);
                } else {
                    throw new PowsyblException("no side ONE for this element");
                }
            case TWO:
                if (connectable instanceof Branch) {
                    return ((Branch<?>) connectable).getTerminal(Branch.Side.TWO);
                } else if (connectable instanceof ThreeWindingsTransformer) {
                    return ((ThreeWindingsTransformer) connectable).getTerminal(ThreeWindingsTransformer.Side.TWO);
                } else {
                    throw new PowsyblException("no side TWO for this element");
                }
            case THREE:
                if (connectable instanceof ThreeWindingsTransformer) {
                    return ((ThreeWindingsTransformer) connectable).getTerminal(ThreeWindingsTransformer.Side.THREE);
                } else {
                    throw new PowsyblException("no side THREE for this element");
                }
            default:
                throw new PowsyblException("side must be ONE, TWO or THREE");
        }
    }

    private static void setConnected(Terminal terminal, boolean connected) {
        if (connected) {
            terminal.connect();
        } else {
            terminal.disconnect();
        }
    }

    private static Terminal getBranchTerminal(Branch<?> branch, String side) {
        if (side.isEmpty()) {
            return null;
        } else if (side.equals(Branch.Side.ONE.name())) {
            return branch.getTerminal1();
        } else if (side.equals(Branch.Side.TWO.name())) {
            return branch.getTerminal2();
        } else {
            throw new PowsyblException("Transformer side must be ONE or TWO");
        }
    }

    private static String getTerminalSideStr(Branch<?> branch, Terminal terminal) {
        if (terminal == branch.getTerminal1()) {
            return Branch.Side.ONE.name();
        } else if (terminal == branch.getTerminal2()) {
            return Branch.Side.TWO.name();
        }
        return "";
    }

    static void setPhaseTapChangerRegulatedSide(TwoWindingsTransformer transformer, String side) {
        transformer.getPhaseTapChanger().setRegulationTerminal(getBranchTerminal(transformer, side));
    }

    private static NetworkDataframeMapper operationalLimits() {
        return NetworkDataframeMapperBuilder.ofStream(NetworkUtil::getLimits)
            .stringsIndex("element_id", TemporaryLimitData::getId)
            .enums("element_type", IdentifiableType.class, TemporaryLimitData::getElementType)
            .enums("side", TemporaryLimitData.Side.class, TemporaryLimitData::getSide)
            .strings("name", TemporaryLimitData::getName)
            .enums("type", LimitType.class, TemporaryLimitData::getType)
            .doubles("value", TemporaryLimitData::getValue)
            .ints("acceptable_duration", TemporaryLimitData::getAcceptableDuration)
            .booleans("fictitious", TemporaryLimitData::isFictitious, false)
            .build();
    }

    private static Stream<Pair<String, ReactiveLimitsHolder>> streamReactiveLimitsHolder(Network network) {
        return Stream.concat(Stream.concat(network.getGeneratorStream().map(g -> Pair.of(g.getId(), g)),
                network.getVscConverterStationStream().map(g -> Pair.of(g.getId(), g))),
            network.getBatteryStream().map(g -> Pair.of(g.getId(), g)));
    }

    private static Stream<Triple<String, ReactiveCapabilityCurve.Point, Integer>> streamPoints(Network network) {
        return streamReactiveLimitsHolder(network)
            .filter(p -> p.getRight().getReactiveLimits() instanceof ReactiveCapabilityCurve)
            .flatMap(p -> indexPoints(p.getLeft(), p.getRight()).stream());
    }

    private static List<Triple<String, ReactiveCapabilityCurve.Point, Integer>> indexPoints(String id,
                                                                                            ReactiveLimitsHolder holder) {
        ReactiveCapabilityCurve curve = (ReactiveCapabilityCurve) holder.getReactiveLimits();
        List<Triple<String, ReactiveCapabilityCurve.Point, Integer>> values = new ArrayList<>(curve.getPointCount());
        int num = 0;
        for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
            values.add(Triple.of(id, point, num));
            num++;
        }
        return values;
    }

    private static NetworkDataframeMapper reactiveCapabilityCurves() {
        return NetworkDataframeMapperBuilder.ofStream(NetworkDataframes::streamPoints)
            .stringsIndex("id", Triple::getLeft)
            .intsIndex("num", Triple::getRight)
            .doubles("p", t -> t.getMiddle().getP())
            .doubles("min_q", t -> t.getMiddle().getMinQ())
            .doubles("max_q", t -> t.getMiddle().getMaxQ())
            .build();
    }

    private static <T> ToIntFunction<T> getRatioTapPosition(Function<T, RatioTapChangerHolder> getter) {
        return ifExistsInt(t -> getter.apply(t).getRatioTapChanger(), RatioTapChanger::getTapPosition);
    }

    private static <T> ToIntFunction<T> getPhaseTapPosition(Function<T, PhaseTapChangerHolder> getter) {
        return ifExistsInt(t -> getter.apply(t).getPhaseTapChanger(), PhaseTapChanger::getTapPosition);
    }

    private static void setTapPosition(TapChanger<?, ?> tapChanger, int position) {
        if (tapChanger != null) {
            tapChanger.setTapPosition(position);
        }
    }

    private static String getBusId(Terminal t) {
        if (t == null) {
            return "";
        } else {
            Bus bus = t.getBusView().getBus();
            return bus != null ? bus.getId() : "";
        }
    }

    private static String getBusBreakerViewBusId(Terminal t) {
        if (t == null) {
            return "";
        } else {
            Bus bus = t.isConnected() ? t.getBusBreakerView().getBus() : t.getBusBreakerView().getConnectableBus();
            return bus != null ? bus.getId() : "";
        }
    }

    private static <T extends Injection<T>> Function<T, String> busBreakerViewBusId() {
        return i -> getBusBreakerViewBusId(i.getTerminal());
    }

    private static int getNode(Terminal t) {
        if (t.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return t.getNodeBreakerView().getNode();
        } else {
            return -1;
        }
    }

    /**
     * Wraps equipment getter to throw if not found
     */
    private static <T> BiFunction<Network, String, T> getOrThrow(BiFunction<Network, String, T> getter, String type) {
        return (network, id) -> {
            T equipment = getter.apply(network, id);
            if (equipment == null) {
                throw new PowsyblException(type + " '" + id + "' not found");
            }
            return equipment;
        };
    }

    private static TwoWindingsTransformer getT2OrThrow(Network network, String id) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
        if (twt == null) {
            throw new PowsyblException("Two windings transformer '" + id + "' not found");
        }
        return twt;
    }

    public static NetworkDataframeMapper getExtensionDataframeMapper(String extensionName, String tableName) {
        return EXTENSIONS_MAPPERS.get(new ExtensionDataframeKey(extensionName, tableName));
    }

    private static NetworkDataframeMapper aliases() {
        return NetworkDataframeMapperBuilder.ofStream(NetworkDataframes::getAliasesData)
            .stringsIndex("id", pair -> pair.getLeft().getId())
            .strings("alias", Pair::getRight)
            .strings("alias_type", pair -> ((Optional<String>) pair.getLeft().getAliasType(pair.getRight())).orElse(""))
            .build();
    }

    private static Stream<Pair<Identifiable, String>> getAliasesData(Network network) {
        return network.getIdentifiables().stream()
            .flatMap(identifiable -> identifiable.getAliases().stream()
                .map(alias -> Pair.of(identifiable, alias)));
    }

}

