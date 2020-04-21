/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.ieeecdf.model.*;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class IeeeCdfImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IeeeCdfImporter.class);

    private static final String FORMAT = "IEEE-CDF";

    private static final String EXT = "txt";

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("ignore-base-voltage",
                                                                                 ParameterType.BOOLEAN,
                                                                                 "Ignore base voltage specified in the file",
                                                                                 Boolean.TRUE);

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
        return "IEEE Common Data Format to IIDM converter";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            if (dataSource.exists(null, EXT)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
                    String titleLine = reader.readLine();
                    if (titleLine != null) {
                        return titleLine.length() >= 44
                                && titleLine.charAt(3) == '/'
                                && titleLine.charAt(6) == '/'
                                && (titleLine.charAt(43) == 'S' || titleLine.charAt(43) == 'W');
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

    private static class ContainersMapping {

        private final Map<Integer, String> busNumToVoltageLevelId = new HashMap<>();

        private final Map<String, Set<Integer>> voltageLevelIdToBusNums = new HashMap<>();

        private final Map<String, String> voltageLevelIdToSubstationId = new HashMap<>();
    }

    private static ContainersMapping createContainerMapping(IeeeCdfModel ieeeCdfModel) {
        ContainersMapping containersMapping = new ContainersMapping();

        // group buses connected to non impedant lines to voltage levels
        createVoltageLevelMapping(ieeeCdfModel, containersMapping);

        // group voltage levels connected by transformers to substations
        createSubstationMapping(ieeeCdfModel, containersMapping);

        return containersMapping;
    }

    private static boolean isTransformer(IeeeCdfBranch ieeeCdfBranch) {
        return ieeeCdfBranch.getType() != null && (ieeeCdfBranch.getType() != IeeeCdfBranch.Type.TRANSMISSION_LINE || ieeeCdfBranch.getFinalTurnsRatio() != 0);
    }

    private static void createSubstationMapping(IeeeCdfModel ieeeCdfModel, ContainersMapping containersMapping) {
        UndirectedGraph<String, Object> sGraph = new Pseudograph<>(Object.class);
        for (String voltageLevelId : containersMapping.voltageLevelIdToBusNums.keySet()) {
            sGraph.addVertex(voltageLevelId);
        }
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            if (isTransformer(ieeeCdfBranch)) {
                sGraph.addEdge(containersMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber()),
                        containersMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber()));
            }
        }
        int substationNum = 1;
        for (Set<String> voltageLevelIds : new ConnectivityInspector<>(sGraph).connectedSets()) {
            String substationId = "S" + substationNum++;
            for (String voltageLevelId : voltageLevelIds) {
                containersMapping.voltageLevelIdToSubstationId.put(voltageLevelId, substationId);
            }
        }
    }

    private static void createVoltageLevelMapping(IeeeCdfModel ieeeCdfModel, ContainersMapping containersMapping) {
        UndirectedGraph<Integer, Object> vlGraph = new Pseudograph<>(Object.class);
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            vlGraph.addVertex(ieeeCdfBus.getNumber());
        }
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            if (ieeeCdfBranch.getResistance() == 0 && ieeeCdfBranch.getReactance() == 0) {
                vlGraph.addEdge(ieeeCdfBranch.getTapBusNumber(), ieeeCdfBranch.getzBusNumber());
            }
        }
        for (Set<Integer> busNums : new ConnectivityInspector<>(vlGraph).connectedSets()) {
            String voltageLevelId = "VL" + busNums.iterator().next();
            containersMapping.voltageLevelIdToBusNums.put(voltageLevelId, busNums);
            for (int busNum : busNums) {
                containersMapping.busNumToVoltageLevelId.put(busNum, voltageLevelId);
            }
        }
    }

    private static String getBusId(int busNum) {
        return "B" + busNum;
    }

    private static void createBuses(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping, PerUnitContext perUnitContext,
                                    Network network) {
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            String voltageLevelId = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBus.getNumber());
            String substationId = containerMapping.voltageLevelIdToSubstationId.get(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(ieeeCdfBus, perUnitContext, voltageLevelId, substation, network);

            // create bus
            createBus(ieeeCdfBus, voltageLevel);

            // create load
            createLoad(ieeeCdfBus, voltageLevel);

            // create shunt compensator
            createShuntCompensator(ieeeCdfBus, perUnitContext, voltageLevel);

            // create generator
            switch (ieeeCdfBus.getType()) {
                case UNREGULATED:
                    // nothing to do
                    break;

                case HOLD_MVAR_GENERATION_WITHIN_VOLTAGE_LIMITS:
                    newGeneratorAdder(ieeeCdfBus, voltageLevel)
                            .setTargetQ(ieeeCdfBus.getReactiveGeneration())
                            .setVoltageRegulatorOn(false)
                            .add();
                    break;

                case HOLD_VOLTAGE_WITHIN_VAR_LIMITS:
                case HOLD_VOLTAGE_AND_ANGLE:
                    Generator generator = newGeneratorAdder(ieeeCdfBus, voltageLevel)
                            .setTargetV(ieeeCdfBus.getDesiredVoltage() * voltageLevel.getNominalV())
                            .setVoltageRegulatorOn(true)
                            .add();
                    if (ieeeCdfBus.getMinReactivePowerOrVoltageLimit() != 0 || ieeeCdfBus.getMaxReactivePowerOrVoltageLimit() != 0) {
                        generator.newMinMaxReactiveLimits()
                                .setMinQ(ieeeCdfBus.getMinReactivePowerOrVoltageLimit())
                                .setMaxQ(ieeeCdfBus.getMaxReactivePowerOrVoltageLimit())
                                .add();
                    }
                    // Keep the given value for reactive output
                    // It is relevant if we want to load a solved case and validate it
                    // Another option would be to store given p, q values at terminal
                    generator.setTargetQ(ieeeCdfBus.getReactiveGeneration());
                    break;

                default:
                    throw new IllegalStateException("Unexpected bus type: " + ieeeCdfBus.getType());
            }
        }
    }

    private static Bus createBus(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        String busId = getBusId(ieeeCdfBus.getNumber());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(ieeeCdfBus.getName())
                .add();
        bus.setV(ieeeCdfBus.getFinalVoltage() * voltageLevel.getNominalV())
                .setAngle(ieeeCdfBus.getFinalAngle());
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

    private static VoltageLevel createVoltageLevel(IeeeCdfBus ieeeCdfBus, PerUnitContext perUnitContext,
                                                   String voltageLevelId, Substation substation, Network network) {
        double nominalV = perUnitContext.isIgnoreBaseVoltage() || ieeeCdfBus.getBaseVoltage() == 0 ? 1 : ieeeCdfBus.getBaseVoltage();
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

    private static void createLoad(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        if (ieeeCdfBus.getActiveLoad() != 0 || ieeeCdfBus.getReactiveLoad() != 0) {
            String busId = getBusId(ieeeCdfBus.getNumber());
            voltageLevel.newLoad()
                    .setId(busId + "-L")
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setP0(ieeeCdfBus.getActiveLoad())
                    .setQ0(ieeeCdfBus.getReactiveLoad())
                    .add();
        }
    }

    private static GeneratorAdder newGeneratorAdder(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        String busId = getBusId(ieeeCdfBus.getNumber());
        return voltageLevel.newGenerator()
                .setId(busId + "-G")
                .setConnectableBus(busId)
                .setBus(busId)
                .setTargetP(ieeeCdfBus.getActiveGeneration())
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE);
    }

    private static void createShuntCompensator(IeeeCdfBus ieeeCdfBus, PerUnitContext perUnitContext, VoltageLevel voltageLevel) {
        if (ieeeCdfBus.getShuntSusceptance() != 0) {
            String busId = getBusId(ieeeCdfBus.getNumber());
            double zb = Math.pow(voltageLevel.getNominalV(), 2) / perUnitContext.getSb();
            voltageLevel.newShuntCompensator()
                    .setId(busId + "-SH")
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setbPerSection(ieeeCdfBus.getShuntSusceptance() / zb)
                    .setCurrentSectionCount(1)
                    .setMaximumSectionCount(1)
                    .add();
        }
    }

    private static String getBranchId(char type, int tapBusNumber, int zBusNumber, int circuit, Network network) {
        int uniqueCircuit = circuit;
        String id;
        do {
            id = "" + type + tapBusNumber + "-" + zBusNumber + "-" + uniqueCircuit++;
        } while (network.getIdentifiable(id) != null);
        return id;
    }

    private static void createLine(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = getBranchId('L', ieeeCdfBranch.getTapBusNumber(), ieeeCdfBranch.getzBusNumber(), ieeeCdfBranch.getCircuit(), network);
        String bus1Id = getBusId(ieeeCdfBranch.getTapBusNumber());
        String bus2Id = getBusId(ieeeCdfBranch.getzBusNumber());
        String voltageLevel1Id = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber());
        String voltageLevel2Id = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber());
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
                .setR(ieeeCdfBranch.getResistance() * zb)
                .setX(ieeeCdfBranch.getReactance() * zb)
                .setG1(0)
                .setB1(ieeeCdfBranch.getChargingSusceptance() / zb / 2)
                .setG2(0)
                .setB2(ieeeCdfBranch.getChargingSusceptance() / zb / 2)
                .add();
    }

    private static TwoWindingsTransformer createTransformer(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = getBranchId('T', ieeeCdfBranch.getTapBusNumber(), ieeeCdfBranch.getzBusNumber(), ieeeCdfBranch.getCircuit(), network);
        String bus1Id = getBusId(ieeeCdfBranch.getTapBusNumber());
        String bus2Id = getBusId(ieeeCdfBranch.getzBusNumber());
        String voltageLevel1Id = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber());
        String voltageLevel2Id = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double zb = Math.pow(voltageLevel2.getNominalV(), 2) / perUnitContext.getSb();
        return voltageLevel2.getSubstation().newTwoWindingsTransformer()
                .setId(id)
                .setBus1(bus1Id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevel1Id)
                .setBus2(bus2Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setRatedU1(voltageLevel1.getNominalV() * ieeeCdfBranch.getFinalTurnsRatio())
                .setRatedU2(voltageLevel2.getNominalV())
                .setR(ieeeCdfBranch.getResistance() * zb)
                .setX(ieeeCdfBranch.getReactance() * zb)
                .setG(0)
                .setB(ieeeCdfBranch.getChargingSusceptance() / zb)
            .add();
    }

    private static Terminal getRegulatingTerminal(IeeeCdfBranch ieeeCdfBranch, TwoWindingsTransformer transformer) {
        Terminal regulatingTerminal = null;
        if (ieeeCdfBranch.getSide() != null) {
            switch (ieeeCdfBranch.getSide()) {
                case CONTROLLED_BUS_IS_ONE_OF_THE_TERMINALS:
                    int controlBusNum = ieeeCdfBranch.getControlBusNumber();
                    if (controlBusNum != 0) {
                        String controlBusId = getBusId(controlBusNum);
                        if (controlBusId.equals(transformer.getTerminal1().getBusBreakerView().getBus().getId())) {
                            regulatingTerminal = transformer.getTerminal1();
                        } else if (controlBusId.equals(transformer.getTerminal2().getBusBreakerView().getBus().getId())) {
                            regulatingTerminal = transformer.getTerminal2();
                        } else {
                            throw new UnsupportedOperationException("Remote control bus not yet supported: " + transformer.getId());
                        }
                    }
                    break;
                case CONTROLLED_BUS_IS_NEAR_THE_TAP_SIDE:
                    regulatingTerminal = transformer.getTerminal1();
                    break;
                case CONTROLLED_BUS_IS_NEAR_THE_IMPEDANCE_SIDE:
                    regulatingTerminal = transformer.getTerminal2();
                    break;
                default:
                    throw new IllegalStateException("Unknown branch side: " + ieeeCdfBranch.getSide());
            }
        }
        return regulatingTerminal;
    }

    private static void createTransformerWithVoltageControl(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        TwoWindingsTransformer transformer = createTransformer(ieeeCdfBranch, containerMapping, perUnitContext, network);
        boolean regulating = false;
        double targetV = Double.NaN;
        Terminal regulatingTerminal = getRegulatingTerminal(ieeeCdfBranch, transformer);
        if (regulatingTerminal != null) {
            Bus regulatingBus = regulatingTerminal.getBusView().getBus();
            if (regulatingBus != null) {
                regulating = true;
                targetV = regulatingBus.getV();
            }
        }
        List<Double> rhos = new ArrayList<>();
        rhos.add(1.0); // TODO create full table
        if (ieeeCdfBranch.getMinTapOrPhaseShift() != 0 && ieeeCdfBranch.getMaxTapOrPhaseShift() != 0) {
            LOGGER.warn("Tap steps are not yet supported ({})", transformer.getId());
        }
        RatioTapChangerAdder ratioTapChangerAdder = transformer.newRatioTapChanger()
                .setLoadTapChangingCapabilities(true)
                .setRegulating(regulating)
                .setRegulationTerminal(regulatingTerminal)
                .setTargetV(targetV)
                .setTargetDeadband(regulating ? 0.0 : Double.NaN)
                .setTapPosition(0);
        for (double rho : rhos) {
            ratioTapChangerAdder.beginStep()
                    .setRho(rho)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                    .endStep();
        }
        ratioTapChangerAdder.add();
    }

    private static void createTransformerWithActivePowerControl(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        TwoWindingsTransformer transformer = createTransformer(ieeeCdfBranch, containerMapping, perUnitContext, network);
        // As there is no active power or current setpoint in IEEE data model there is no way to have regulating phase
        // shifter and so on we always set it to fixed tap.
        PhaseTapChangerAdder phaseTapChangerAdder = transformer.newPhaseTapChanger()
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulating(false)
                .setTapPosition(0);
        List<Double> alphas = new ArrayList<>();
        alphas.add(-ieeeCdfBranch.getFinalAngle());  // TODO create full table
        if (ieeeCdfBranch.getMinTapOrPhaseShift() != 0 && ieeeCdfBranch.getMaxTapOrPhaseShift() != 0) {
            LOGGER.warn("Phase shift steps are not yet supported ({})", transformer.getId());
        }
        for (double alpha : alphas) {
            phaseTapChangerAdder.beginStep()
                    .setAlpha(alpha)
                    .setRho(1)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                    .endStep();
        }
        phaseTapChangerAdder.add();
    }

    private static void createBranches(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            if (ieeeCdfBranch.getType() == null) {
                createLine(ieeeCdfBranch, containerMapping, perUnitContext, network);
            } else {
                switch (ieeeCdfBranch.getType()) {
                    case TRANSMISSION_LINE:
                        if (ieeeCdfBranch.getFinalTurnsRatio() == 0) {
                            createLine(ieeeCdfBranch, containerMapping, perUnitContext, network);
                        } else {
                            createTransformer(ieeeCdfBranch, containerMapping, perUnitContext, network);
                        }
                        break;

                    case FIXED_TAP:
                        createTransformer(ieeeCdfBranch, containerMapping, perUnitContext, network);
                        break;

                    case VARIABLE_TAP_FOR_VOLTAVE_CONTROL:
                        createTransformerWithVoltageControl(ieeeCdfBranch, containerMapping, perUnitContext, network);
                        break;

                    case VARIABLE_TAP_FOR_REACTIVE_POWER_CONTROL:
                        throw new UnsupportedOperationException("Transformers not yet implemented: " + ieeeCdfBranch.getType());

                    case VARIABLE_PHASE_ANGLE_FOR_ACTIVE_POWER_CONTROL:
                        createTransformerWithActivePowerControl(ieeeCdfBranch, containerMapping, perUnitContext, network);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected branch type: " + ieeeCdfBranch.getType());
                }
            }

            // TODO create current limits
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
            // parse file
            IeeeCdfModel ieeeCdfModel = new IeeeCdfReader().read(reader);

            // set date and time
            IeeeCdfTitle ieeeCdfTitle = ieeeCdfModel.getTitle();
            if (ieeeCdfTitle.getDate() != null) {
                ZonedDateTime caseDateTime = ieeeCdfTitle.getDate().atStartOfDay(ZoneOffset.UTC.normalized());
                network.setCaseDate(new DateTime(caseDateTime.toInstant().toEpochMilli(), DateTimeZone.UTC));
            }

            // build container to fit IIDM requirements
            ContainersMapping containerMapping = createContainerMapping(ieeeCdfModel);

            boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                                                                                  ParameterDefaultValueConfig.INSTANCE);
            PerUnitContext perUnitContext = new PerUnitContext(ieeeCdfModel.getTitle().getMvaBase(), ignoreBaseVoltage);

            // create objects
            createBuses(ieeeCdfModel, containerMapping, perUnitContext, network);
            createBranches(ieeeCdfModel, containerMapping, perUnitContext, network);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }
}
