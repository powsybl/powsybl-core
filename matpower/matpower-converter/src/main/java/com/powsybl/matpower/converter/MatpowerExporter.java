/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.parameters.DynamicValueParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.HvdcUtils;
import com.powsybl.matpower.model.*;
import org.apache.commons.math3.complex.Complex;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Exporter.class)
public class MatpowerExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerExporter.class);

    private static final double BASE_MVA = 100;
    private static final String FORMAT_VERSION = "2";
    private static final int AREA_NUMBER = 1;
    private static final int LOSS_ZONE = 1;
    private static final int CONNECTED_STATUS = 1;
    private static final int DISCONNECTED_STATUS = 0;
    private static final String V_PROP = "v";
    private static final String ANGLE_PROP = "angle";
    private static final double MIN_Z_PU = Math.pow(10, -8);

    public static final String WITH_BUS_NAMES_PARAMETER_NAME = "matpower.export.with-bus-names";
    public static final String MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER_NAME = "matpower.export.max-generator-active-power-limit";
    public static final String MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER_NAME = "matpower.export.max-generator-reactive-power-limit";

    private static final boolean WITH_BUS_NAMES_DEFAULT_VALUE = false;
    private static final double MAX_GENERATOR_ACTIVE_POWER_LIMIT_DEFAULT_VALUE = 10000;
    private static final double MAX_GENERATOR_REACTIVE_POWER_LIMIT_DEFAULT_VALUE = 10000;

    private static final Parameter WITH_BUS_NAMES_PARAMETER = new Parameter(WITH_BUS_NAMES_PARAMETER_NAME,
                                                                            ParameterType.BOOLEAN,
                                                                            "Export bus names",
                                                                            WITH_BUS_NAMES_DEFAULT_VALUE);
    private static final Parameter MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER
            = new Parameter(MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER_NAME,
                            ParameterType.DOUBLE,
                            "Max generator active power limit to export",
                            MAX_GENERATOR_ACTIVE_POWER_LIMIT_DEFAULT_VALUE);
    private static final Parameter MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER
            = new Parameter(MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER_NAME,
                            ParameterType.DOUBLE,
                            "Max generator reactive power limit to export",
                            MAX_GENERATOR_REACTIVE_POWER_LIMIT_DEFAULT_VALUE);

    private static final List<Parameter> PARAMETERS = List.of(WITH_BUS_NAMES_PARAMETER,
                                                              MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER,
                                                              MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public MatpowerExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public MatpowerExporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getFormat() {
        return MatpowerConstants.FORMAT;
    }

    @Override
    public String getComment() {
        return "IIDM to MATPOWER format converter";
    }

    @Override
    public List<Parameter> getParameters() {
        return DynamicValueParameter.load(PARAMETERS, getFormat(), defaultValueConfig);
    }

    private static boolean hasSlackExtension(Bus bus) {
        VoltageLevel vl = bus.getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Terminal terminal = slackTerminal.getTerminal();
            return terminal.getBusView().getBus() == bus;
        }
        return false;
    }

    private static MBus.Type getType(Bus bus, Context context) {
        if (context.refBusId.contains(bus.getId()) || hasSlackExtension(bus)) {
            return MBus.Type.REF;
        }
        // PV buses will be defined at the end of the export process
        return MBus.Type.PQ;
    }

    static class Context {

        private final double maxGeneratorActivePowerLimit;

        private final double maxGeneratorReactivePowerLimit;

        final List<String> refBusId = new ArrayList<>();

        int num = 0;

        final Map<String, Integer> mBusesNumbersByIds = new HashMap<>();

        final List<String> generatorIdsConvertedToLoad = new ArrayList<>();
        final Set<Component> synchronousComponentsToBeExported = new HashSet<>();
        final Map<Integer, List<GenRc>> generatorsToBeExported = new HashMap<>();

        private record GenRc(String id, double targetVpu, double targetP, double minP, double maxP, double targetQ, double minQ, double maxQ,
                             boolean isValidVoltageRegulation, boolean isRemoteRegulation, double ratedS) {
        }

        public Context(double maxGeneratorActivePowerLimit, double maxGeneratorReactivePowerLimit) {
            this.maxGeneratorActivePowerLimit = maxGeneratorActivePowerLimit;
            this.maxGeneratorReactivePowerLimit = maxGeneratorReactivePowerLimit;
        }

        // Matpower power flow does not support multiple components
        // Only Vsc HvdcLines with regulation on at both converters are exported as dcLines.
        // Vsc converters with regulation on are exported as generators and as loads when regulation is off
        // All Lcc converters are exported as loads
        // then we cannot always include the complete mainConnectedComponent.
        // Only the synchronousComponents connected to the MainSynchronousComponent
        // using Vsc HvdcLines with regulation on at both converters must be considered.
        // We built a graph with all synchronousComponents connected by vsc hvdcLines.
        // Only the connectedSets including the MainSynchronousComponent, must be considered
        private void findSynchronousComponentsToBeExported(Network network) {
            // MainSynchronousComponent is always exported
            synchronousComponentsToBeExported.add(network.getBusView().getSynchronousComponents().stream()
                    .filter(Context::isMainSynchronousComponent)
                    .findAny()
                    .orElseThrow());

            List<Set<Component>> connectedSets = findConnectedSetsOfSynchronousComponents(network);

            for (Set<Component> connectedSet : connectedSets) {
                if (connectedSet.stream().anyMatch(Context::isMainSynchronousComponent)) {
                    synchronousComponentsToBeExported.addAll(connectedSet);
                }
            }
        }

        // Duplicated vertices and edges are discarded by the graph.
        private static List<Set<Component>> findConnectedSetsOfSynchronousComponents(Network network) {
            Graph<Component, Pair<Component, Component>> scGraph = new Pseudograph<>(null, null, false);

            // Only Vsc hvdcLines are considered
            network.getHvdcLines().forEach(hvdcLine -> {
                if (hvdcLine.getConverterStation1().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)
                        && hvdcLine.getConverterStation2().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)
                        && isExportedAsDcLine((VscConverterStation) hvdcLine.getConverterStation1(), (VscConverterStation) hvdcLine.getConverterStation2())) {
                    addToGraph(scGraph, hvdcLine);
                }
            });

            return new ConnectivityInspector<>(scGraph).connectedSets();
        }

        private static void addToGraph(Graph<Component, Pair<Component, Component>> scGraph, HvdcLine hvdcLine) {
            Component synchronousComponent1 = findComponent(hvdcLine.getConverterStation1());
            Component synchronousComponent2 = findComponent(hvdcLine.getConverterStation2());

            if (synchronousComponent1 != null && synchronousComponent2 != null && synchronousComponent1 != synchronousComponent2) {
                scGraph.addVertex(synchronousComponent1);
                scGraph.addVertex(synchronousComponent2);
                scGraph.addEdge(synchronousComponent1, synchronousComponent2, Pair.of(synchronousComponent1, synchronousComponent2));
            }
        }

        private static Component findComponent(HvdcConverterStation<?> hvdcConverterStation) {
            Terminal terminal = hvdcConverterStation.getTerminal();
            Bus bus = terminal.getBusView().getBus();
            return terminal.isConnected() && bus != null ? bus.getSynchronousComponent() : null;
        }

        private static boolean isMainSynchronousComponent(Component synchronousComponent) {
            return synchronousComponent.getSize() > 0 && synchronousComponent.getBuses().iterator().next().isInMainSynchronousComponent();
        }
    }

    private static boolean isExported(Bus bus, Context context) {
        return bus != null && context.synchronousComponentsToBeExported.contains(bus.getSynchronousComponent());
    }

    // In matpower cases, the bus number is the only way to identify it. During the export process, we preserve the
    // original ones if the iidm model has been created by importing a matpower model
    private static int preserveBusIds(Network network, Context context) {
        List<Bus> busBreakerViewBuses = network.getVoltageLevelStream()
                .filter(voltageLevel -> voltageLevel.getTopologyKind().equals(TopologyKind.BUS_BREAKER))
                .flatMap(voltageLevel -> voltageLevel.getBusBreakerView().getBusStream())
                .toList();

        Map<String, List<Integer>> busIdNumbers = new HashMap<>();
        for (Bus busBreakerViewBus : busBreakerViewBuses) {
            OptionalInt number = extractBusNumber(busBreakerViewBus.getId());
            if (number.isPresent()) {
                Bus bus = busBreakerViewBus.getVoltageLevel().getBusView().getMergedBus(busBreakerViewBus.getId());
                if (bus != null) {
                    busIdNumbers.computeIfAbsent(bus.getId(), n -> new ArrayList<>()).add(number.getAsInt());
                }
            }
        }
        // select the minimum as the number
        busIdNumbers.forEach((key, value) -> context.mBusesNumbersByIds.put(key, value.stream().min(Comparator.naturalOrder()).orElseThrow()));

        // last number used
        return context.mBusesNumbersByIds.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    // according to the busId of the import process
    private static OptionalInt extractBusNumber(String configuredBusId) {
        String busNumber = configuredBusId.replace("BUS-", "");
        return busNumber.matches("[1-9]\\d*") ? OptionalInt.of(Integer.parseInt(busNumber)) : OptionalInt.empty();
    }

    private static int findBusNumber(String busId, Context context) {
        if (context.mBusesNumbersByIds.containsKey(busId)) {
            return context.mBusesNumbersByIds.get(busId);
        }
        context.num++;
        context.mBusesNumbersByIds.put(busId, context.num);
        return context.num;
    }

    private static void createTransformerStarBuses(Network network, MatpowerModel model, Context context) {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            Bus bus1 = twt.getLeg1().getTerminal().getBusView().getBus();
            Bus bus2 = twt.getLeg2().getTerminal().getBusView().getBus();
            Bus bus3 = twt.getLeg3().getTerminal().getBusView().getBus();
            if (isExported(bus1, context) && isExported(bus2, context) && isExported(bus3, context)) {
                MBus mBus = new MBus();
                mBus.setNumber(findBusNumber(twt.getId(), context));
                mBus.setName(twt.getNameOrId());
                mBus.setType(MBus.Type.PQ);
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(twt.getRatedU0());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                mBus.setRealPowerDemand(0d);
                mBus.setReactivePowerDemand(0d);
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(0d);
                mBus.setVoltageMagnitude(checkAndFixVoltageMagnitude(twt.hasProperty(V_PROP) ? Double.parseDouble(twt.getProperty(V_PROP)) / twt.getRatedU0() : 1d));
                mBus.setVoltageAngle(checkAndFixVoltageAngle(twt.hasProperty(ANGLE_PROP) ? Double.parseDouble(twt.getProperty(ANGLE_PROP)) : 0d));
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
            }
        }
    }

    private static void createDanglingLineBuses(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus, context)) {
                VoltageLevel vl = t.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(findBusNumber(dl.getId(), context));
                mBus.setName(dl.getNameOrId());
                mBus.setType(MBus.Type.PQ);
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(dl.getTerminal().getVoltageLevel().getNominalV());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                mBus.setRealPowerDemand(dl.getP0());
                mBus.setReactivePowerDemand(dl.getQ0());
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(0d);
                mBus.setVoltageMagnitude(checkAndFixVoltageMagnitude(dl.getBoundary().getV() / vl.getNominalV()));
                mBus.setVoltageAngle(checkAndFixVoltageAngle(dl.getBoundary().getAngle()));
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
            }
        }
    }

    private static void createBuses(Network network, MatpowerModel model, Context context) {
        for (Bus bus : network.getBusView().getBuses()) {
            if (isExported(bus, context)) {
                VoltageLevel vl = bus.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(findBusNumber(bus.getId(), context));
                mBus.setName(bus.getNameOrId());
                mBus.setType(getType(bus, context));
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(vl.getNominalV());
                double pDemand = 0;
                double qDemand = 0;
                for (Load l : bus.getLoads()) {
                    pDemand += l.getP0();
                    qDemand += l.getQ0();
                }
                for (Battery battery : bus.getBatteries()) {
                    // generator convention for batteries
                    pDemand -= battery.getTargetP();
                    qDemand -= battery.getTargetQ();
                }
                for (LccConverterStation lcc : bus.getLccConverterStations()) {
                    pDemand += HvdcUtils.getConverterStationTargetP(lcc);
                    qDemand += HvdcUtils.getLccConverterStationLoadTargetQ(lcc);
                }
                mBus.setRealPowerDemand(pDemand);
                mBus.setReactivePowerDemand(qDemand);
                double bSum = 0;
                double zb = vl.getNominalV() * vl.getNominalV() / BASE_MVA;
                for (ShuntCompensator sc : bus.getShuntCompensators()) {
                    bSum += sc.getB() * zb * BASE_MVA;
                }
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(bSum);
                mBus.setVoltageMagnitude(checkAndFixVoltageMagnitude(bus.getV() / vl.getNominalV()));
                mBus.setVoltageAngle(checkAndFixVoltageAngle(bus.getAngle()));
                mBus.setMinimumVoltageMagnitude(getVoltageLimit(vl.getLowVoltageLimit(), vl.getNominalV()));
                mBus.setMaximumVoltageMagnitude(getVoltageLimit(vl.getHighVoltageLimit(), vl.getNominalV()));
                model.addBus(mBus);
            }
        }

        createDanglingLineBuses(network, model, context);
        createTransformerStarBuses(network, model, context);
    }

    private static double getVoltageLimit(double voltageLimit, double nominalV) {
        return Double.isNaN(voltageLimit) ? 0 : voltageLimit / nominalV;
    }

    private static boolean isEmergencyLimit(LoadingLimits.TemporaryLimit limit) {
        return limit.getAcceptableDuration() <= 60;
    }

    private static Optional<LoadingLimits.TemporaryLimit> findShortTermLimit(Stream<LoadingLimits.TemporaryLimit> limitStream) {
        return limitStream.filter(limit -> !isEmergencyLimit(limit))
                .max(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration));
    }

    private static Optional<LoadingLimits.TemporaryLimit> findEmergencyLimit(Stream<LoadingLimits.TemporaryLimit> limitStream) {
        return limitStream.filter(MatpowerExporter::isEmergencyLimit)
                .min(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration));
    }

    private static Optional<LoadingLimits.TemporaryLimit> previousLimit(Collection<LoadingLimits.TemporaryLimit> limits, LoadingLimits.TemporaryLimit limit) {
        return limits.stream().filter(l -> l.getAcceptableDuration() > limit.getAcceptableDuration())
                .min(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration));
    }

    private static double toApparentPower(double current, VoltageLevel vl) {
        return current * vl.getNominalV() / 1000d;
    }

    private static void createLimits(MBranch mBranch, LoadingLimits limits, DoubleUnaryOperator converter) {
        // rateA is mapped to permanent limit
        if (!Double.isNaN(limits.getPermanentLimit())) {
            mBranch.setRateA(converter.applyAsDouble(limits.getPermanentLimit()));
        }
        // rateB is mapped to the shortest term limit, if not an emergency limit (tempo <= 60s)
        LoadingLimits.TemporaryLimit limitB = findShortTermLimit(limits.getTemporaryLimits().stream())
                .filter(limit -> !isEmergencyLimit(limit) && limit.getValue() != Double.MAX_VALUE)
                .orElse(null);
        if (limitB != null) {
            mBranch.setRateB(converter.applyAsDouble(limitB.getValue()));
        }
        // rateC is mapped to the emergency limit (tempo <= 60s)
        findEmergencyLimit(limits.getTemporaryLimits().stream())
                .flatMap(limit -> previousLimit(limits.getTemporaryLimits(), limit))
                .filter(limit -> limitB == null || limit.getAcceptableDuration() != limitB.getAcceptableDuration())
                .ifPresent(limitC -> mBranch.setRateC(converter.applyAsDouble(limitC.getValue())));
    }

    private static void createLimits(List<FlowsLimitsHolder> limitsHolders, VoltageLevel vl, MBranch mBranch) {
        limitsHolders.stream().flatMap(limitsHolder -> Stream.concat(limitsHolder.getApparentPowerLimits().stream(), // apparent power limits first then current limits
                                                                     limitsHolder.getCurrentLimits().stream()))
                .filter(limits -> !Double.isNaN(limits.getPermanentLimit())) // skip when there is no permanent
                .max(Comparator.comparingInt(loadingLimit -> loadingLimit.getTemporaryLimits().size())) // many temporary limits first
                .ifPresent(limits -> {
                    if (limits.getLimitType() == LimitType.CURRENT) {
                        createLimits(mBranch, limits, current -> toApparentPower(current, vl)); // convert from A to MVA
                    } else {
                        createLimits(mBranch, limits, DoubleUnaryOperator.identity());
                    }
                });
    }

    /**
     * Arbitrary adapted on side one.
     */
    private static class FlowsLimitsHolderBranchAdapter implements FlowsLimitsHolder {

        private final Branch<?> branch;

        private final TwoSides side;

        public FlowsLimitsHolderBranchAdapter(Branch<?> branch, TwoSides side) {
            this.branch = branch;
            this.side = side;
        }

        @Override
        public List<OperationalLimitsGroup> getOperationalLimitsGroups() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> getSelectedOperationalLimitsGroupId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSelectedOperationalLimitsGroup(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeOperationalLimitsGroup(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancelSelectedOperationalLimitsGroup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CurrentLimits> getCurrentLimits() {
            return branch.getCurrentLimits(side);
        }

        @Override
        public CurrentLimits getNullableCurrentLimits() {
            return branch.getNullableCurrentLimits(side);
        }

        @Override
        public Optional<ActivePowerLimits> getActivePowerLimits() {
            return branch.getActivePowerLimits(side);
        }

        @Override
        public ActivePowerLimits getNullableActivePowerLimits() {
            return branch.getNullableActivePowerLimits(side);
        }

        @Override
        public Optional<ApparentPowerLimits> getApparentPowerLimits() {
            return branch.getApparentPowerLimits(side);
        }

        @Override
        public ApparentPowerLimits getNullableApparentPowerLimits() {
            return branch.getNullableApparentPowerLimits(side);
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            throw new UnsupportedOperationException();
        }
    }

    private void createLines(Network network, MatpowerModel model, Context context) {
        for (Line l : network.getLines()) {
            Terminal t1 = l.getTerminal1();
            Terminal t2 = l.getTerminal2();
            createMBranch(l.getId(), t1, t2, l.getR(), l.getX(), l.getB1(), l.getB2(), context)
                    .ifPresent(branch -> {
                        createLimits(List.of(new FlowsLimitsHolderBranchAdapter(l, TwoSides.ONE), new FlowsLimitsHolderBranchAdapter(l, TwoSides.TWO)),
                                     t1.getVoltageLevel(), branch);
                        model.addBranch(branch);
                    });
        }
    }

    private void createTransformers2(Network network, MatpowerModel model, Context context) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            createTransformer(twt, model, context);
        }
    }

    private void createTransformer(TwoWindingsTransformer twt, MatpowerModel model, Context context) {
        Terminal t1 = twt.getTerminal1();
        Terminal t2 = twt.getTerminal2();
        Bus bus1 = t1.getBusView().getBus();
        Bus bus2 = t2.getBusView().getBus();
        if (isExported(bus1, context) && isExported(bus2, context)) {
            if (!bus1.getId().equals(bus2.getId())) {
                VoltageLevel vl1 = t1.getVoltageLevel();
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus1.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(bus2.getId()));
                mBranch.setStatus(CONNECTED_STATUS);
                double r = twt.getR();
                double x = twt.getX();
                double b = twt.getB();
                double rho = (twt.getRatedU2() / vl2.getNominalV()) / (twt.getRatedU1() / vl1.getNominalV());
                var rtc = twt.getRatioTapChanger();
                if (rtc != null) {
                    rho *= rtc.getCurrentStep().getRho();
                    r *= 1 + rtc.getCurrentStep().getR() / 100;
                    x *= 1 + rtc.getCurrentStep().getX() / 100;
                    b *= 1 + rtc.getCurrentStep().getB() / 100;
                }
                var ptc = twt.getPhaseTapChanger();
                if (ptc != null) {
                    mBranch.setPhaseShiftAngle(-ptc.getCurrentStep().getAlpha());
                    rho *= ptc.getCurrentStep().getRho();
                    r *= 1 + ptc.getCurrentStep().getR() / 100;
                    x *= 1 + ptc.getCurrentStep().getX() / 100;
                    b *= 1 + ptc.getCurrentStep().getB() / 100;
                }
                mBranch.setRatio(1d / rho);
                double zb = vl2.getNominalV() * vl2.getNominalV() / BASE_MVA;
                double rpu = r / zb;
                double xpu = x / zb;
                setBranchRX(twt.getId(), mBranch, rpu, xpu);
                mBranch.setB(b * zb);
                createLimits(List.of(new FlowsLimitsHolderBranchAdapter(twt, TwoSides.ONE), new FlowsLimitsHolderBranchAdapter(twt, TwoSides.TWO)),
                    t1.getVoltageLevel(), mBranch);
                model.addBranch(mBranch);
            } else {
                LOGGER.warn("Skip branch between connected to same bus '{}' at both sides", bus1.getId());
            }
        }
    }

    private void createTieLines(Network network, MatpowerModel model, Context context) {
        for (TieLine l : network.getTieLines()) {
            Terminal t1 = l.getDanglingLine1().getTerminal();
            Terminal t2 = l.getDanglingLine2().getTerminal();
            createMBranch(l.getId(), t1, t2, l.getR(), l.getX(), l.getB1(), l.getB2(), context)
                    .ifPresent(branch -> {
                        createLimits(List.of(new FlowsLimitsHolderBranchAdapter(l, TwoSides.ONE), new FlowsLimitsHolderBranchAdapter(l, TwoSides.TWO)),
                                     t1.getVoltageLevel(), branch);
                        model.addBranch(branch);
                    });
        }
    }

    private static Optional<MBranch> createMBranch(String id, Terminal t1, Terminal t2, double r, double x, double b1, double b2, Context context) {
        Bus bus1 = t1.getBusView().getBus();
        Bus bus2 = t2.getBusView().getBus();
        if (isExported(bus1, context) && isExported(bus2, context)) {
            if (!bus1.getId().equals(bus2.getId())) {
                VoltageLevel vl1 = t1.getVoltageLevel();
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus1.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(bus2.getId()));
                mBranch.setStatus(CONNECTED_STATUS);

                double rpu = impedanceToPerUnitForLine(r, vl1.getNominalV(), vl2.getNominalV(), BASE_MVA);
                double xpu = impedanceToPerUnitForLine(x, vl1.getNominalV(), vl2.getNominalV(), BASE_MVA);
                Complex ytr = impedanceToAdmittance(r, x);
                double b1pu = admittanceEndToPerUnitForLine(ytr.getImaginary(), b1, vl1.getNominalV(), vl2.getNominalV(), BASE_MVA);
                double b2pu = admittanceEndToPerUnitForLine(ytr.getImaginary(), b2, vl2.getNominalV(), vl1.getNominalV(), BASE_MVA);
                setBranchRX(id, mBranch, rpu, xpu);
                mBranch.setB(b1pu + b2pu);
                return Optional.of(mBranch);
            } else {
                LOGGER.warn("Skip branch between connected to same bus '{}' at both sides", bus1.getId());
            }
        }
        return Optional.empty();
    }

    // avoid NaN when r and x, both are 0.0
    private static Complex impedanceToAdmittance(double r, double x) {
        return r == 0.0 && x == 0.0 ? new Complex(0.0, 0.0) : new Complex(r, x).reciprocal();
    }

    private static double impedanceToPerUnitForLine(double impedance, double nominalVoltageAtEnd,
        double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        return impedance * sBase / (nominalVoltageAtEnd * nominalVoltageAtOtherEnd);
    }

    private static double admittanceEndToPerUnitForLine(double transmissionAdmittance, double shuntAdmittanceAtEnd,
        double nominalVoltageAtEnd, double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        // note that ytr is in engineering units
        return (shuntAdmittanceAtEnd * nominalVoltageAtEnd * nominalVoltageAtEnd
            + (nominalVoltageAtEnd - nominalVoltageAtOtherEnd) * nominalVoltageAtEnd * transmissionAdmittance) / sBase;
    }

    private void createDanglingLineBranches(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus, context)) {
                VoltageLevel vl = t.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(dl.getId()));
                mBranch.setStatus(CONNECTED_STATUS);
                double zb = vl.getNominalV() * vl.getNominalV() / BASE_MVA;
                double rpu = dl.getR() / zb;
                double xpu = dl.getX() / zb;
                setBranchRX(dl.getId(), mBranch, rpu, xpu);
                mBranch.setB(dl.getB() * zb);
                createLimits(List.of(dl), t.getVoltageLevel(), mBranch);
                model.addBranch(mBranch);
            }
        }
    }

    private void createTransformerLegs(Network network, MatpowerModel model, Context context) {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            var leg1 = twt.getLeg1();
            var leg2 = twt.getLeg2();
            var leg3 = twt.getLeg3();
            Terminal t1 = leg1.getTerminal();
            Terminal t2 = leg2.getTerminal();
            Terminal t3 = leg3.getTerminal();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            Bus bus3 = t3.getBusView().getBus();
            if (isExported(bus1, context) && isExported(bus2, context) && isExported(bus3, context)) {
                model.addBranch(createTransformerLeg(twt, leg1, bus1, context));
                model.addBranch(createTransformerLeg(twt, leg2, bus2, context));
                model.addBranch(createTransformerLeg(twt, leg3, bus3, context));
            }
        }
    }

    private static MBranch createTransformerLeg(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Leg leg, Bus bus, Context context) {
        MBranch mBranch = new MBranch();
        mBranch.setFrom(context.mBusesNumbersByIds.get(bus.getId()));
        mBranch.setTo(context.mBusesNumbersByIds.get(twt.getId()));
        mBranch.setStatus(CONNECTED_STATUS);
        double rho = 1d / (leg.getRatedU() / leg.getTerminal().getVoltageLevel().getNominalV());
        double r = leg.getR();
        double x = leg.getX();
        double b = leg.getB();
        var rtc = leg.getRatioTapChanger();
        if (rtc != null) {
            rho *= rtc.getCurrentStep().getRho();
            r *= 1 + rtc.getCurrentStep().getR() / 100;
            x *= 1 + rtc.getCurrentStep().getX() / 100;
            b *= 1 + rtc.getCurrentStep().getB() / 100;
        }
        var ptc = leg.getPhaseTapChanger();
        if (ptc != null) {
            mBranch.setPhaseShiftAngle(-ptc.getCurrentStep().getAlpha());
            rho *= ptc.getCurrentStep().getRho();
            r *= 1 + ptc.getCurrentStep().getR() / 100;
            x *= 1 + ptc.getCurrentStep().getX() / 100;
            b *= 1 + ptc.getCurrentStep().getB() / 100;
        }
        double zb = Math.pow(twt.getRatedU0(), 2) / BASE_MVA;
        double rpu = r / zb;
        double xpu = x / zb;
        setBranchRX(twt.getId() + "(leg " + leg.getSide().getNum() + ")", mBranch, rpu, xpu);
        mBranch.setB(b * zb);
        mBranch.setRatio(1d / rho);
        createLimits(List.of(leg), leg.getTerminal().getVoltageLevel(), mBranch);
        return mBranch;
    }

    private static void setBranchRX(String id, MBranch mBranch, double rpu, double xpu) {
        double zpu = Math.hypot(rpu, xpu);
        double newRpu = rpu;
        double newXpu = xpu;
        if (zpu < MIN_Z_PU) {
            LOGGER.warn("Branch '{}' has a low impedance {}, cut to {}", id, zpu, MIN_Z_PU);
            newRpu = 0;
            newXpu = MIN_Z_PU;
        }
        mBranch.setR(newRpu);
        mBranch.setX(newXpu);
    }

    private void createBranches(Network network, MatpowerModel model, Context context) {
        createLines(network, model, context);
        createTieLines(network, model, context);
        createTransformers2(network, model, context);
        createDanglingLineBranches(network, model, context);
        createTransformerLegs(network, model, context);
    }

    private void findDanglingLineGenerators(Network network, Context context) {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus, context)) {
                var g = dl.getGeneration();
                if (g != null) {
                    int busNumber = context.mBusesNumbersByIds.get(dl.getId());
                    VoltageLevel vl = t.getVoltageLevel();
                    addMgen(context, busNumber, dl.getId(),
                            checkAndFixTargetVpu(g.getTargetV() / vl.getNominalV()),
                            g.getTargetP(),
                            Math.max(g.getMinP(), -context.maxGeneratorActivePowerLimit),
                            Math.min(g.getMaxP(), context.maxGeneratorActivePowerLimit),
                            g.getTargetQ(),
                            Math.max(g.getReactiveLimits().getMinQ(g.getTargetP()), -context.maxGeneratorReactivePowerLimit),
                            Math.min(g.getReactiveLimits().getMaxQ(g.getTargetP()), context.maxGeneratorReactivePowerLimit),
                            g.isVoltageRegulationOn(), false, Double.NaN);
                }
            }
        }
    }

    private void findGenerators(Network network, Context context) {
        for (Generator g : network.getGenerators()) {
            Terminal t = g.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus, context)) {
                int busNumber = context.mBusesNumbersByIds.get(bus.getId());
                String id = g.getId();
                double targetP = g.getTargetP();
                double targetQ = g.getTargetQ();
                double targetVpu = checkAndFixTargetVpu(findTargetVpu(g));
                double minP = g.getMinP();
                double maxP = g.getMaxP();
                double maxQ = g.getReactiveLimits().getMaxQ(g.getTargetP());
                double minQ = g.getReactiveLimits().getMinQ(g.getTargetP());
                Bus regulatedBus = g.getRegulatingTerminal().getBusView().getBus();
                boolean isValidVoltageRegulation = isValidVoltageRegulation(g.isVoltageRegulatorOn(), regulatedBus);
                boolean isRemoteRegulation = isRemoteRegulation(bus, regulatedBus);
                double ratedS = g.getRatedS();
                addMgen(context, busNumber, id, targetVpu, targetP, minP, maxP, targetQ, Math.min(minQ, maxQ), Math.max(minQ, maxQ), isValidVoltageRegulation, isRemoteRegulation, ratedS);
            }
        }
    }

    // matpower only supports local control, all remote control will be localized with the defined targetVpu
    private static double findTargetVpu(Generator generator) {
        return generator.getTargetV() / generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
    }

    private void findStaticVarCompensatorGenerators(Network network, Context context) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            Terminal t = svc.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus, context)) {
                int busNumber = context.mBusesNumbersByIds.get(bus.getId());
                String id = svc.getId();
                double targetQ;
                if (StaticVarCompensator.RegulationMode.REACTIVE_POWER.equals(svc.getRegulationMode())) {
                    targetQ = -svc.getReactivePowerSetpoint();
                } else { // OFF or VOLTAGE regulation
                    targetQ = 0;
                }
                double vSquared = bus.getVoltageLevel().getNominalV() * bus.getVoltageLevel().getNominalV(); // approximation
                double minQ = svc.getBmin() * vSquared;
                double maxQ = svc.getBmax() * vSquared;
                double targetVpu = checkAndFixTargetVpu(findTargetVpu(svc));
                Bus regulatedBus = svc.getRegulatingTerminal().getBusView().getBus();
                boolean isValidVoltageRegulation = isValidVoltageRegulation(StaticVarCompensator.RegulationMode.VOLTAGE.equals(svc.getRegulationMode()), regulatedBus);
                boolean isRemoteRegulation = isRemoteRegulation(bus, regulatedBus);
                addMgen(context, busNumber, id, targetVpu, 0, 0, 0, targetQ, minQ, maxQ, isValidVoltageRegulation, isRemoteRegulation, Double.NaN);
            }
        }
    }

    // matpower only supports local control, all remote control will be localized with the defined targetVpu
    private static double findTargetVpu(StaticVarCompensator staticVarCompensator) {
        return staticVarCompensator.getVoltageSetpoint() / staticVarCompensator.getRegulatingTerminal().getVoltageLevel().getNominalV();
    }

    private void createDcLines(Network network, MatpowerModel model, Context context) {

        for (HvdcLine hvdcLine : network.getHvdcLines()) {
            HvdcConverterStation<?> hvdcConverterStation1 = hvdcLine.getConverterStation1();
            HvdcConverterStation<?> hvdcConverterStation2 = hvdcLine.getConverterStation2();
            if (hvdcConverterStation1 instanceof VscConverterStation vscConverterStation1
                    && hvdcConverterStation2 instanceof VscConverterStation vscConverterStation2) {

                if (hvdcLine.getConvertersMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
                    exportVscHvdcLine(vscConverterStation1, vscConverterStation2, hvdcLine, model, context);
                } else {
                    exportVscHvdcLine(vscConverterStation2, vscConverterStation1, hvdcLine, model, context);
                }
            }
        }
    }

    private static boolean isExportedAsDcLine(VscConverterStation vscConverterStation1, VscConverterStation vscConverterStation2) {
        return vscConverterStation1.isVoltageRegulatorOn() && vscConverterStation2.isVoltageRegulatorOn();
    }

    private static void exportVscHvdcLine(VscConverterStation rectifierVscConverterStation, VscConverterStation inverterVscConverterStation, HvdcLine hvdcLine, MatpowerModel model, Context context) {
        if (isExportedAsDcLine(rectifierVscConverterStation, inverterVscConverterStation)) {
            createDcLine(rectifierVscConverterStation, inverterVscConverterStation, hvdcLine, model, context);
        } else {
            createGeneratorOrLoadFromVscConverter(rectifierVscConverterStation, context);
            createGeneratorOrLoadFromVscConverter(inverterVscConverterStation, context);
        }
    }

    private static void createDcLine(VscConverterStation rectifierVscConverterStation, VscConverterStation inverterVscConverterStation, HvdcLine hvdcLine, MatpowerModel model, Context context) {
        Terminal rectifierTerminal = rectifierVscConverterStation.getTerminal();
        Bus rectifierBus = findBus(rectifierTerminal);
        Terminal inverterTerminal = inverterVscConverterStation.getTerminal();
        Bus inverterBus = findBus(inverterTerminal);

        if (isExported(rectifierBus, context) && isExported(inverterBus, context)) {
            MDcLine mdcLine = new MDcLine();

            mdcLine.setFrom(context.mBusesNumbersByIds.get(rectifierBus.getId()));
            mdcLine.setTo(context.mBusesNumbersByIds.get(inverterBus.getId()));
            mdcLine.setStatus(getStatus(rectifierTerminal, inverterTerminal));

            double rectifierTargetP = -HvdcUtils.getConverterStationTargetP(rectifierVscConverterStation);
            double inverterTargetP = HvdcUtils.getConverterStationTargetP(inverterVscConverterStation);

            double maxP = hvdcLine.getMaxP();
            mdcLine.setPmin(0.0);
            mdcLine.setPmax(maxP);

            // equal to the negative of the injection of corresponding dummy generator
            mdcLine.setPf(rectifierTargetP);
            mdcLine.setQf(checkAndFixTargetQ(rectifierVscConverterStation.getReactivePowerSetpoint()));
            mdcLine.setVf(checkAndFixTargetVpu(findTargetVpu(rectifierVscConverterStation)));
            double rectifierMinQ = checkAndFixMinQ(rectifierVscConverterStation.getReactiveLimits().getMinQ(rectifierTargetP));
            double rectifierMaxQ = checkAndFixMaxQ(rectifierVscConverterStation.getReactiveLimits().getMaxQ(rectifierTargetP));
            mdcLine.setQminf(rectifierMinQ);
            mdcLine.setQmaxf(rectifierMaxQ);

            // equal to the injection of the corresponding generator
            mdcLine.setPt(inverterTargetP);
            mdcLine.setQt(checkAndFixTargetQ(inverterVscConverterStation.getReactivePowerSetpoint()));
            mdcLine.setVt(checkAndFixTargetVpu(findTargetVpu(inverterVscConverterStation)));
            double inverterMinQ = checkAndFixMinQ(inverterVscConverterStation.getReactiveLimits().getMinQ(inverterTargetP));
            double inverterMaxQ = checkAndFixMaxQ(inverterVscConverterStation.getReactiveLimits().getMaxQ(inverterTargetP));
            mdcLine.setQmint(inverterMinQ);
            mdcLine.setQmaxt(inverterMaxQ);

            double losses = rectifierTargetP - inverterTargetP;
            double l0 = calculateL0(rectifierVscConverterStation.getLossFactor(), rectifierTargetP, losses);
            mdcLine.setLoss0(l0);
            mdcLine.setLoss1(calculateL1(l0, losses, rectifierTargetP));
            model.addDcLine(mdcLine);
        }
    }

    private static Bus findBus(Terminal terminal) {
        return terminal.getBusView().getBus() != null ? terminal.getBusView().getBus() : terminal.getBusView().getConnectableBus();
    }

    private static int getStatus(Terminal t1, Terminal t2) {
        return t1.isConnected() && t2.isConnected() ? CONNECTED_STATUS : DISCONNECTED_STATUS;
    }

    private static double checkAndFixTargetQ(double targetQ) {
        return Double.isNaN(targetQ) ? 0.0 : targetQ;
    }

    private static double checkAndFixTargetVpu(double targetVpu) {
        return Double.isNaN(targetVpu) || targetVpu <= 0.0 ? 1.0 : targetVpu;
    }

    // If minQ is -Double.MAX_VALUE matpower sets the reactive power in dclines to NaN
    private static double checkAndFixMinQ(double minQ) {
        return minQ < -Integer.MAX_VALUE ? -Integer.MAX_VALUE : minQ;
    }

    // If maxQ is Double.MAX_VALUE matpower sets the reactive power in dclines to NaN
    private static double checkAndFixMaxQ(double maxQ) {
        return maxQ > Integer.MAX_VALUE ? Integer.MAX_VALUE : maxQ;
    }

    // matpower only supports local control, all remote control will be localized with the defined targetVpu
    private static double findTargetVpu(VscConverterStation vscConverterStation) {
        double nominalV = vscConverterStation.getTerminal().getVoltageLevel().getNominalV();
        if (vscConverterStation.getRegulatingTerminal() != null) {
            nominalV = vscConverterStation.getRegulatingTerminal().getVoltageLevel().getNominalV();
        }
        return vscConverterStation.getVoltageSetpoint() / nominalV;
    }

    // According to the import process, to guarantee round-trip
    private static double calculateL0(double lossFactor, double rectifierTargetP, double losses) {
        return rectifierTargetP != 0.0 ? lossFactor * rectifierTargetP / 100.0 : losses;
    }

    private static double calculateL1(double l0, double losses, double rectifierTargetP) {
        return rectifierTargetP != 0.0 ? (losses - l0) / rectifierTargetP : 0.0;
    }

    private static void createGeneratorOrLoadFromVscConverter(VscConverterStation vscConverterStation, Context context) {
        Terminal terminal = vscConverterStation.getTerminal();
        Bus bus = findBus(terminal);

        if (isExported(bus, context)) {
            int busNumber = context.mBusesNumbersByIds.get(bus.getId());
            String id = vscConverterStation.getId();
            double targetQ = checkAndFixTargetQ(vscConverterStation.getReactivePowerSetpoint());
            double targetVpu = checkAndFixTargetVpu(findTargetVpu(vscConverterStation));
            Bus regulatedBus = vscConverterStation.getRegulatingTerminal().getBusView().getBus();
            double targetP = HvdcUtils.getConverterStationTargetP(vscConverterStation);
            double minQ = checkAndFixMinQ(vscConverterStation.getReactiveLimits().getMinQ(targetP)); // approximation
            double maxQ = checkAndFixMaxQ(vscConverterStation.getReactiveLimits().getMaxQ(targetP)); // approximation
            boolean isValidVoltageRegulation = isValidVoltageRegulation(vscConverterStation.isVoltageRegulatorOn(), regulatedBus);
            double maxP = vscConverterStation.getHvdcLine().getMaxP();
            boolean isRemoteRegulation = isRemoteRegulation(bus, regulatedBus);
            addMgen(context, busNumber, id, targetVpu, targetP, -maxP, maxP, targetQ, minQ, maxQ, isValidVoltageRegulation, isRemoteRegulation, Double.NaN);
        }
    }

    private static void addMgen(Context context, int busNum, String id, double targetVpu, double targetP, double minP, double maxP,
                                double targetQ, double minQ, double maxQ, boolean isValidVoltageRegulation, boolean isRemoteRegulation, double ratedS) {
        Context.GenRc genRc = new Context.GenRc(id, targetVpu, targetP, minP, maxP, targetQ, minQ, maxQ, isValidVoltageRegulation, isRemoteRegulation, ratedS);
        context.generatorsToBeExported.computeIfAbsent(busNum, k -> new ArrayList<>()).add(genRc);
    }

    // Matpower power flow does not support bus with multiple generators that do not have the same voltage regulation
    // status. if the bus has PV type, all of its generator must have a valid voltage set point.
    private static void createGeneratorsAndDefinePVBuses(MatpowerModel model, Context context) {
        context.generatorsToBeExported.keySet().stream().sorted().forEach(busNumber -> {
            List<Context.GenRc> genRcs = context.generatorsToBeExported.get(busNumber);
            MBus mBus = model.getBusByNum(busNumber);
            List<Context.GenRc> genRcsWithRegulationOn = genRcs.stream().filter(genRc -> genRc.isValidVoltageRegulation).toList();
            List<Context.GenRc> genRcsWithRegulationOff = genRcs.stream().filter(genRc -> !genRc.isValidVoltageRegulation).toList();
            if (genRcsWithRegulationOn.isEmpty()) {
                genRcsWithRegulationOff.forEach(genRc -> {
                    MGen mGen = createMGen(model, busNumber, genRc, context);
                    // we can safely set voltage setpoint to zero, because a PQ bus never go back to PV even if reactive limits
                    // are activated in Matpower power flow
                    mGen.setVoltageMagnitudeSetpoint(0);
                });
            } else {
                if (mBus.getType().equals(MBus.Type.PQ)) {
                    mBus.setType(MBus.Type.PV);
                }
                genRcsWithRegulationOn.forEach(genRc -> createMGen(model, busNumber, genRc, context));

                genRcsWithRegulationOff.forEach(genRc -> {
                    mBus.setRealPowerDemand(mBus.getRealPowerDemand() - genRc.targetP);
                    mBus.setReactivePowerDemand(mBus.getReactivePowerDemand() - genRc.targetQ);
                    context.generatorIdsConvertedToLoad.add(genRc.id);
                });
            }
        });
    }

    private static MGen createMGen(MatpowerModel model, int busNumber, Context.GenRc genRc, Context context) {
        MGen mGen = new MGen();
        mGen.setNumber(busNumber);
        mGen.setStatus(CONNECTED_STATUS);
        mGen.setRealPowerOutput(genRc.targetP);
        mGen.setReactivePowerOutput(Double.isNaN(genRc.targetQ) ? 0 : genRc.targetQ);
        mGen.setVoltageMagnitudeSetpoint(genRc.targetVpu);

        mGen.setMinimumRealPowerOutput(Math.max(genRc.minP, -context.maxGeneratorActivePowerLimit));
        mGen.setMaximumRealPowerOutput(Math.min(genRc.maxP, context.maxGeneratorActivePowerLimit));
        mGen.setMinimumReactivePowerOutput(Math.max(genRc.minQ, -context.maxGeneratorReactivePowerLimit));
        mGen.setMaximumReactivePowerOutput(Math.min(genRc.maxQ, context.maxGeneratorReactivePowerLimit));
        mGen.setTotalMbase(Double.isNaN(genRc.ratedS) ? 0 : genRc.ratedS);
        model.addGenerator(mGen);

        if (genRc.isRemoteRegulation) {
            LOGGER.warn("Generator remote voltage control not supported in Matpower model, control has been localized {}", genRc.id);
        }
        return mGen;
    }

    private static boolean isValidVoltageRegulation(boolean voltageRegulation, Bus regulatedBus) {
        return voltageRegulation && regulatedBus != null;
    }

    private static boolean isRemoteRegulation(Bus bus, Bus regulatedBus) {
        return !(bus != null && regulatedBus != null && bus.getId().equals(regulatedBus.getId()));
    }

    private static double checkAndFixVoltageMagnitude(double voltageMagnitude) {
        return Double.isNaN(voltageMagnitude) || voltageMagnitude <= 0.0 ? 1.0 : voltageMagnitude;
    }

    private static double checkAndFixVoltageAngle(double voltageAngle) {
        return Double.isNaN(voltageAngle) ? 0.0 : voltageAngle;
    }

    // Matpower needs a slack bus for each synchronous component
    // Slack must be defined in a bus with generation or with dclines
    // to serve the roles of both a voltage angle reference and
    // a real power slack
    private static void findSlackBusesForEachSynchronousComponent(Context context) {

        context.synchronousComponentsToBeExported.forEach(synchronousComponent -> {
            boolean hasSlack = synchronousComponent.getBusStream()
                    .filter(bus -> isExported(bus, context))
                    .anyMatch(MatpowerExporter::hasSlackExtension);

            if (!hasSlack) {
                String refBusId = synchronousComponent.getBusStream()
                        .filter(bus -> isExported(bus, context))
                        .map(MatpowerExporter::getActivePowerGenerationAndVscCount)
                        .max(Comparator.comparing(Rc::activePowerGeneration)
                                .thenComparing(Rc::vscConvertersWithRegulationOn)
                                .thenComparing(rc -> rc.bus.getId()))
                        .orElseThrow()
                        .bus().getId();
                context.refBusId.add(refBusId);
                LOGGER.debug("Matpower reference bus automatically selected: {} for synchronousComponent: {}", refBusId, synchronousComponent.getNum());
            }
        });
    }

    private static Rc getActivePowerGenerationAndVscCount(Bus bus) {
        double[] activePowerGeneration = new double[1];
        int[] vscConverterCount = new int[1];
        bus.visitConnectedEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitGenerator(Generator generator) {
                activePowerGeneration[0] += generator.getMaxP();
            }

            @Override
            public void visitHvdcConverterStation(HvdcConverterStation<?> hvdcConverterStation) {
                if (hvdcConverterStation instanceof VscConverterStation vscConverterStation
                        && vscConverterStation.isVoltageRegulatorOn()) {
                        vscConverterCount[0]++;
                    }
                }
        });
        return new Rc(bus, activePowerGeneration[0], vscConverterCount[0]);
    }

    private record Rc(Bus bus, double activePowerGeneration, int vscConvertersWithRegulationOn) {
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reportNode);

        boolean withBusNames = Parameter.readBoolean(getFormat(), parameters, WITH_BUS_NAMES_PARAMETER, defaultValueConfig);
        double maxGeneratorActivePower = Parameter.readDouble(getFormat(), parameters, MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER, defaultValueConfig);
        double maxGeneratorReactivePower = Parameter.readDouble(getFormat(), parameters, MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER, defaultValueConfig);

        MatpowerModel model = new MatpowerModel(network.getId());
        model.setBaseMva(BASE_MVA);
        model.setVersion(FORMAT_VERSION);

        Context context = new Context(maxGeneratorActivePower, maxGeneratorReactivePower);
        context.findSynchronousComponentsToBeExported(network);

        findSlackBusesForEachSynchronousComponent(context);

        context.num = preserveBusIds(network, context);
        createBuses(network, model, context);
        createBranches(network, model, context);
        findGenerators(network, context);
        findStaticVarCompensatorGenerators(network, context);
        findDanglingLineGenerators(network, context);
        createDcLines(network, model, context);

        createGeneratorsAndDefinePVBuses(model, context);

        if (!context.generatorIdsConvertedToLoad.isEmpty()) {
            LOGGER.debug("{} generators have been converted to a load: {}", context.generatorIdsConvertedToLoad.size(), context.generatorIdsConvertedToLoad);
        }

        try (OutputStream os = dataSource.newOutputStream(null, MatpowerConstants.EXT, false)) {
            MatpowerWriter.write(model, os, withBusNames);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("Matpower export of '{}' done", network.getId());
    }
}
