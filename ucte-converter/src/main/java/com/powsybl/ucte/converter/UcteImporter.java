/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.converter;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.ucte.network.*;
import eu.itesla_project.ucte.network.ext.UcteNetworkExt;
import eu.itesla_project.ucte.network.ext.UcteSubstation;
import eu.itesla_project.ucte.network.ext.UcteVoltageLevel;
import eu.itesla_project.ucte.network.io.UcteReader;
import eu.itesla_project.entsoe.util.EntsoeFileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class UcteImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteImporter.class);

    private static final float LINE_MIN_Z = 0.05f;

    private static final String[] EXTENSIONS = {"uct", "UCT"};

    @Override
    public String getFormat() {
        return "UCTE";
    }

    @Override
    public InputStream get16x16Icon() {
        return UcteImporter.class.getResourceAsStream("/icons/ucte16x16.png");
    }

    @Override
    public String getComment() {
        return "UCTE-DEF";
    }

    private static float getConductance(UcteTransformer ucteTransfo) {
        float g = 0;
        if (!Float.isNaN(ucteTransfo.getConductance())) {
            g = ucteTransfo.getConductance();
        }
        return g;
    }

    private static float getSusceptance(UcteElement ucteElement) {
        float b = 0;
        if (!Float.isNaN(ucteElement.getSusceptance())) {
            b = ucteElement.getSusceptance();
        }
        return b;
    }

    private static void createBuses(UcteNetworkExt ucteNetwork, Network network, EntsoeFileName ucteFileName) {
        for (UcteSubstation ucteSubstation : ucteNetwork.getSubstations()) {

            // skip substations with only one Xnode
            UcteNodeCode firstUcteNodeCode = Iterables.find(ucteSubstation.getNodes(),
                code -> code.getUcteCountryCode() != UcteCountryCode.XX, null);
            if (firstUcteNodeCode == null) {
                continue;
            }

            LOGGER.trace("Create substation '{}'", ucteSubstation.getName());

            Substation substation = network.newSubstation()
                    .setId(ucteSubstation.getName())
                    .setCountry(Country.valueOf(firstUcteNodeCode.getUcteCountryCode().name()))
                .add();

            for (UcteVoltageLevel ucteVoltageLevel : ucteSubstation.getVoltageLevels()) {
                UcteVoltageLevelCode ucteVoltageLevelCode = ucteVoltageLevel.getNodes().iterator().next().getVoltageLevelCode();

                LOGGER.trace("Create voltage level '{}'", ucteVoltageLevel.getName());

                VoltageLevel voltageLevel = substation.newVoltageLevel()
                        .setId(ucteVoltageLevel.getName())
                        .setNominalV(ucteVoltageLevelCode.getVoltageLevel())
                        .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();

                for (UcteNodeCode ucteNodeCode : ucteVoltageLevel.getNodes()) {
                    UcteNode ucteNode = ucteNetwork.getNode(ucteNodeCode);

                    // skip Xnodes
                    if (ucteNode.getCode().getUcteCountryCode() == UcteCountryCode.XX) {
                        continue;
                    }

                    LOGGER.trace("Create bus '{}'", ucteNodeCode.toString());

                    Bus bus = voltageLevel.getBusBreakerView().newBus()
                            .setId(ucteNodeCode.toString())
                        .add();

                    if (isValueValid(ucteNode.getActiveLoad()) || isValueValid(ucteNode.getReactiveLoad())) {
                        createLoad(ucteNode, voltageLevel, bus);
                    }

                    if (ucteNode.isGenerator()) {
                        createGenerator(ucteNode, voltageLevel, bus);
                    }
                }
            }
        }
    }

    private static boolean isValueValid(float value) {
        return !Float.isNaN(value) && value != 0;
    }

    private static void createLoad(UcteNode ucteNode, VoltageLevel voltageLevel, Bus bus) {
        String loadId = bus.getId() + "_load";

        LOGGER.trace("Create load '{}'", loadId);

        float p0 = 0;
        if (isValueValid(ucteNode.getActiveLoad())) {
            p0 = ucteNode.getActiveLoad();
        }
        float q0 = 0;
        if (isValueValid(ucteNode.getReactiveLoad())) {
            q0 = ucteNode.getReactiveLoad();
        }

        voltageLevel.newLoad()
                .setId(loadId)
                .setBus(bus.getId())
                .setConnectableBus(bus.getId())
                .setP0(p0)
                .setQ0(q0)
            .add();
    }

    private static void createGenerator(UcteNode ucteNode, VoltageLevel voltageLevel, Bus bus) {
        String generatorId = bus.getId() + "_generator";

        LOGGER.trace("Create generator '{}'", generatorId);

        EnergySource energySource = EnergySource.OTHER;
        if (ucteNode.getPowerPlantType() != null) {
            switch (ucteNode.getPowerPlantType()) {
                case C:
                case G:
                case L:
                case O:
                    energySource = EnergySource.THERMAL;
                    break;
                case H:
                    energySource = EnergySource.HYDRO;
                    break;
                case N:
                    energySource = EnergySource.NUCLEAR;
                    break;
                case W:
                    energySource = EnergySource.WIND;
                    break;
            }
        }

        Generator generator = voltageLevel.newGenerator()
                .setId(generatorId)
                .setEnergySource(energySource)
                .setBus(bus.getId())
                .setConnectableBus(bus.getId())
                .setMinP(-ucteNode.getMinimumPermissibleActivePowerGeneration())
                .setMaxP(-ucteNode.getMaximumPermissibleActivePowerGeneration())
                .setVoltageRegulatorOn(ucteNode.isRegulatingVoltage())
                .setTargetP(-ucteNode.getActivePowerGeneration())
                .setTargetQ(-ucteNode.getReactivePowerGeneration())
                .setTargetV(ucteNode.getVoltageReference())
            .add();
        generator.newMinMaxReactiveLimits()
                .setMinQ(-ucteNode.getMinimumPermissibleReactivePowerGeneration())
                .setMaxQ(-ucteNode.getMaximumPermissibleReactivePowerGeneration())
            .add();
    }

    private static void createXnodeCoupler(UcteNetworkExt ucteNetwork, UcteLine ucteLine,
                                           UcteNodeCode ucteXnodeCode, UcteVoltageLevel ucteXvoltageLevel,
                                           UcteNodeCode ucteOtherNodeCode, Network network) {
        // coupler connected to a XNODE
        // creation of an intermediate YNODE and small impedance line:
        // otherNode--coupler--XNODE => otherNode--coupler--YNODE--line--XNODE
        String xNodeName = ucteXnodeCode.toString();
        String yNodeName = "Y" + xNodeName.substring(1);

        VoltageLevel xNodeVoltageLevel = network.getVoltageLevel(ucteXvoltageLevel.getName());

        // create YNODE
        xNodeVoltageLevel.getBusBreakerView().newBus()
                 .setId(yNodeName)
             .add();

        UcteNode ucteXnode = ucteNetwork.getNode(ucteXnodeCode);

        LOGGER.warn("Create small impedance dangling line '{}' (coupler connected to XNODE '{}')",
                xNodeName + yNodeName, ucteXnode.getCode());

        float p0 = 0;
        if (isValueValid(ucteXnode.getActiveLoad())) {
            p0 += ucteXnode.getActiveLoad();
        }
        if (isValueValid(ucteXnode.getActivePowerGeneration())) {
            p0 += ucteXnode.getActivePowerGeneration();
        }
        float q0 = 0;
        if (isValueValid(ucteXnode.getReactiveLoad())) {
            q0 += ucteXnode.getReactiveLoad();
        }
        if (isValueValid(ucteXnode.getReactivePowerGeneration())) {
            q0 += ucteXnode.getReactivePowerGeneration();
        }

        // create small impedance dangling line connected to the YNODE
        xNodeVoltageLevel.newDanglingLine()
                .setId(xNodeName + yNodeName)
                .setBus(yNodeName)
                .setConnectableBus(yNodeName)
                .setR(0.0f)
                .setX(LINE_MIN_Z)
                .setG(0f)
                .setB(0f)
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(ucteXnode.getCode().toString())
            .add();

        // create coupler between YNODE and other node
        xNodeVoltageLevel.getBusBreakerView().newSwitch()
                .setId(ucteLine.getId().toString())
                .setBus1(yNodeName)
                .setBus2(ucteOtherNodeCode.toString())
                .setOpen(ucteLine.getStatus() == UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION)
            .add();
    }

    private static void createDanglingLine(UcteNetworkExt ucteNetwork, UcteLine ucteLine, boolean connected,
                                           UcteNode xnode, UcteNodeCode nodeCode, UcteVoltageLevel ucteVoltageLevel,
                                           Network network) {

        LOGGER.trace("Create dangling line '{}' (Xnode='{}')", ucteLine.getId(), xnode.getCode());

        float p0 = 0;
        if (isValueValid(xnode.getActiveLoad())) {
            p0 += xnode.getActiveLoad();
        }
        if (isValueValid(xnode.getActivePowerGeneration())) {
            p0 += xnode.getActivePowerGeneration();
        }
        float q0 = 0;
        if (isValueValid(xnode.getReactiveLoad())) {
            q0 += xnode.getReactiveLoad();
        }
        if (isValueValid(xnode.getReactivePowerGeneration())) {
            q0 += xnode.getReactivePowerGeneration();
        }

        VoltageLevel voltageLevel = network.getVoltageLevel(ucteVoltageLevel.getName());
        DanglingLine dl = voltageLevel.newDanglingLine()
                .setId(ucteLine.getId().toString())
                .setBus(connected ? nodeCode.toString() : null)
                .setConnectableBus(nodeCode.toString())
                .setR(ucteLine.getResistance())
                .setX(ucteLine.getReactance())
                .setG(0f)
                .setB(getSusceptance(ucteLine))
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(xnode.getCode().toString())
                .add();
        if (ucteLine.getCurrentLimit() != null) {
            dl.newCurrentLimits()
                    .setPermanentLimit(ucteLine.getCurrentLimit())
                    .add();
        }

    }

    private static void createLines(UcteNetworkExt ucteNetwork, Network network) {
        for (UcteLine ucteLine : ucteNetwork.getLines()) {
            UcteNodeCode nodeCode1 = ucteLine.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = ucteLine.getId().getNodeCode2();
            UcteVoltageLevel ucteVoltageLevel1 = ucteNetwork.getVoltageLevel(nodeCode1);
            UcteVoltageLevel ucteVoltageLevel2 = ucteNetwork.getVoltageLevel(nodeCode2);

            switch (ucteLine.getStatus()) {
                case BUSBAR_COUPLER_IN_OPERATION:
                case BUSBAR_COUPLER_OUT_OF_OPERATION: {

                    LOGGER.trace("Create coupler '{}'", ucteLine.getId());

                    if (ucteVoltageLevel1 != ucteVoltageLevel2) {
                        throw new RuntimeException("Coupler between two different voltage levels");
                    }

                    if (nodeCode1.getUcteCountryCode() == UcteCountryCode.XX &&
                        nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {
                        // coupler connected to a XNODE
                        createXnodeCoupler(ucteNetwork, ucteLine, nodeCode1, ucteVoltageLevel1, nodeCode2, network);

                    } else if (nodeCode2.getUcteCountryCode() == UcteCountryCode.XX &&
                               nodeCode1.getUcteCountryCode() != UcteCountryCode.XX) {
                        // coupler connected to a XNODE
                        createXnodeCoupler(ucteNetwork, ucteLine, nodeCode2, ucteVoltageLevel2, nodeCode1, network);

                    } else {
                         // standard coupler
                        VoltageLevel voltageLevel = network.getVoltageLevel(ucteVoltageLevel1.getName());
                        voltageLevel.getBusBreakerView().newSwitch()
                                .setId(ucteLine.getId().toString())
                                .setBus1(nodeCode1.toString())
                                .setBus2(nodeCode2.toString())
                                .setOpen(ucteLine.getStatus() == UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION)
                                .add();
                    }

                    break;
                }

                case REAL_ELEMENT_IN_OPERATION:
                case REAL_ELEMENT_OUT_OF_OPERATION:
                case EQUIVALENT_ELEMENT_IN_OPERATION:
                case EQUIVALENT_ELEMENT_OUT_OF_OPERATION: {

                    boolean connected = ucteLine.getStatus() == UcteElementStatus.REAL_ELEMENT_IN_OPERATION
                            || ucteLine.getStatus() == UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;

                    double z = Math.hypot(ucteLine.getResistance(), ucteLine.getReactance());

                    if (z < LINE_MIN_Z
                            && nodeCode1.getUcteCountryCode() != UcteCountryCode.XX
                            && nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {

                        LOGGER.info("Create coupler '{}' from low impedance line ({})", ucteLine.getId(), z);

                        if (ucteVoltageLevel1 != ucteVoltageLevel2) {
                            throw new RuntimeException("Nodes coupled with a low impedance line are expected to be in the same voltage level");
                        }
                        VoltageLevel voltageLevel = network.getVoltageLevel(ucteVoltageLevel1.getName());
                        voltageLevel.getBusBreakerView().newSwitch()
                                .setId(ucteLine.getId().toString())
                                .setBus1(nodeCode1.toString())
                                .setBus2(nodeCode2.toString())
                                .setOpen(!connected)
                            .add();

                    } else {

                        if (nodeCode1.getUcteCountryCode() != UcteCountryCode.XX
                                && nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {

                            LOGGER.trace("Create line '{}'", ucteLine.getId());

                            Line l = network.newLine()
                                    .setId(ucteLine.getId().toString())
                                    .setVoltageLevel1(ucteVoltageLevel1.getName())
                                    .setVoltageLevel2(ucteVoltageLevel2.getName())
                                    .setBus1(connected ? nodeCode1.toString() : null)
                                    .setBus2(connected ? nodeCode2.toString() : null)
                                    .setConnectableBus1(nodeCode1.toString())
                                    .setConnectableBus2(nodeCode2.toString())
                                    .setR(ucteLine.getResistance())
                                    .setX(ucteLine.getReactance())
                                    .setG1(0f)
                                    .setG2(0f)
                                    .setB1(getSusceptance(ucteLine) / 2)
                                    .setB2(getSusceptance(ucteLine) / 2)
                                .add();
                            if (ucteLine.getCurrentLimit() != null) {
                                int currentLimit = ucteLine.getCurrentLimit();
                                l.newCurrentLimits1()
                                        .setPermanentLimit(currentLimit)
                                    .add();
                                l.newCurrentLimits2()
                                        .setPermanentLimit(currentLimit)
                                    .add();
                            }

                        } else if (nodeCode1.getUcteCountryCode() == UcteCountryCode.XX
                                && nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {

                            UcteNode xnode = ucteNetwork.getNode(nodeCode1);

                            createDanglingLine(ucteNetwork, ucteLine, connected, xnode, nodeCode2, ucteVoltageLevel2, network);

                        } else if (nodeCode1.getUcteCountryCode() != UcteCountryCode.XX
                                && nodeCode2.getUcteCountryCode() == UcteCountryCode.XX) {

                            UcteNode xnode = ucteNetwork.getNode(nodeCode2);

                            createDanglingLine(ucteNetwork, ucteLine, connected, xnode, nodeCode1, ucteVoltageLevel1, network);

                        } else {
                            throw new RuntimeException("Line between 2 Xnodes");
                        }
                    }

                    break;
                }

                default:
                    throw new InternalError();
            }
        }
    }

    private static void createRatioTapChanger(UctePhaseRegulation uctePhaseRegulation, TwoWindingsTransformer transformer) {

        LOGGER.trace("Create ratio tap changer '{}'", transformer.getId());

        RatioTapChangerAdder rtca = transformer.newRatioTapChanger()
                .setLowTapPosition(-uctePhaseRegulation.getN())
                .setTapPosition(uctePhaseRegulation.getNp())
                .setLoadTapChangingCapabilities(!Float.isNaN(uctePhaseRegulation.getU()));
        if (!Float.isNaN(uctePhaseRegulation.getU())) {
            rtca.setLoadTapChangingCapabilities(true)
                    .setRegulating(true)
                    .setTargetV(uctePhaseRegulation.getU())
                    .setRegulationTerminal(transformer.getTerminal1());
        }
        for (int i = -uctePhaseRegulation.getN(); i <= uctePhaseRegulation.getN(); i++) {
            float rho = 1 / (1 + i * uctePhaseRegulation.getDu() / 100f);
            rtca.beginStep()
                    .setRho(rho)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep();
        }
        rtca.add();
    }

    private static void createPhaseTapChanger(UcteAngleRegulation ucteAngleRegulation, TwoWindingsTransformer transformer) {

        LOGGER.trace("Create phase tap changer '{}'", transformer.getId());

        PhaseTapChangerAdder ptca = transformer.newPhaseTapChanger()
                .setLowTapPosition(-ucteAngleRegulation.getN())
                .setTapPosition(ucteAngleRegulation.getNp())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);

        for (int i = -ucteAngleRegulation.getN(); i <= ucteAngleRegulation.getN(); i++) {
            float rho;
            float alpha;
            double dx = i * ucteAngleRegulation.getDu() / 100f * Math.cos(Math.toRadians(ucteAngleRegulation.getTheta()));
            double dy = i * ucteAngleRegulation.getDu() / 100f * Math.sin(Math.toRadians(ucteAngleRegulation.getTheta()));
            switch (ucteAngleRegulation.getType()) {
                case ASYM:
                    rho = (float) (1 / Math.hypot(dy, 1 + dx));
                    alpha = (float) Math.toDegrees(Math.atan2(dy, 1 + dx));
                    break;

                case SYMM:
                    rho = 1f;
                    alpha = (float) Math.toDegrees(2 * Math.atan2(dy, 2f * (1 + dx)));
                    break;

                default:
                    throw new InternalError();
            }
            ptca.beginStep()
                    .setRho(rho)
                    .setAlpha(-alpha) // minus because in the UCT model PST is on side 2 and side1 on IIDM model
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep();
        }
        ptca.add();
    }

    private static TwoWindingsTransformer createXnodeTransfo(UcteNetworkExt ucteNetwork, UcteTransformer ucteTransfo, boolean connected,
                                                             UcteNodeCode xNodeCode, UcteNodeCode ucteOtherNodeCode, UcteVoltageLevel ucteOtherVoltageLevel,
                                                             Substation substation, EntsoeFileName ucteFileName) {
        // transfo connected to a XNODE, create an intermediate YNODE and small impedance line
        // otherNode--transfo--XNODE => otherNode--transfo--YNODE--line--XNODE
        String xNodeName = xNodeCode.toString();
        String yNodeName = ucteFileName.getCountry() != null ? ucteFileName.getCountry() + "_" + xNodeName : "YNODE_" + xNodeName;

        VoltageLevel yVoltageLevel = substation.newVoltageLevel()
                .setId(yNodeName + "_VL")
                .setNominalV(xNodeCode.getVoltageLevelCode().getVoltageLevel()) // nominal voltage of the XNODE
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        yVoltageLevel.getBusBreakerView().newBus()
                .setId(yNodeName)
            .add();

        UcteNode ucteXnode = ucteNetwork.getNode(xNodeCode);

        LOGGER.warn("Create small impedance dangling line '{}' (transformer connected to XNODE '{}')",
                xNodeName + yNodeName, ucteXnode.getCode());

        float p0 = 0;
        if (isValueValid(ucteXnode.getActiveLoad())) {
            p0 += ucteXnode.getActiveLoad();
        }
        if (isValueValid(ucteXnode.getActivePowerGeneration())) {
            p0 += ucteXnode.getActivePowerGeneration();
        }
        float q0 = 0;
        if (isValueValid(ucteXnode.getReactiveLoad())) {
            q0 += ucteXnode.getReactiveLoad();
        }
        if (isValueValid(ucteXnode.getReactivePowerGeneration())) {
            q0 += ucteXnode.getReactivePowerGeneration();
        }

        // create a small impedance dangling line connected to the YNODE
        yVoltageLevel.newDanglingLine()
                .setId(xNodeName + " " + yNodeName)
                .setBus(yNodeName)
                .setConnectableBus(yNodeName)
                .setR(0.0f)
                .setX(LINE_MIN_Z)
                .setG(0f)
                .setB(0f)
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(ucteXnode.getCode().toString())
            .add();

        String voltageLevelId1;
        String voltageLevelId2;
        String busId1;
        String busId2;
        if (ucteXnode.getCode().equals(ucteTransfo.getId().getNodeCode1())) {
            voltageLevelId1 = ucteOtherVoltageLevel.getName();
            voltageLevelId2 = yVoltageLevel.getName();
            busId1 = ucteOtherNodeCode.toString();
            busId2 = yNodeName;
        } else {
            voltageLevelId1 = yVoltageLevel.getName();
            voltageLevelId2 = ucteOtherVoltageLevel.getName();
            busId1 = yNodeName;
            busId2 = ucteOtherNodeCode.toString();
        }

        // create a transformer connected to the YNODE and other node
        return substation.newTwoWindingsTransformer()
                .setId(ucteTransfo.getId().toString())
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setBus1(connected ? busId1 : null)
                .setBus2(connected ? busId2 : null)
                .setConnectableBus1(busId1)
                .setConnectableBus2(busId2)
                .setRatedU1(ucteTransfo.getRatedVoltage2())
                .setRatedU2(ucteTransfo.getRatedVoltage1())
                .setR(ucteTransfo.getResistance())
                .setX(ucteTransfo.getReactance())
                .setG(getConductance(ucteTransfo))
                .setB(getSusceptance(ucteTransfo))
            .add();
    }

    private static void createTransformers(UcteNetworkExt ucteNetwork, Network network, EntsoeFileName ucteFileName) {
        for (UcteTransformer ucteTransfo : ucteNetwork.getTransformers()) {
            UcteNodeCode nodeCode1 = ucteTransfo.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = ucteTransfo.getId().getNodeCode2();
            UcteVoltageLevel ucteVoltageLevel1 = ucteNetwork.getVoltageLevel(nodeCode1);
            UcteVoltageLevel ucteVoltageLevel2 = ucteNetwork.getVoltageLevel(nodeCode2);
            UcteSubstation ucteSubstation = ucteVoltageLevel1.getSubstation();
            Substation substation = network.getSubstation(ucteSubstation.getName());

            LOGGER.trace("Create transformer '{}'", ucteTransfo.getId());

            boolean connected;
            switch (ucteTransfo.getStatus()) {
                case REAL_ELEMENT_IN_OPERATION:
                case EQUIVALENT_ELEMENT_IN_OPERATION:
                    connected = true;
                    break;

                case REAL_ELEMENT_OUT_OF_OPERATION:
                case EQUIVALENT_ELEMENT_OUT_OF_OPERATION:
                    connected = false;
                    break;

                default:
                    throw new InternalError();
            }

            TwoWindingsTransformer transformer;

            if (nodeCode1.getUcteCountryCode() == UcteCountryCode.XX &&
                nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {
                // transformer connected to XNODE
                transformer = createXnodeTransfo(ucteNetwork, ucteTransfo, connected, nodeCode1, nodeCode2, ucteVoltageLevel2, substation, ucteFileName);

            } else if (nodeCode2.getUcteCountryCode() == UcteCountryCode.XX &&
                       nodeCode1.getUcteCountryCode() != UcteCountryCode.XX) {
                // transformer connected to XNODE
                transformer = createXnodeTransfo(ucteNetwork, ucteTransfo, connected, nodeCode2, nodeCode1, ucteVoltageLevel1, substation, ucteFileName);

            } else {
                // standard transformer
                transformer = substation.newTwoWindingsTransformer()
                        .setId(ucteTransfo.getId().toString())
                        .setVoltageLevel1(ucteVoltageLevel2.getName())
                        .setVoltageLevel2(ucteVoltageLevel1.getName())
                        .setBus1(connected ? nodeCode2.toString() : null)
                        .setBus2(connected ? nodeCode1.toString() : null)
                        .setConnectableBus1(nodeCode2.toString())
                        .setConnectableBus2(nodeCode1.toString())
                        .setRatedU1(ucteTransfo.getRatedVoltage2())
                        .setRatedU2(ucteTransfo.getRatedVoltage1())
                        .setR(ucteTransfo.getResistance())
                        .setX(ucteTransfo.getReactance())
                        .setG(getConductance(ucteTransfo))
                        .setB(getSusceptance(ucteTransfo))
                    .add();
            }

            if (ucteTransfo.getCurrentLimit() != null) {
                int currentLimit = ucteTransfo.getCurrentLimit();
                transformer.newCurrentLimits2()
                        .setPermanentLimit(currentLimit)
                    .add();
            }

            UcteRegulation ucteRegulation = ucteNetwork.getRegulation(ucteTransfo.getId());
            if (ucteRegulation != null) {
                if (ucteRegulation.getPhaseRegulation() != null) {
                    createRatioTapChanger(ucteRegulation.getPhaseRegulation(), transformer);
                }
                if (ucteRegulation.getAngleRegulation() != null) {
                    createPhaseTapChanger(ucteRegulation.getAngleRegulation(), transformer);
                }
            }
        }
    }

    private String findExtension(ReadOnlyDataSource dataSource) throws IOException {
        for (String ext : EXTENSIONS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            if (ext != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
                    return new UcteReader().checkHeader(reader);
                }
            }
            return false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new RuntimeException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(EXTENSIONS) + " not found");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
                long start = System.currentTimeMillis();
                UcteNetworkExt ucteNetwork = new UcteNetworkExt(new UcteReader().read(reader), LINE_MIN_Z);
                String fileName = dataSource.getBaseName();

                EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);

                Network network = NetworkFactory.create(fileName, "UCTE");
                network.setCaseDate(ucteFileName.getDate());
                network.setForecastDistance(ucteFileName.getForecastDistance());

                createBuses(ucteNetwork, network, ucteFileName);
                createLines(ucteNetwork, network);
                createTransformers(ucteNetwork, network, ucteFileName);
                LOGGER.debug("UCTE import done in {} ms", System.currentTimeMillis() - start);
                return network;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
