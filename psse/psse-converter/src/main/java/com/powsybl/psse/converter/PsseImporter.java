/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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

    private static final String[] EXTS = {"raw", "RAW"};

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

    private String findExtension(ReadOnlyDataSource dataSource, boolean throwException) throws IOException {
        for (String ext : EXTS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        if (throwException) {
            throw new PsseException("File " + dataSource.getBaseName()
                    + "." + String.join("|", EXTS) + " not found");
        }
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource, false);
            if (ext != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
                    return new PsseRawReader().checkCaseIdentification(reader);
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
            String ext = findExtension(fromDataSource, false);
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
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

    private static void createLoad(PsseLoad psseLoad, ContainersMapping containerMapping, Network network) {
        String busId = getBusId(psseLoad.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseLoad.getI()));
        Load load = voltageLevel.newLoad()
                .setId(busId + "-L" + psseLoad.getId())
                .setConnectableBus(busId)
                .setP0(psseLoad.getPl()) //TODO: take into account Ip, Yp
                .setQ0(psseLoad.getQl()) //TODO: take into account Iq, Yq
                .add();

        if (psseLoad.getStatus() == 1) {
            load.getTerminal().connect();
        }

    }

    private static void createShuntCompensator(PsseFixedShunt psseShunt, PerUnitContext perUnitContext, ContainersMapping containerMapping, Network network) {
        String busId = getBusId(psseShunt.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseShunt.getI()));
        ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                .setId(busId + "-SH" + psseShunt.getId())
                .setConnectableBus(busId)
                .setbPerSection(psseShunt.getBl())//TODO: take into account gl
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();

        if (psseShunt.getStatus() == 1) {
            shunt.getTerminal().connect();
        }

        if (psseShunt.getGl() != 0) {
            LOGGER.warn("Shunt Gl not supported ({})", psseShunt.getI());
        }
    }

    private static void createGenerator(PsseGenerator psseGen, ContainersMapping containerMapping, Network network) {
        String busId = getBusId(psseGen.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseGen.getI()));
        Generator generator =  voltageLevel.newGenerator()
                .setId(busId + "-G" + psseGen.getId())
                .setConnectableBus(busId)
                .setTargetP(psseGen.getPg())
                .setMaxP(psseGen.getPt())
                .setMinP(psseGen.getPb())
                .setVoltageRegulatorOn(false)
                .setTargetQ(psseGen.getQt())
                .add();

        generator.newMinMaxReactiveLimits()
                .setMinQ(psseGen.getQb())
                .setMaxQ(psseGen.getQt())
                .add();

        if (psseGen.getStat() == 1) {
            generator.getTerminal().connect();
        }

        if (psseGen.getVs() > 0) {
            if (psseGen.getIreg() == 0) {
                //PV group
                generator.setTargetV(psseGen.getVs() * voltageLevel.getNominalV());
                generator.setVoltageRegulatorOn(true);
                generator.setTargetQ(psseGen.getQg());
            } else {
                //TODO : implement remote voltage control regulation
                LOGGER.warn("Remote Voltage control not supported ({})", generator.getId());
            }
        }
        //TODO: take into account zr zx Mbase...
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
        Line line = network.newLine()
                .setId(id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevel1Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setR(psseLine.getR() * zb)
                .setX(psseLine.getX() * zb)
                .setG1(0) //TODO
                .setB1(psseLine.getB() / zb / 2)
                .setG2(0) //TODO
                .setB2(psseLine.getB() / zb / 2)
                .add();

        if (psseLine.getSt() == 1) {
            line.getTerminal1().connect();
            line.getTerminal2().connect();
        }

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

        if (psseTfo.getFirstRecord().getK() == 0) {
            TwoWindingsTransformer tfo2W = voltageLevel2.getSubstation().newTwoWindingsTransformer()
                    .setId(id)
                    .setConnectableBus1(bus1Id)
                    .setVoltageLevel1(voltageLevel1Id)
                    .setConnectableBus2(bus2Id)
                    .setVoltageLevel2(voltageLevel2Id)
                    .setRatedU1(voltageLevel1.getNominalV() * psseTfo.getThirdRecord1().getWindv())
                    .setRatedU2(voltageLevel2.getNominalV())
                    .setR(psseTfo.getSecondRecord().getR12() * zb)
                    .setX(psseTfo.getSecondRecord().getX12() * zb)
                    .setG(0) //TODO
                    .setB(0) //TODO
                    .add();

            if (psseTfo.getFirstRecord().getStat() == 1) {
                tfo2W.getTerminal1().connect();
                tfo2W.getTerminal2().connect();
            }

        } else {
            LOGGER.warn("Non-2-windings transformers not supported ({})", id);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        try {
            String ext = findExtension(dataSource, true);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {

                // parse file
                PsseRawModel psseModel = new PsseRawReader().read(reader);

                // set date and time
                // TODO

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
                    createLoad(psseLoad, containerMapping, network);
                }

                //Create shunts
                for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
                    createShuntCompensator(psseShunt, perUnitContext, containerMapping, network);
                }

                for (PsseGenerator psseGen : psseModel.getGenerators()) {
                    createGenerator(psseGen, containerMapping, network);
                }

                for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
                    createLine(psseLine, containerMapping, perUnitContext, network);
                }

                for (PsseTransformer psseTfo : psseModel.getTransformers()) {
                    createTransformer(psseTfo, containerMapping, perUnitContext, network);
                }

                return network;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
