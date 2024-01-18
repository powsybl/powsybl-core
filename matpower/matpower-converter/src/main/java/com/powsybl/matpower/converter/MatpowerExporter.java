/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.HvdcUtils;
import com.powsybl.matpower.model.*;
import org.apache.commons.math3.complex.Complex;
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
        return PARAMETERS;
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
        if (context.refBusId != null && context.refBusId.equals(bus.getId())
                || hasSlackExtension(bus)) {
            return MBus.Type.REF;
        }
        for (Generator g : bus.getGenerators()) {
            if (g.isVoltageRegulatorOn()) {
                return MBus.Type.PV;
            }
        }
        return MBus.Type.PQ;
    }

    static class Context {

        private final double maxGeneratorActivePowerLimit;

        private final double maxGeneratorReactivePowerLimit;

        String refBusId;

        int num = 1;

        final Map<String, Integer> mBusesNumbersByIds = new HashMap<>();

        final List<String> generatorIdsConvertedToLoad = new ArrayList<>();

        public Context(double maxGeneratorActivePowerLimit, double maxGeneratorReactivePowerLimit) {
            this.maxGeneratorActivePowerLimit = maxGeneratorActivePowerLimit;
            this.maxGeneratorReactivePowerLimit = maxGeneratorReactivePowerLimit;
        }
    }

    private static boolean isExported(Bus bus) {
        // TODO replace by isInMainConnectedComponent when supporting HVDC lines
        // in the meantime, we should only get main synchronous component to avoid
        // multiple components, that are not supported by Matpower power flow
        return bus != null && bus.isInMainSynchronousComponent();
    }

    private static void createTransformerStarBuses(Network network, MatpowerModel model, Context context) {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            Bus bus1 = twt.getLeg1().getTerminal().getBusView().getBus();
            Bus bus2 = twt.getLeg2().getTerminal().getBusView().getBus();
            Bus bus3 = twt.getLeg3().getTerminal().getBusView().getBus();
            if (isExported(bus1) && isExported(bus2) && isExported(bus3)) {
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
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
                mBus.setVoltageMagnitude(twt.hasProperty(V_PROP) ? Double.parseDouble(twt.getProperty(V_PROP)) / twt.getRatedU0() : 1d);
                mBus.setVoltageAngle(twt.hasProperty(ANGLE_PROP) ? Double.parseDouble(twt.getProperty(ANGLE_PROP)) : 0d);
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(twt.getId(), mBus.getNumber());
            }
        }
    }

    private static void createDanglingLineBuses(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
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
                mBus.setVoltageMagnitude(dl.getBoundary().getV() / vl.getNominalV());
                mBus.setVoltageAngle(dl.getBoundary().getAngle());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(dl.getId(), mBus.getNumber());
            }
        }
    }

    private static void createBuses(Network network, MatpowerModel model, Context context) {
        for (Bus bus : network.getBusView().getBuses()) {
            if (isExported(bus)) {
                VoltageLevel vl = bus.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
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
                mBus.setVoltageMagnitude(Double.isNaN(bus.getV()) ? 1 : bus.getV() / vl.getNominalV());
                mBus.setVoltageAngle(Double.isNaN(bus.getAngle()) ? 0 : bus.getAngle());
                mBus.setMinimumVoltageMagnitude(Double.isNaN(vl.getLowVoltageLimit()) ? 0 : vl.getLowVoltageLimit() / vl.getNominalV());
                mBus.setMaximumVoltageMagnitude(Double.isNaN(vl.getHighVoltageLimit()) ? 0 : vl.getHighVoltageLimit() / vl.getNominalV());
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(bus.getId(), mBus.getNumber());
            }
        }

        createDanglingLineBuses(network, model, context);
        createTransformerStarBuses(network, model, context);
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
        limitsHolders.stream().flatMap(limitsHolder -> Stream.concat(limitsHolder.getApparentPowerLimits().stream(), // apparrent power limits first then current limits
                                                                     limitsHolder.getCurrentLimits().stream()))
                .filter(limits -> !Double.isNaN(limits.getPermanentLimit())) // skip when there is no permanent
                .max(Comparator.comparingInt(loadingLimit -> loadingLimit.getTemporaryLimits().size())) // many tempary limits first
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
            Terminal t1 = twt.getTerminal1();
            Terminal t2 = twt.getTerminal2();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            if (isExported(bus1) && isExported(bus2)) {
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
        if (isExported(bus1) && isExported(bus2)) {
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
            if (isExported(bus)) {
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
            if (isExported(bus1) && isExported(bus2) && isExported(bus3)) {
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

    private void createDanglingLineGenerators(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus)) {
                var g = dl.getGeneration();
                if (g != null) {
                    VoltageLevel vl = t.getVoltageLevel();
                    MGen mGen = new MGen();
                    mGen.setNumber(context.mBusesNumbersByIds.get(dl.getId()));
                    mGen.setStatus(CONNECTED_STATUS);
                    mGen.setRealPowerOutput(g.getTargetP());
                    mGen.setReactivePowerOutput(g.getTargetQ());
                    mGen.setVoltageMagnitudeSetpoint(g.isVoltageRegulationOn() ? g.getTargetV() / vl.getNominalV() : 0);
                    mGen.setMinimumRealPowerOutput(Math.max(g.getMinP(), -context.maxGeneratorActivePowerLimit));
                    mGen.setMaximumRealPowerOutput(Math.min(g.getMaxP(), context.maxGeneratorActivePowerLimit));
                    mGen.setMinimumReactivePowerOutput(Math.max(g.getReactiveLimits().getMinQ(g.getTargetP()), -context.maxGeneratorReactivePowerLimit));
                    mGen.setMaximumReactivePowerOutput(Math.min(g.getReactiveLimits().getMaxQ(g.getTargetP()), context.maxGeneratorReactivePowerLimit));
                    model.addGenerator(mGen);
                }
            }
        }
    }

    private void createGenerators(Network network, MatpowerModel model, Context context) {
        for (Generator g : network.getGenerators()) {
            Terminal t = g.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                String id = g.getId();
                double targetP = g.getTargetP();
                double targetQ = g.getTargetQ();
                double targetV = g.getTargetV();
                double minP = g.getMinP();
                double maxP = g.getMaxP();
                double maxQ = g.getReactiveLimits().getMaxQ(g.getTargetP());
                double minQ = g.getReactiveLimits().getMinQ(g.getTargetP());
                Bus regulatedBus = g.getRegulatingTerminal().getBusView().getBus();
                boolean voltageRegulation = g.isVoltageRegulatorOn();
                double ratedS = g.getRatedS();
                addMgen(model, context, bus, vl, id, targetV, targetP, minP, maxP, targetQ, Math.min(minQ, maxQ), Math.max(minQ, maxQ), regulatedBus,
                        voltageRegulation, ratedS);
            }
        }

        createDanglingLineGenerators(network, model, context);
    }

    private void createStaticVarCompensators(Network network, MatpowerModel model, Context context) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            Terminal t = svc.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
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
                double targetV = svc.getVoltageSetpoint();
                Bus regulatedBus = svc.getRegulatingTerminal().getBusView().getBus();
                boolean voltageRegulation = StaticVarCompensator.RegulationMode.VOLTAGE.equals(svc.getRegulationMode());
                addMgen(model, context, bus, vl, id, targetV, 0, 0, 0, targetQ, minQ,
                        maxQ, regulatedBus, voltageRegulation, Double.NaN);
            }
        }
        createDanglingLineGenerators(network, model, context);
    }

    private void createVSCs(Network network, MatpowerModel model, Context context) {
        for (VscConverterStation vsc : network.getVscConverterStations()) {
            Terminal t = vsc.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isExported(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                String id = vsc.getId();
                double targetQ = vsc.getReactivePowerSetpoint();
                double targetV = vsc.getVoltageSetpoint();
                Bus regulatedBus = vsc.getRegulatingTerminal().getBusView().getBus();
                double targetP = HvdcUtils.getConverterStationTargetP(vsc);
                double minQ = vsc.getReactiveLimits().getMinQ(targetP); // approximation
                double maxQ = vsc.getReactiveLimits().getMaxQ(targetP); // approximation
                boolean voltageRegulation = vsc.isVoltageRegulatorOn();
                double maxP = vsc.getHvdcLine() != null ? vsc.getHvdcLine().getMaxP() : Double.MAX_VALUE;
                addMgen(model, context, bus, vl, id, targetV, targetP, -maxP, maxP, targetQ, minQ,
                        maxQ, regulatedBus, voltageRegulation, Double.NaN);
            }
        }
        createDanglingLineGenerators(network, model, context);
    }

    private static void addMgen(MatpowerModel model, Context context, Bus bus, VoltageLevel vl,
                                String id, double targetV, double targetP, double minP, double maxP, double targetQ,
                                double minQ, double maxQ, Bus regulatedBus, boolean voltageRegulation, double ratedS) {
        int busNum = context.mBusesNumbersByIds.get(bus.getId());
        MBus mBus = model.getBusByNum(busNum);
        boolean validVoltageRegulation = voltageRegulation && regulatedBus != null;
        // Matpower power flow does not support bus with multiple generators that do not have the same voltage regulation
        // status. if the bus has PV type, all of its generator must have a valid voltage set point.
        if (!validVoltageRegulation && mBus.getType() == MBus.Type.PV) {
            // convert to load
            mBus.setRealPowerDemand(mBus.getRealPowerDemand() - targetP);
            mBus.setReactivePowerDemand(mBus.getReactivePowerDemand() - targetQ);
            context.generatorIdsConvertedToLoad.add(id);
        } else {
            MGen mGen = new MGen();
            mGen.setNumber(busNum);
            mGen.setStatus(CONNECTED_STATUS);
            mGen.setRealPowerOutput(targetP);
            mGen.setReactivePowerOutput(Double.isNaN(targetQ) ? 0 : targetQ);
            if (validVoltageRegulation) {
                double targetVpu = targetV / vl.getNominalV();
                if (!regulatedBus.getId().equals(bus.getId())) {
                    double oldTargetV = targetVpu;
                    targetVpu *= vl.getNominalV() / regulatedBus.getVoltageLevel().getNominalV();
                    LOGGER.warn(
                            "Generator remote voltage control not supported in Matpower model, rescale targetV of '{}' from {} to {}",
                            id, oldTargetV, targetVpu);
                }
                mGen.setVoltageMagnitudeSetpoint(targetVpu);
            } else {
                // we can safely set voltage setpoint to zero, because a PQ bus never go back to PV even if reactive limits
                // are activated in Matpower power flow
                mGen.setVoltageMagnitudeSetpoint(0);
            }
            mGen.setMinimumRealPowerOutput(Math.max(minP, -context.maxGeneratorActivePowerLimit));
            mGen.setMaximumRealPowerOutput(Math.min(maxP, context.maxGeneratorActivePowerLimit));
            mGen.setMinimumReactivePowerOutput(Math.max(minQ, -context.maxGeneratorReactivePowerLimit));
            mGen.setMaximumReactivePowerOutput(Math.min(maxQ, context.maxGeneratorReactivePowerLimit));
            mGen.setTotalMbase(Double.isNaN(ratedS) ? 0 : ratedS);
            model.addGenerator(mGen);
        }
    }

    private static int getBranchCount(Bus bus) {
        int[] branchCount = new int[1];
        bus.visitConnectedEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitLine(Line line, TwoSides side) {
                branchCount[0]++;
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                branchCount[0]++;
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
                branchCount[0]++;
            }

            @Override
            public void visitDanglingLine(DanglingLine danglingLine) {
                branchCount[0]++;
            }
        });
        return branchCount[0];
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, Reporter reporter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reporter);

        boolean withBusNames = Parameter.readBoolean(getFormat(), parameters, WITH_BUS_NAMES_PARAMETER, defaultValueConfig);
        double maxGeneratorActivePower = Parameter.readDouble(getFormat(), parameters, MAX_GENERATOR_ACTIVE_POWER_LIMIT_PARAMETER, defaultValueConfig);
        double maxGeneratorReactivePower = Parameter.readDouble(getFormat(), parameters, MAX_GENERATOR_REACTIVE_POWER_LIMIT_PARAMETER, defaultValueConfig);

        MatpowerModel model = new MatpowerModel(network.getId());
        model.setBaseMva(BASE_MVA);
        model.setVersion(FORMAT_VERSION);

        Context context = new Context(maxGeneratorActivePower, maxGeneratorReactivePower);
        boolean hasSlack = network.getBusView().getBusStream().anyMatch(MatpowerExporter::hasSlackExtension);
        if (!hasSlack) {
            context.refBusId = network.getBusView().getBusStream()
                    .filter(MatpowerExporter::isExported)
                    .max(Comparator.comparingInt(MatpowerExporter::getBranchCount))
                    .orElseThrow()
                    .getId();
            LOGGER.debug("Matpower reference bus automatically selected: {}", context.refBusId);
        }
        createBuses(network, model, context);
        createBranches(network, model, context);
        createGenerators(network, model, context);
        createStaticVarCompensators(network, model, context);
        createVSCs(network, model, context);

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
