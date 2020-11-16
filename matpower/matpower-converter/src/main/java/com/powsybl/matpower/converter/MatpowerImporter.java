/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.matpower.model.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
@AutoService(Importer.class)
public class MatpowerImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerImporter.class);

    private static final String FORMAT = "MATPOWER";

    private static final String EXT = "mat";

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

    private static final class PerUnitContext {

        private final double baseMva; // base apparent power

        private final boolean ignoreBaseMva;

        private PerUnitContext(double baseMva, boolean ignoreBaseMva) {
            this.baseMva = baseMva;
            this.ignoreBaseMva = ignoreBaseMva;
        }

        public boolean isIgnoreBaseMva() {
            return ignoreBaseMva;
        }

        private double getBaseMva() {
            return baseMva;
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

    private static void createBuses(MatpowerModel model, ContainersMapping containerMapping, Network network, PerUnitContext perUnitContext) {
        for (MBus mBus : model.getBuses()) {
            String voltageLevelId = containerMapping.getVoltageLevelId(mBus.getNumber());
            String substationId = containerMapping.getSubstationId(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(mBus, voltageLevelId, substation, network, perUnitContext);

            // create bus
            createBus(mBus, voltageLevel);

            // create load
            createLoad(mBus, voltageLevel);

            // create shunt compensator
            createShuntCompensator(mBus, voltageLevel, perUnitContext);

            //create generators
            createGenerators(model, mBus, voltageLevel);
        }
    }

    private static void createGenerators(MatpowerModel model, MBus mBus, VoltageLevel voltageLevel) {
        model.getGenerators().stream().filter(gen -> gen.getNumber() == mBus.getNumber()).forEach(mGen -> {
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
            LOGGER.debug("Created generator {}", generator.getId());
        });
    }

    private static Bus createBus(MBus mBus, VoltageLevel voltageLevel) {
        String busId = getId(BUS_PREFIX, mBus.getNumber());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .add();
        bus.setV(mBus.getVoltageMagnitude() * voltageLevel.getNominalV())
                .setAngle(mBus.getVoltageAngle());
        LOGGER.debug("Created bus {}", bus.getId());
        return bus;
    }

    private static Substation createSubstation(Network network, String substationId) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            substation = network.newSubstation()
                    .setId(substationId)
                    .add();
            LOGGER.debug("Created substation {}", substation.getId());
        }
        return substation;
    }

    private static VoltageLevel createVoltageLevel(MBus mBus, String voltageLevelId, Substation substation, Network network, PerUnitContext perUnitContext) {
        double nominalV = perUnitContext.isIgnoreBaseMva() || mBus.getBaseVoltage() == 0 ? 1 : mBus.getBaseVoltage();
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            voltageLevel = substation.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(nominalV)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();
            LOGGER.debug("Created voltagelevel {}", voltageLevel.getId());
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
            LOGGER.debug("Created load {}", newLoad.getId());
        }
    }

    private static void createShuntCompensator(MBus mBus, VoltageLevel voltageLevel, PerUnitContext perUnitContext) {
        if (mBus.getShuntSusceptance() != 0) {
            String busId = getId(BUS_PREFIX, mBus.getNumber());
            String shuntId = getId(SHUNT_PREFIX, mBus.getNumber());
            double zb = voltageLevel.getNominalV() * voltageLevel.getNominalV() / perUnitContext.getBaseMva();
            ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
                    .setId(shuntId)
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setSectionCount(1);
            adder.newLinearModel()
                    .setBPerSection(mBus.getShuntSusceptance() / perUnitContext.getBaseMva() / zb)
                    .setMaximumSectionCount(1)
                    .add();
            ShuntCompensator newShunt = adder.add();
            LOGGER.debug("Created shunt {}", newShunt.getId());
        }
    }

    private static boolean isInService(MBranch branch) {
        return Math.abs(branch.getStatus()) > 0;
    }

    private static boolean isInService(MGen generator) {
        return generator.getStatus() > 0;
    }

    private static void createBranches(MatpowerModel model, ContainersMapping containerMapping, Network network, PerUnitContext perUnitContext) {
        for (MBranch mBranch : model.getBranches()) {

            String bus1Id = getId(BUS_PREFIX, mBranch.getFrom());
            String bus2Id = getId(BUS_PREFIX, mBranch.getTo());
            String voltageLevel1Id = containerMapping.getVoltageLevelId(mBranch.getFrom());
            String voltageLevel2Id = containerMapping.getVoltageLevelId(mBranch.getTo());
            VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
            VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
            double zb = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / perUnitContext.getBaseMva();
            boolean isInService = isInService(mBranch);
            String connectedBus1 = isInService ? bus1Id : null;
            String connectedBus2 = isInService ? bus2Id : null;

            if (isTransformer(model, mBranch)) {
                TwoWindingsTransformer newTwt = voltageLevel2.getSubstation().newTwoWindingsTransformer()
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
                LOGGER.debug("Created TwoWindingsTransformer {} {} {}", newTwt.getId(), bus1Id, bus2Id);
            } else {
                Line newLine = network.newLine()
                        .setId(getId(LINE_PREFIX, mBranch.getFrom(), mBranch.getTo()))
                        .setEnsureIdUnicity(true)
                        .setBus1(connectedBus1)
                        .setConnectableBus1(bus1Id)
                        .setVoltageLevel1(voltageLevel1Id)
                        .setBus2(connectedBus2)
                        .setConnectableBus2(bus2Id)
                        .setVoltageLevel2(voltageLevel2Id)
                        .setR(mBranch.getR() * zb)
                        .setX(mBranch.getX() * zb)
                        .setG1(0)
                        .setB1(mBranch.getB() / zb / 2)
                        .setG2(0)
                        .setB2(mBranch.getB() / zb / 2)
                        .add();
                LOGGER.debug("Created line {} {} {}", newLine.getId(), bus1Id, bus2Id);
            }
        }
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource.exists(null, EXT);
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
        return FORMAT;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            try (InputStream is = fromDataSource.newInputStream(null, EXT);
                 OutputStream os = toDataSource.newOutputStream(null, EXT, false)) {
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
        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        //there is no time & date declared in the MATPOWER file: set a default now()
        network.setCaseDate(DateTime.now());

        try {
            try (InputStream iStream = dataSource.newInputStream(null, EXT)) {

                MatpowerModel model = MatpowerReader.read(iStream, dataSource.getBaseName());
                LOGGER.debug("MATPOWER model {}", model.getCaseName());

                ContainersMapping containerMapping = ContainersMapping.create(model.getBuses(), model.getBranches(),
                    MBus::getNumber, MBranch::getFrom, MBranch::getTo, branch -> 0, MBranch::getR, MBranch::getX, branch -> isTransformer(model, branch),
                    busNums -> getId(VOLTAGE_LEVEL_PREFIX, busNums.iterator().next()), substationNum -> getId(SUBSTATION_PREFIX, substationNum));

                boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                        ParameterDefaultValueConfig.INSTANCE);

                PerUnitContext perUnitContext = new PerUnitContext(model.getBaseMva(), ignoreBaseVoltage);

                createBuses(model, containerMapping, network, perUnitContext);

                createBranches(model, containerMapping, network, perUnitContext);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }
}
