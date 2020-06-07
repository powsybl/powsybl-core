/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
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
import com.powsybl.psse.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
@AutoService(Importer.class)
public class PsseImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseImporter.class);

    private static final String FORMAT = "PSS/E";

    private static final String EXT = "raw"; //TO DO: support both RAW and raw extensions

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("ignore-base-voltage",
            ParameterType.BOOLEAN,
            "Ignore base voltage specified in the file",
            Boolean.TRUE);

    private static final double DEFAULT_ACTIVE_POWER_LIMIT = 9999d;

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(IGNORE_BASE_VOLTAGE_PARAMETER);
    }

    @Override
    public String getComment() {
        return "PSS/E Format to IIDM converter";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            if (dataSource.exists(null, EXT)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
                    String titleLine = reader.readLine();
                    if (titleLine != null) {
                        return titleLine.length() >= 38
                                && titleLine.charAt(37) == '/';
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return false;
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

    private static final class PerUnitContext {

        private final double sb; // base apparent power

        private final boolean ignoreBaseVoltage;

        private PerUnitContext(double sb, boolean ignoreBaseVoltage) {
            this.sb = sb;
            this.ignoreBaseVoltage = ignoreBaseVoltage;
        }

        private double getSb() {
            return sb;
        }

        public boolean isIgnoreBaseVoltage() {
            return ignoreBaseVoltage;
        }
    }

    private static String getBusId(int busNum) {
        return "B" + busNum;
    }

    private static Bus createBus(PsseBus psseBus, VoltageLevel voltageLevel) {
        String busId = getBusId(psseBus.getI());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(psseBus.getName())
                .add();
        bus.setV(psseBus.getVm() * voltageLevel.getNominalV())
                .setAngle(psseBus.getVa());

        return bus;
    }

    private static Substation createSubstation(Network network, String substationId) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            substation = network.newSubstation()
                    .setId(substationId)
                    .add();
        }
        return substation;
    }

    private static VoltageLevel createVoltageLevel(PsseBus psseBus, PerUnitContext perUnitContext,
                                                   String voltageLevelId, Substation substation, Network network) {
        double nominalV = perUnitContext.isIgnoreBaseVoltage() || psseBus.getBaskv() == 0 ? 1 : psseBus.getBaskv();
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            voltageLevel = substation.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(nominalV)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();
        }
        return voltageLevel;
    }

    private static void createLoad(PsseLoad psseLoad, VoltageLevel voltageLevel) {
        String busId = getBusId(psseLoad.getI());
        voltageLevel.newLoad()
                .setId(busId + "-L" + psseLoad.getId())
                .setConnectableBus(busId)
                .setBus(busId)
                .setP0(psseLoad.getPl()) //TO DO: take into account Ip, Yp
                .setQ0(psseLoad.getQl()) //TO DO: take into account Iq, Yq
                .add();

    }

    private static void createShuntCompensator(PsseFixedShunt psseShunt, PerUnitContext perUnitContext, VoltageLevel voltageLevel) {
        String busId = getBusId(psseShunt.getI());
        double zb = Math.pow(voltageLevel.getNominalV(), 2) / perUnitContext.getSb();
        voltageLevel.newShuntCompensator()
                .setId(busId + "-SH" + psseShunt.getId())
                .setConnectableBus(busId)
                .setBus(busId)
                .setbPerSection(psseShunt.getBl())//TO DO: take into account gl
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();

        if (psseShunt.getGl() != 0) {
            LOGGER.warn("Shunt Gl not supported ({})", psseShunt.getI());
        }
    }

    private static void createGenerator(PsseGenerator psseGen, VoltageLevel voltageLevel) {
        String busId = getBusId(psseGen.getI());
        Generator generator =  voltageLevel.newGenerator()
                .setId(busId + "-G" + psseGen.getId())
                .setConnectableBus(busId)
                .setBus(busId)
                .setTargetP(psseGen.getPg())
                .setMaxP(DEFAULT_ACTIVE_POWER_LIMIT)
                .setMinP(-DEFAULT_ACTIVE_POWER_LIMIT)
                .setVoltageRegulatorOn(false)
                .setTargetQ(psseGen.getQg())
                .add();

        if (psseGen.getVs() > 0) {
            if (psseGen.getIreg() == 0) {
                //PV group
                generator.setTargetV(psseGen.getVs() * voltageLevel.getNominalV());
                generator.setVoltageRegulatorOn(true);
            } else {
                //TO DO : implement remote voltage control regulation
                LOGGER.warn("Remote Voltage control not supported ({})", generator.getId());
            }
        }
        //TO DO: take into account zr zx Mbase...
    }

    private static void createBuses(PsseRawModel psseModel, ContainersMapping containerMapping, PerUnitContext perUnitContext,
                                    Network network) {
        for (PsseBus psseBus : psseModel.getBuses()) {
            String voltageLevelId = containerMapping.getVoltageLevelId(psseBus.getI());
            String substationId = containerMapping.getSubstationId(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(psseBus, perUnitContext, voltageLevelId, substation, network);

            // create bus
            createBus(psseBus, voltageLevel);

        }
    }

    private static void createLine(PsseNonTransformerBranch psseLine, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = "L-" + psseLine.getI() + "-" + psseLine.getJ() + "-" + psseLine.getCkt();
        String bus1Id = getBusId(psseLine.getI());
        String bus2Id = getBusId(psseLine.getJ());
        String voltageLevel1Id = containerMapping.getVoltageLevelId(psseLine.getI());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(psseLine.getJ());
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double zb = Math.pow(voltageLevel2.getNominalV(), 2) / perUnitContext.getSb();
        network.newLine()
                .setId(id)
                .setBus1(bus1Id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevel1Id)
                .setBus2(bus2Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setR(psseLine.getR() * zb)
                .setX(psseLine.getX() * zb)
                .setG1(0) //TO DO
                .setB1(psseLine.getB() / zb / 2)
                .setG2(0) //TO DO
                .setB2(psseLine.getB() / zb / 2)
                .add();

        if (psseLine.getGi() != 0 || psseLine.getGj() != 0) {
            LOGGER.warn("Branch G not supported ({})", psseLine.getI());
        }
    }

    private static void createTransformer(PsseTransformer psseTfo, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = "T-" + psseTfo.getFirstRecord().getI() + "-" + psseTfo.getFirstRecord().getJ() + "-" + psseTfo.getFirstRecord().getCkt();
        String bus1Id = getBusId(psseTfo.getFirstRecord().getI());
        String bus2Id = getBusId(psseTfo.getFirstRecord().getJ());
        String voltageLevel1Id = containerMapping.getVoltageLevelId(psseTfo.getFirstRecord().getI());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(psseTfo.getFirstRecord().getJ());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double zb = Math.pow(voltageLevel2.getNominalV(), 2) / perUnitContext.getSb();

        if (psseTfo.getFirstRecord().getNmetr() == 2) {
            voltageLevel2.getSubstation().newTwoWindingsTransformer()
                    .setId(id)
                    .setBus1(bus1Id)
                    .setConnectableBus1(bus1Id)
                    .setVoltageLevel1(voltageLevel1Id)
                    .setBus2(bus2Id)
                    .setConnectableBus2(bus2Id)
                    .setVoltageLevel2(voltageLevel2Id)
                    .setRatedU1(voltageLevel1.getNominalV() * psseTfo.getThirdRecord1().getWindv())
                    .setRatedU2(voltageLevel2.getNominalV())
                    .setR(psseTfo.getSecondRecord().getR12() * zb)
                    .setX(psseTfo.getSecondRecord().getX12() * zb)
                    .setG(0) //TO DO
                    .setB(0) //TO DO
                    .add();

        } else {
            LOGGER.warn("Non-2-windings transformers not supported ({})", id);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {

            // parse file
            PsseRawModel psseModel = new PsseRawReader().read(reader);

            // set date and time
            // TO DO

            // build container to fit IIDM requirements
            List<Object> branches = ImmutableList.builder()
                    .addAll(psseModel.getNonTransformerBranches())
                    .addAll(psseModel.getTransformers())
                    .build();
            ToIntFunction<Object> branchToNum1 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getI() : ((PsseTransformer) branch).getFirstRecord().getI();
            ToIntFunction<Object> branchToNum2 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getJ() : ((PsseTransformer) branch).getFirstRecord().getJ();
            ToDoubleFunction<Object> branchToResistance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getR() : ((PsseTransformer) branch).getSecondRecord().getR12();
            ToDoubleFunction<Object> branchToReactance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getX() : ((PsseTransformer) branch).getSecondRecord().getX12();
            Predicate<Object> branchToIsTransformer = branch -> branch instanceof PsseTransformer;
            ContainersMapping containerMapping = ContainersMapping.create(psseModel.getBuses(), branches, PsseBus::getI, branchToNum1,
                branchToNum2, branchToResistance, branchToReactance, branchToIsTransformer,
                busNums -> "VL" + busNums.iterator().next(), substationNum -> "S" + substationNum++);

            boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                    ParameterDefaultValueConfig.INSTANCE);
            PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(), ignoreBaseVoltage);

            // create buses
            createBuses(psseModel, containerMapping, perUnitContext, network);

            //Create loads
            for (PsseLoad psseLoad : psseModel.getLoads()) {
                createLoad(psseLoad, network.getVoltageLevel(containerMapping.getVoltageLevelId(psseLoad.getI())));
            }

            //Create shunts
            for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
                createShuntCompensator(psseShunt, perUnitContext, network.getVoltageLevel(containerMapping.getVoltageLevelId(psseShunt.getI())));
            }

            for (PsseGenerator psseGen : psseModel.getGenerators()) {
                createGenerator(psseGen, network.getVoltageLevel(containerMapping.getVoltageLevelId(psseGen.getI())));
            }

            for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
                createLine(psseLine, containerMapping, perUnitContext, network);
            }

            for (PsseTransformer psseTfo : psseModel.getTransformers()) {
                createTransformer(psseTfo, containerMapping, perUnitContext, network);
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }

}
