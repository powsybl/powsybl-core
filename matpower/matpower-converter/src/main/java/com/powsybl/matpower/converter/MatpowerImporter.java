/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.matpower.model.*;

import org.apache.commons.math3.complex.Complex;
import org.jgrapht.alg.util.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
@AutoService(Importer.class)
public class MatpowerImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerImporter.class);

    private static final String BUS_PREFIX = "BUS";
    private static final String GENERATOR_PREFIX = "GEN";
    private static final String LINE_PREFIX = "LINE";
    private static final String LOAD_PREFIX = "LOAD";
    private static final String SHUNT_PREFIX = "SHUNT";
    private static final String SUBSTATION_PREFIX = "SUB";
    private static final String TRANSFORMER_PREFIX = "TWT";
    private static final String VOLTAGE_LEVEL_PREFIX = "VL";

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("matpower.import.ignore-base-voltage",
            ParameterType.BOOLEAN,
            "Ignore base voltage specified in the file",
            Boolean.TRUE);

    private static final class Context {

        private final double baseMva; // base apparent power

        private final boolean ignoreBaseMva;

        private final List<Bus> slackBuses = new ArrayList<>();

        private Context(double baseMva, boolean ignoreBaseMva) {
            this.baseMva = baseMva;
            this.ignoreBaseMva = ignoreBaseMva;
        }

        private boolean isIgnoreBaseMva() {
            return ignoreBaseMva;
        }

        private double getBaseMva() {
            return baseMva;
        }

        private List<Bus> getSlackBuses() {
            return slackBuses;
        }
    }

    private static boolean isLine(MatpowerModel model, MBranch branch) {
        if (branch.getRatio() == 0) {
            return true;
        }
        MBus from = model.getBusByNum(branch.getFrom());
        MBus to = model.getBusByNum(branch.getTo());
        return branch.getRatio() == 1 && from.getBaseVoltage() == to.getBaseVoltage();
    }

    private static boolean isTransformer(MatpowerModel model, MBranch branch) {
        return !isLine(model, branch);
    }

    private static String getId(String prefix, int num) {
        return prefix + "-" + num;
    }

    private static String getId(String prefix, int from, int to) {
        return prefix + "-" + from + "-" + to;
    }

    private static void createBuses(MatpowerModel model, ContainersMapping containerMapping, Network network, Context context) {
        Map<String, Pair<Double, Double>> voltageLimitsByVoltageLevelId = new HashMap<>();
        for (MBus mBus : model.getBuses()) {
            String voltageLevelId = containerMapping.getVoltageLevelId(mBus.getNumber());
            String substationId = containerMapping.getSubstationId(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(mBus, voltageLevelId, substation, network, context);

            // create bus
            Bus bus = createBus(mBus, voltageLevel);
            if (mBus.getType() == MBus.Type.REF) {
                context.getSlackBuses().add(bus);
            }

            // create voltage limits
            createVoltageLimits(mBus, voltageLevel, voltageLimitsByVoltageLevelId);

            // create load
            createLoad(mBus, voltageLevel);

            // create shunt compensator
            createShuntCompensator(mBus, voltageLevel, context);

            //create generators
            createGenerators(model, mBus, voltageLevel);
        }

        // set voltage limits
        for (var e : voltageLimitsByVoltageLevelId.entrySet()) {
            String voltageLevelId = e.getKey();
            double lowVoltageLimit = e.getValue().getFirst();
            double highVoltageLimit = e.getValue().getSecond();
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (highVoltageLimit >= lowVoltageLimit) {
                voltageLevel.setLowVoltageLimit(lowVoltageLimit)
                        .setHighVoltageLimit(highVoltageLimit);
            } else {
                LOGGER.warn("Invalid voltage limits [{}, {}] for voltage level {}",
                        lowVoltageLimit, highVoltageLimit, voltageLevelId);
                voltageLevel.setLowVoltageLimit(highVoltageLimit)
                        .setHighVoltageLimit(lowVoltageLimit);
            }
        }
    }

    private static void createVoltageLimits(MBus mBus, VoltageLevel voltageLevel, Map<String, Pair<Double, Double>> voltageLimitsByVoltageLevelId) {
        // as in IIDM, we only have one min and one max voltage level by voltage level we keep only the most severe ones
        Pair<Double, Double> voltageLimits = voltageLimitsByVoltageLevelId.computeIfAbsent(voltageLevel.getId(), k -> Pair.of(Double.NaN, Double.NaN));
        if (mBus.getMinimumVoltageMagnitude() != 0) {
            double lowVoltageLimit = mBus.getMinimumVoltageMagnitude() * voltageLevel.getNominalV();
            if (Double.isNaN(voltageLimits.getFirst()) || lowVoltageLimit > voltageLimits.getFirst()) {
                voltageLimits.setFirst(lowVoltageLimit);
            }
        }
        if (mBus.getMaximumVoltageMagnitude() != 0) {
            double highVoltageLimit = mBus.getMaximumVoltageMagnitude() * voltageLevel.getNominalV();
            if (Double.isNaN(voltageLimits.getSecond()) || highVoltageLimit < voltageLimits.getSecond()) {
                voltageLimits.setSecond(highVoltageLimit);
            }
        }
    }

    private static void createGenerators(MatpowerModel model, MBus mBus, VoltageLevel voltageLevel) {
        for (MGen mGen : model.getGeneratorsByBusNum(mBus.getNumber())) {
            String busId = getId(BUS_PREFIX, mGen.getNumber());
            String genId = getId(GENERATOR_PREFIX, mGen.getNumber());
            Generator generator = voltageLevel.newGenerator()
                    .setId(genId)
                    .setEnsureIdUnicity(true)
                    .setConnectableBus(busId)
                    .setBus(isInService(mGen) ? busId : null)
                    .setTargetV(mGen.getVoltageMagnitudeSetpoint() * voltageLevel.getNominalV())
                    .setTargetP(mGen.getRealPowerOutput())
                    .setTargetQ(mGen.getReactivePowerOutput())
                    .setVoltageRegulatorOn(mGen.getVoltageMagnitudeSetpoint() != 0)
                    .setMaxP(mGen.getMaximumRealPowerOutput())
                    .setMinP(mGen.getMinimumRealPowerOutput())
                    .add();

            if ((mGen.getPc1() != 0) || (mGen.getPc2() != 0)) {
                generator.newReactiveCapabilityCurve()
                        .beginPoint()
                        .setP(mGen.getPc1())
                        .setMaxQ(mGen.getQc1Max())
                        .setMinQ(mGen.getQc1Min())
                        .endPoint()
                        .beginPoint()
                        .setP(mGen.getPc2())
                        .setMaxQ(mGen.getQc2Max())
                        .setMinQ(mGen.getQc2Min())
                        .endPoint()
                        .add();
            } else {
                generator.newMinMaxReactiveLimits()
                        .setMinQ(mGen.getMinimumReactivePowerOutput())
                        .setMaxQ(mGen.getMaximumReactivePowerOutput())
                        .add();
            }
            LOGGER.trace("Created generator {}", generator.getId());
        }
    }

    private static Bus createBus(MBus mBus, VoltageLevel voltageLevel) {
        String busId = getId(BUS_PREFIX, mBus.getNumber());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(mBus.getName())
                .add();
        bus.setV(mBus.getVoltageMagnitude() * voltageLevel.getNominalV())
                .setAngle(mBus.getVoltageAngle());
        LOGGER.trace("Created bus {}", bus.getId());
        return bus;
    }

    private static Substation createSubstation(Network network, String substationId) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            substation = network.newSubstation()
                    .setId(substationId)
                    .add();
            LOGGER.trace("Created substation {}", substation.getId());
        }
        return substation;
    }

    private static double getNominalV(MBus mBus, boolean ignoreBaseVoltage) {
        return ignoreBaseVoltage || mBus.getBaseVoltage() == 0 ? 1 : mBus.getBaseVoltage();
    }

    private static VoltageLevel createVoltageLevel(MBus mBus, String voltageLevelId, Substation substation, Network network, Context context) {
        double nominalV = getNominalV(mBus, context.isIgnoreBaseMva());
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            voltageLevel = substation.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(nominalV)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();
            LOGGER.trace("Created voltagelevel {}", voltageLevel.getId());
        }
        return voltageLevel;
    }

    private static void createLoad(MBus mBus, VoltageLevel voltageLevel) {
        if (mBus.getRealPowerDemand() != 0 || mBus.getReactivePowerDemand() != 0) {
            String busId = getId(BUS_PREFIX, mBus.getNumber());
            String loadId = getId(LOAD_PREFIX, mBus.getNumber());
            Load newLoad = voltageLevel.newLoad()
                .setId(loadId)
                .setConnectableBus(busId)
                .setBus(busId)
                .setP0(mBus.getRealPowerDemand())
                .setQ0(mBus.getReactivePowerDemand())
                .add();
            LOGGER.trace("Created load {}", newLoad.getId());
        }
    }

    private static void createShuntCompensator(MBus mBus, VoltageLevel voltageLevel, Context context) {
        if (mBus.getShuntSusceptance() != 0) {
            String busId = getId(BUS_PREFIX, mBus.getNumber());
            String shuntId = getId(SHUNT_PREFIX, mBus.getNumber());
            double zb = voltageLevel.getNominalV() * voltageLevel.getNominalV() / context.getBaseMva();
            ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
                    .setId(shuntId)
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setVoltageRegulatorOn(false)
                    .setSectionCount(1);
            adder.newLinearModel()
                    .setBPerSection(mBus.getShuntSusceptance() / context.getBaseMva() / zb)
                    .setMaximumSectionCount(1)
                    .add();
            ShuntCompensator newShunt = adder.add();
            LOGGER.trace("Created shunt {}", newShunt.getId());
        }
    }

    private static boolean isInService(MBranch branch) {
        return Math.abs(branch.getStatus()) > 0;
    }

    private static boolean isInService(MGen generator) {
        return generator.getStatus() > 0;
    }

    private static void createApparentPowerLimits(MBranch mBranch, ApparentPowerLimitsAdder limitsAdder) {
        limitsAdder.setPermanentLimit(mBranch.getRateA()); // long term rating
        if (mBranch.getRateB() != 0) {
            limitsAdder.beginTemporaryLimit()
                    .setName("RateB")
                    .setValue(mBranch.getRateB())
                    .setAcceptableDuration(60 * 20) // 20' for short term rating
                    .endTemporaryLimit();
        }
        if (mBranch.getRateC() != 0) {
            limitsAdder.beginTemporaryLimit()
                    .setName("RateC")
                    .setValue(mBranch.getRateC())
                    .setAcceptableDuration(60) // 1' for emergency rating
                    .endTemporaryLimit();
        }
        limitsAdder.add();
    }

    private static void createBranches(MatpowerModel model, ContainersMapping containerMapping, Network network, Context context) {
        for (MBranch mBranch : model.getBranches()) {

            String bus1Id = getId(BUS_PREFIX, mBranch.getFrom());
            String bus2Id = getId(BUS_PREFIX, mBranch.getTo());
            String voltageLevel1Id = containerMapping.getVoltageLevelId(mBranch.getFrom());
            String voltageLevel2Id = containerMapping.getVoltageLevelId(mBranch.getTo());
            VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
            VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
            double zb = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / context.getBaseMva();
            boolean isInService = isInService(mBranch);
            String connectedBus1 = isInService ? bus1Id : null;
            String connectedBus2 = isInService ? bus2Id : null;

            Branch<?> branch;
            if (isTransformer(model, mBranch)) {
                TwoWindingsTransformer newTwt = voltageLevel2.getSubstation().map(Substation::newTwoWindingsTransformer).orElseGet(network::newTwoWindingsTransformer)
                        .setId(getId(TRANSFORMER_PREFIX, mBranch.getFrom(), mBranch.getTo()))
                        .setEnsureIdUnicity(true)
                        .setBus1(connectedBus1)
                        .setConnectableBus1(bus1Id)
                        .setVoltageLevel1(voltageLevel1Id)
                        .setBus2(connectedBus2)
                        .setConnectableBus2(bus2Id)
                        .setVoltageLevel2(voltageLevel2Id)
                        .setRatedU1(voltageLevel1.getNominalV() * mBranch.getRatio())
                        .setRatedU2(voltageLevel2.getNominalV())
                        .setR(mBranch.getR() * zb)
                        .setX(mBranch.getX() * zb)
                        .setG(0)
                        .setB(mBranch.getB() / zb)
                        .add();
                if (mBranch.getPhaseShiftAngle() != 0) {
                    newTwt.newPhaseTapChanger()
                            .setTapPosition(0)
                            .beginStep()
                            .setRho(1)
                            .setAlpha(-mBranch.getPhaseShiftAngle())
                            .setR(0)
                            .setX(0)
                            .setG(0)
                            .setB(0)
                            .endStep()
                            .add();
                }
                branch = newTwt;
                LOGGER.trace("Created TwoWindingsTransformer {} {} {}", newTwt.getId(), bus1Id, bus2Id);
            } else {
                double nominalV1 = voltageLevel1.getNominalV();
                double nominalV2 = voltageLevel2.getNominalV();
                double sBase = context.getBaseMva();
                double r = impedanceToEngineeringUnitsForLine(mBranch.getR(), nominalV1, nominalV2, sBase);
                double x = impedanceToEngineeringUnitsForLine(mBranch.getX(), nominalV1, nominalV2, sBase);
                Complex ytr = impedanceToAdmittance(r, x);
                double g1 = admittanceEndToEngineeringUnitsForLine(ytr.getReal(), 0.0, nominalV1, nominalV2, sBase);
                double b1 = admittanceEndToEngineeringUnitsForLine(ytr.getImaginary(), mBranch.getB() * 0.5, nominalV1, nominalV2, sBase);
                double g2 = admittanceEndToEngineeringUnitsForLine(ytr.getReal(), 0.0, nominalV2, nominalV1, sBase);
                double b2 = admittanceEndToEngineeringUnitsForLine(ytr.getImaginary(), mBranch.getB() * 0.5, nominalV2, nominalV1, sBase);

                branch = network.newLine()
                        .setId(getId(LINE_PREFIX, mBranch.getFrom(), mBranch.getTo()))
                        .setEnsureIdUnicity(true)
                        .setBus1(connectedBus1)
                        .setConnectableBus1(bus1Id)
                        .setVoltageLevel1(voltageLevel1Id)
                        .setBus2(connectedBus2)
                        .setConnectableBus2(bus2Id)
                        .setVoltageLevel2(voltageLevel2Id)
                        .setR(r)
                        .setX(x)
                        .setG1(g1)
                        .setB1(b1)
                        .setG2(g2)
                        .setB2(b2)
                        .add();
                LOGGER.trace("Created line {} {} {}", branch.getId(), bus1Id, bus2Id);
            }
            if (mBranch.getRateA() != 0) {
                // we create the apparent power limit arbitrary on both sides
                // there is probably something to fix on IIDM API to not have sided apparent
                // power limits. Apparent power does not depend on voltage so it does not make
                // sens to associate the limit to a branch side.
                createApparentPowerLimits(mBranch, branch.newApparentPowerLimits1());
                createApparentPowerLimits(mBranch, branch.newApparentPowerLimits2());
            }
        }
    }

    // avoid NaN when r and x, both are 0.0
    private static Complex impedanceToAdmittance(double r, double x) {
        return r == 0.0 && x == 0.0 ? new Complex(0.0, 0.0) : new Complex(r, x).reciprocal();
    }

    private static double impedanceToEngineeringUnitsForLine(double impedance, double nominalVoltageAtEnd,
                                                             double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        return impedance * nominalVoltageAtEnd * nominalVoltageAtOtherEnd / sBase;
    }

    private static double admittanceEndToEngineeringUnitsForLine(double transmissionAdmittance, double shuntAdmittanceAtEnd,
                                                                 double nominalVoltageAtEnd, double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        // note that ytr is already in engineering units
        return shuntAdmittanceAtEnd * sBase / (nominalVoltageAtEnd * nominalVoltageAtEnd) - (1 - nominalVoltageAtOtherEnd / nominalVoltageAtEnd) * transmissionAdmittance;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource.exists(null, MatpowerConstants.EXT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getComment() {
        return "MATPOWER Format to IIDM converter";
    }

    @Override
    public String getFormat() {
        return MatpowerConstants.FORMAT;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            try (InputStream is = fromDataSource.newInputStream(null, MatpowerConstants.EXT);
                 OutputStream os = toDataSource.newOutputStream(null, MatpowerConstants.EXT, false)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(IGNORE_BASE_VOLTAGE_PARAMETER);
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);
        Network network = networkFactory.createNetwork(dataSource.getBaseName(), MatpowerConstants.FORMAT);

        //there is no time & date declared in the MATPOWER file: set a default now()
        network.setCaseDate(DateTime.now());

        try {
            try (InputStream iStream = dataSource.newInputStream(null, MatpowerConstants.EXT)) {

                MatpowerModel model = MatpowerReader.read(iStream, dataSource.getBaseName());
                LOGGER.debug("MATPOWER model '{}'", model.getCaseName());

                boolean ignoreBaseVoltage = Parameter.readBoolean(MatpowerConstants.FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                    ParameterDefaultValueConfig.INSTANCE);

                Map<Integer, MBus> busNumToMBus = model.getBuses().stream().collect(Collectors.toMap(MBus::getNumber, Function.identity()));

                ContainersMapping containerMapping = ContainersMapping.create(model.getBuses(), model.getBranches(),
                    MBus::getNumber,
                    MBranch::getFrom,
                    MBranch::getTo,
                    branch -> branch.getR() == 0.0 && branch.getX() == 0.0,
                    branch -> isTransformer(model, branch),
                    busNumber -> getNominalVFromBusNumber(busNumToMBus, busNumber, ignoreBaseVoltage),
                    busNums -> getId(VOLTAGE_LEVEL_PREFIX, busNums.stream().sorted().findFirst().orElseThrow(() -> new PowsyblException("Unexpected empty busNums"))),
                    substationNums -> getId(SUBSTATION_PREFIX, substationNums.stream().sorted().findFirst().orElseThrow(() -> new PowsyblException("Unexpected empty substationNums"))));

                Context context = new Context(model.getBaseMva(), ignoreBaseVoltage);

                createBuses(model, containerMapping, network, context);

                createBranches(model, containerMapping, network, context);

                for (Bus slackBus : context.getSlackBuses()) {
                    SlackTerminal.attach(slackBus);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }

    private double getNominalVFromBusNumber(Map<Integer, MBus> busNumToMBus, int busNumber, boolean ignoreBaseVoltage) {
        if (!busNumToMBus.containsKey(busNumber)) { // never should happen
            throw new PowsyblException("busId without MBus" + busNumber);
        }
        return getNominalV(busNumToMBus.get(busNumber), ignoreBaseVoltage);
    }
}
