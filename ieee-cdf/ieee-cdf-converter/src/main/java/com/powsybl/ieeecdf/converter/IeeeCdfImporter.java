/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.ieeecdf.model.*;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ContainersMapping;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Importer.class)
public class IeeeCdfImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IeeeCdfImporter.class);

    private static final String FORMAT = "IEEE-CDF";

    private static final String EXT = "txt";

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("ignore-base-voltage",
                                                                                 ParameterType.BOOLEAN,
                                                                                 "Ignore base voltage specified in the file",
                                                                                 Boolean.FALSE);

    private static final double DEFAULT_ACTIVE_POWER_LIMIT = 9999d;

    static final ToDoubleFunction<IeeeCdfBus> DEFAULT_NOMINAL_VOLTAGE_PROVIDER = ieeeCdfBus -> ieeeCdfBus.getBaseVoltage() == 0 ? 1 : ieeeCdfBus.getBaseVoltage();

    private final ToDoubleFunction<IeeeCdfBus> nominalVoltageProvider;

    public IeeeCdfImporter() {
        this(DEFAULT_NOMINAL_VOLTAGE_PROVIDER);
    }

    public IeeeCdfImporter(ToDoubleFunction<IeeeCdfBus> nominalVoltageProvider) {
        this.nominalVoltageProvider = Objects.requireNonNull(nominalVoltageProvider);
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<String> getSupportedExtensions() {
        return List.of(EXT);
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
            if (dataSource.isDataExtension(EXT) && dataSource.exists(null, EXT)) {
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

    private static boolean isTransformer(IeeeCdfBranch ieeeCdfBranch) {
        return ieeeCdfBranch.getType() != null && (ieeeCdfBranch.getType() != IeeeCdfBranch.Type.TRANSMISSION_LINE || ieeeCdfBranch.getFinalTurnsRatio() != 0);
    }

    private static String getBusId(int busNum) {
        return "B" + busNum;
    }

    private void createBuses(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping, PerUnitContext perUnitContext,
                                    Network network) {
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            String voltageLevelId = containerMapping.getVoltageLevelId(ieeeCdfBus.getNumber());
            String substationId = containerMapping.getSubstationId(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(ieeeCdfBus, perUnitContext, voltageLevelId, substation, network);

            // create bus
            Bus bus = createBus(ieeeCdfBus, voltageLevel);

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

            // Attach a slack bus
            if (ieeeCdfBus.getType() == IeeeCdfBus.Type.HOLD_VOLTAGE_AND_ANGLE) {
                SlackTerminal.attach(bus);
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

    private double getNominalV(IeeeCdfBus ieeeCdfBus, PerUnitContext perUnitContext) {
        if (perUnitContext.isIgnoreBaseVoltage()) {
            return 1;
        }
        return nominalVoltageProvider.applyAsDouble(ieeeCdfBus);
    }

    private VoltageLevel createVoltageLevel(IeeeCdfBus ieeeCdfBus, PerUnitContext perUnitContext,
                                            String voltageLevelId, Substation substation, Network network) {
        double nominalV = getNominalV(ieeeCdfBus, perUnitContext);
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
                .setMaxP(DEFAULT_ACTIVE_POWER_LIMIT)
                .setMinP(-DEFAULT_ACTIVE_POWER_LIMIT);
    }

    private static void createShuntCompensator(IeeeCdfBus ieeeCdfBus, PerUnitContext perUnitContext, VoltageLevel voltageLevel) {
        if (ieeeCdfBus.getShuntSusceptance() != 0) {
            String busId = getBusId(ieeeCdfBus.getNumber());
            double zb = Math.pow(voltageLevel.getNominalV(), 2) / perUnitContext.getSb();
            voltageLevel.newShuntCompensator()
                    .setId(busId + "-SH")
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setSectionCount(1)
                    .newLinearModel()
                        .setMaximumSectionCount(1)
                        .setBPerSection(ieeeCdfBus.getShuntSusceptance() / zb)
                        .add()
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
        String voltageLevel1Id = containerMapping.getVoltageLevelId(ieeeCdfBranch.getTapBusNumber());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(ieeeCdfBranch.getzBusNumber());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);

        double nominalV1 = voltageLevel1.getNominalV();
        double nominalV2 = voltageLevel2.getNominalV();
        double sBase = perUnitContext.getSb();
        double r = impedanceToEngineeringUnitsForLine(ieeeCdfBranch.getResistance(), nominalV1, nominalV2, sBase);
        double x = impedanceToEngineeringUnitsForLine(ieeeCdfBranch.getReactance(), nominalV1, nominalV2, sBase);
        Complex ytr = impedanceToAdmittance(r, x);
        double g1 = admittanceEndToEngineeringUnitsForLine(ytr.getReal(), 0.0, nominalV1, nominalV2, sBase);
        double b1 = admittanceEndToEngineeringUnitsForLine(ytr.getImaginary(), ieeeCdfBranch.getChargingSusceptance() * 0.5, nominalV1, nominalV2, sBase);
        double g2 = admittanceEndToEngineeringUnitsForLine(ytr.getReal(), 0.0, nominalV2, nominalV1, sBase);
        double b2 = admittanceEndToEngineeringUnitsForLine(ytr.getImaginary(), ieeeCdfBranch.getChargingSusceptance() * 0.5, nominalV2, nominalV1, sBase);

        network.newLine()
                .setId(id)
                .setBus1(bus1Id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevel1Id)
                .setBus2(bus2Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .add();
    }

    // avoid NaN when r and x, both are 0.0
    private static Complex impedanceToAdmittance(double r, double x) {
        return r == 0.0 && x == 0.0 ? new Complex(0.0, 0.0) : new Complex(r, x).reciprocal();
    }

    private static double impedanceToEngineeringUnitsForLine(double impedance, double nominalVoltageAtEnd, double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        return impedance * nominalVoltageAtEnd * nominalVoltageAtOtherEnd / sBase;
    }

    private static double admittanceEndToEngineeringUnitsForLine(double transmissionAdmittance, double shuntAdmittanceAtEnd,
                                                                 double nominalVoltageAtEnd, double nominalVoltageAtOtherEnd, double sBase) {
        // this method handles also line with different nominal voltage at ends
        // note that ytr is already in engineering units
        return shuntAdmittanceAtEnd * sBase / (nominalVoltageAtEnd * nominalVoltageAtEnd) - (1 - nominalVoltageAtOtherEnd / nominalVoltageAtEnd) * transmissionAdmittance;
    }

    private static TwoWindingsTransformer createTransformer(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = getBranchId('T', ieeeCdfBranch.getTapBusNumber(), ieeeCdfBranch.getzBusNumber(), ieeeCdfBranch.getCircuit(), network);
        String bus1Id = getBusId(ieeeCdfBranch.getTapBusNumber());
        String bus2Id = getBusId(ieeeCdfBranch.getzBusNumber());
        String voltageLevel1Id = containerMapping.getVoltageLevelId(ieeeCdfBranch.getTapBusNumber());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(ieeeCdfBranch.getzBusNumber());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double zb = Math.pow(voltageLevel2.getNominalV(), 2) / perUnitContext.getSb();
        return voltageLevel2.getSubstation().map(Substation::newTwoWindingsTransformer)
                .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
            // parse file
            IeeeCdfModel ieeeCdfModel = new IeeeCdfReader().read(reader);

            boolean ignoreBaseVoltage = Parameter.readBoolean(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);

            return convert(ieeeCdfModel, networkFactory, dataSource.getBaseName(), ignoreBaseVoltage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Network convert(IeeeCdfModel ieeeCdfModel, NetworkFactory networkFactory, String networkId, boolean ignoreBaseVoltage) {
        PerUnitContext perUnitContext = new PerUnitContext(ieeeCdfModel.getTitle().getMvaBase(), ignoreBaseVoltage);

        Network network = networkFactory.createNetwork(networkId, FORMAT);

        // set date and time
        IeeeCdfTitle ieeeCdfTitle = ieeeCdfModel.getTitle();
        if (ieeeCdfTitle.getDate() != null) {
            ZonedDateTime caseDateTime = ieeeCdfTitle.getDate().atStartOfDay(ZoneOffset.UTC.normalized());
            network.setCaseDate(ZonedDateTime.ofInstant(caseDateTime.toInstant(), ZoneOffset.UTC));
        }

        // build container to fit IIDM requirements
        Map<Integer, IeeeCdfBus> busNumToIeeeCdfBus = ieeeCdfModel.getBuses().stream().collect(Collectors.toMap(IeeeCdfBus::getNumber, Function.identity()));

        ContainersMapping containerMapping = ContainersMapping.create(ieeeCdfModel.getBuses(), ieeeCdfModel.getBranches(),
            IeeeCdfBus::getNumber,
            IeeeCdfBranch::getTapBusNumber,
            IeeeCdfBranch::getzBusNumber,
            branch -> branch.getResistance() == 0.0 && branch.getReactance() == 0.0,
            IeeeCdfImporter::isTransformer,
            busNumber -> getNominalVFromBusNumber(busNumToIeeeCdfBus, busNumber, perUnitContext),
            busNums -> "VL" + busNums.stream().sorted().findFirst().orElseThrow(() -> new PowsyblException("Unexpected empty busNums")),
            substationNums -> "S" + substationNums.stream().sorted().findFirst().orElseThrow(() -> new PowsyblException("Unexpected empty substationNums")));

        // create objects
        createBuses(ieeeCdfModel, containerMapping, perUnitContext, network);
        createBranches(ieeeCdfModel, containerMapping, perUnitContext, network);

        return network;
    }

    private double getNominalVFromBusNumber(Map<Integer, IeeeCdfBus> busNumToIeeeCdfBus, int busNumber, PerUnitContext perUnitContext) {
        if (!busNumToIeeeCdfBus.containsKey(busNumber)) { // never should happen
            throw new PowsyblException("busId without IeeeCdfBus" + busNumber);
        }
        return getNominalV(busNumToIeeeCdfBus.get(busNumber), perUnitContext);
    }
}
