/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.*;
import com.powsybl.ucte.network.io.UcteWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    private HashMap<String, UcteNodeCode> iidmIdToUcteId = new HashMap<>();

    @Override
    public String getFormat() {
        return "UCTE";
    }

    @Override
    public String getComment() {
        return "IIDM to UCTE converter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }
        UcteNetwork ucteNetwork = createUcteNetwork(network);

        try (OutputStream os = dataSource.newOutputStream("", "uct", false);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            new UcteWriter(ucteNetwork).write(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void convertTieLine(UcteNetwork ucteNetwork, Line line) {
        String id1 = line.getId().substring(0, 8); //first node line 1
        String id2 = line.getId().substring(9, 17); //second one line 1
        String id3 = line.getId().substring(22, 30); //first node line 2
        String id4 = line.getId().substring(31, 39); //second node line 2

        UcteNodeCode ucteNodeCode1 = iidmIdToUcteNodeCode(id1);
        UcteNodeCode ucteNodeCode2 = iidmIdToUcteNodeCode(id2);
        UcteNodeCode ucteNodeCode3 = iidmIdToUcteNodeCode(id3);
        UcteNodeCode ucteNodeCode4 = iidmIdToUcteNodeCode(id4);

        if (ucteNetwork.getNode(ucteNodeCode1) == null) {
            ucteNetwork.addNode(
                    new UcteNode(
                            ucteNodeCode1,
                            ucteNodeCode1.getGeographicalSpot(),
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            voltageLevelCodeFromChar(ucteNodeCode1.toString().charAt(6)).getVoltageLevel(),
                            0f,
                            0f,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            null
                    ));
        }
        if (ucteNetwork.getNode(ucteNodeCode2) == null) {
            ucteNetwork.addNode(
                    new UcteNode(
                            ucteNodeCode2,
                            ucteNodeCode2.getGeographicalSpot(),
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            voltageLevelCodeFromChar(ucteNodeCode2.toString().charAt(6)).getVoltageLevel(),
                            0f,
                            0f,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            null
                    ));
        }
        if (ucteNetwork.getNode(ucteNodeCode3) == null) {
            ucteNetwork.addNode(
                    new UcteNode(
                            ucteNodeCode3,
                            ucteNodeCode3.getGeographicalSpot(),
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            voltageLevelCodeFromChar(ucteNodeCode3.toString().charAt(6)).getVoltageLevel(),
                            0f,
                            0f,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            null
                    ));
        }
        if (ucteNetwork.getNode(ucteNodeCode4) == null) {
            ucteNetwork.addNode(
                    new UcteNode(
                            ucteNodeCode4,
                            ucteNodeCode4.getGeographicalSpot(),
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            voltageLevelCodeFromChar(ucteNodeCode4.toString().charAt(6)).getVoltageLevel(),
                            0f,
                            0f,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            Float.NaN,
                            null
                    ));
        }

        UcteElementId ucteElementId1 = new UcteElementId(ucteNodeCode1, ucteNodeCode2, line.getId().charAt(18));
        UcteElementId ucteElementId2 = new UcteElementId(ucteNodeCode3, ucteNodeCode4, line.getId().charAt(40));

        UcteLine ucteLine1 = new UcteLine(ucteElementId1, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2, (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), "");
        UcteLine ucteLine2 = new UcteLine(ucteElementId2, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2, (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), "");

        ucteNetwork.addLine(ucteLine1);
        ucteNetwork.addLine(ucteLine2);
    }

    private void convertTwoWindingTransformers(Network network, UcteNetwork ucteNetwork) {
        LOGGER.debug("Converting two winding transformers");
        network.getTwoWindingsTransformerStream()
                .forEach(twoWindingsTransformer -> convertTwoWindingTransformer(ucteNetwork, twoWindingsTransformer));
        LOGGER.debug("two winding transformers converted");
    }

    private void convertTwoWindingTransformer(UcteNetwork ucteNetwork, TwoWindingsTransformer twoWindingsTransformer) {
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        UcteNodeCode ucteNodeCode = convertUcteNodeCode(
                terminal1.getBusBreakerView().getConnectableBus().getId(),
                terminal1.getVoltageLevel(),
                terminal1.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode2 = convertUcteNodeCode(
                terminal2.getBusBreakerView().getConnectableBus().getId(),
                terminal2.getVoltageLevel(),
                terminal2.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteElementId ucteElementId = convertUcteElementId(ucteNodeCode, ucteNodeCode2, twoWindingsTransformer, terminal1, terminal2);
        UcteTransformer ucteTransformer = new UcteTransformer(
                ucteElementId,
                UcteElementStatus.fromCode(1),
                (float) twoWindingsTransformer.getR(),
                (float) twoWindingsTransformer.getX(),
                (float) twoWindingsTransformer.getB(),
                (int) twoWindingsTransformer.getCurrentLimits2().getPermanentLimit(),
                twoWindingsTransformer.getName(),
                (float) twoWindingsTransformer.getRatedU2(),
                (float) twoWindingsTransformer.getRatedU1(),
                100,
                (float) twoWindingsTransformer.getG()); //TODO Find a representation for the nominal power

        convertRegulation(ucteNetwork, ucteElementId, twoWindingsTransformer);
        ucteNetwork.addTransformer(ucteTransformer);

    }

    private void convertRegulation(UcteNetwork ucteNetwork, UcteElementId ucteElementId, TwoWindingsTransformer twoWindingsTransformer) {
        if (twoWindingsTransformer.getRatioTapChanger() != null || twoWindingsTransformer.getPhaseTapChanger() != null) {
            UctePhaseRegulation uctePhaseRegulation = null;
            UcteAngleRegulation ucteAngleRegulation = null;
            if (twoWindingsTransformer.getRatioTapChanger() != null) {
                uctePhaseRegulation = convertRatioTapChanger(twoWindingsTransformer);
            }
            if (twoWindingsTransformer.getPhaseTapChanger() != null) {
                ucteAngleRegulation = convertPhaseTapChanger(twoWindingsTransformer);
            }
            UcteRegulation ucteRegulation = new UcteRegulation(ucteElementId, uctePhaseRegulation, ucteAngleRegulation);
            ucteNetwork.addRegulation(ucteRegulation);
        }
    }

    private void convertRegulation(UcteNetwork ucteNetwork, UcteElementId ucteElementId, ThreeWindingsTransformer threeWindingsTransformer) {
        if (threeWindingsTransformer.getLeg2().getRatioTapChanger() != null) {
            UctePhaseRegulation uctePhaseRegulation = convertRatioTapChanger(threeWindingsTransformer);
            UcteRegulation ucteRegulation = new UcteRegulation(ucteElementId, uctePhaseRegulation, null);
            ucteNetwork.addRegulation(ucteRegulation);
        }
    }

    private void convertThreeWindingTransformers(Network network, UcteNetwork ucteNetwork) {
        LOGGER.debug("Converting iidm ThreeWindingTransformers");
        network.getThreeWindingsTransformerStream()
                .forEach(threeWindingsTransformer -> convertThreeWindingTransformer(ucteNetwork, threeWindingsTransformer));
        LOGGER.debug("iidm ThreeWindingTransformers converted");
    }

    private void convertThreeWindingTransformer(UcteNetwork ucteNetwork, ThreeWindingsTransformer twt) {
        Terminal t1 = twt.getLeg1().getTerminal();
        Terminal t2 = twt.getLeg2().getTerminal();
        Terminal t3 = twt.getLeg3().getTerminal();

        UcteNodeCode fictiveNodeCode = new UcteNodeCode(
                UcteCountryCode.valueOf(twt.getSubstation().getCountry().toString()),
                "FICTIVE",
                UcteVoltageLevelCode.VL_500,
                '1');

        UcteNode fictiveNode = new UcteNode(
                fictiveNodeCode,
                "FICTIVE",
                UcteNodeStatus.EQUIVALENT,
                UcteNodeTypeCode.PQ,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null);

        UcteNodeCode ucteNodeCode1 = convertUcteNodeCode(
                t1.getBusBreakerView().getConnectableBus().getId(),
                t1.getVoltageLevel(),
                t1.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode2 = convertUcteNodeCode(
                t2.getBusBreakerView().getConnectableBus().getId(),
                t2.getVoltageLevel(),
                t2.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode3 = convertUcteNodeCode(
                t3.getBusBreakerView().getConnectableBus().getId(),
                t3.getVoltageLevel(),
                t3.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteElementId ucteElementId1 = new UcteElementId(ucteNodeCode1, fictiveNodeCode, '1');
        UcteElementId ucteElementId2 = new UcteElementId(ucteNodeCode2, fictiveNodeCode, '1');
        UcteElementId ucteElementId3 = new UcteElementId(ucteNodeCode3, fictiveNodeCode, '1');

        UcteTransformer transformer1 = new UcteTransformer(
                ucteElementId1,
                UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                (float) twt.getLeg1().getR(),
                (float) twt.getLeg1().getX(),
                (float) twt.getLeg1().getB(),
                (int) twt.getLeg1().getCurrentLimits().getPermanentLimit(),
                twt.getName(),
                (float) twt.getLeg1().getRatedU(),
                (float) twt.getLeg1().getRatedU(),
                100,
                (float) twt.getLeg1().getG());

        UcteTransformer transformer2 = new UcteTransformer(
                ucteElementId2,
                UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                (float) twt.getLeg2().getR(),
                (float) twt.getLeg2().getX(),
                (float) twt.getLeg1().getB(),
                (int) twt.getLeg2().getCurrentLimits().getPermanentLimit(),
                twt.getName(),
                (float) twt.getLeg2().getRatedU(),
                (float) twt.getLeg2().getRatedU(),
                100,
                (float) twt.getLeg1().getG());

        UcteTransformer transformer3 = new UcteTransformer(
                ucteElementId3,
                UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                (float) twt.getLeg3().getR(),
                (float) twt.getLeg3().getX(),
                (float) twt.getLeg1().getB(),
                (int) twt.getLeg3().getCurrentLimits().getPermanentLimit(),
                twt.getName(),
                (float) twt.getLeg3().getRatedU(),
                (float) twt.getLeg3().getRatedU(),
                100,
                (float) twt.getLeg1().getG());

        ucteNetwork.addNode(fictiveNode);
        ucteNetwork.addTransformer(transformer1);
        ucteNetwork.addTransformer(transformer2);
        ucteNetwork.addTransformer(transformer3);

        convertRegulation(ucteNetwork, ucteElementId1, twt);
    }

    private UctePhaseRegulation convertRatioTapChanger(TwoWindingsTransformer twoWindingsTransformer) {
        LOGGER.debug("Converting iidm ratio tap changer of transformer {}", twoWindingsTransformer.getId());

        float du = (float) calculatePhaseDu(twoWindingsTransformer);
        UctePhaseRegulation uctePhaseRegulation = new UctePhaseRegulation(
                du,
                twoWindingsTransformer.getRatioTapChanger().getHighTapPosition(),
                twoWindingsTransformer.getRatioTapChanger().getTapPosition(),
                Float.NaN);
        if (!Double.isNaN(twoWindingsTransformer.getRatioTapChanger().getTargetV())) {
            uctePhaseRegulation.setU((float) twoWindingsTransformer.getRatioTapChanger().getTargetV());
        }
        return uctePhaseRegulation;
    }

    private UctePhaseRegulation convertRatioTapChanger(ThreeWindingsTransformer threeWindingsTransformer) {
        LOGGER.debug("Converting iidm ratio tap changer of transformer {}", threeWindingsTransformer.getId());

        float du = (float) calculatePhaseDu(threeWindingsTransformer);
        UctePhaseRegulation uctePhaseRegulation = new UctePhaseRegulation(
                du,
                threeWindingsTransformer.getLeg2().getRatioTapChanger().getHighTapPosition(),
                threeWindingsTransformer.getLeg2().getRatioTapChanger().getTapPosition(),
                Float.NaN);
        if (!Double.isNaN(threeWindingsTransformer.getLeg2().getRatioTapChanger().getTargetV())) {
            uctePhaseRegulation.setU((float) threeWindingsTransformer.getLeg2().getRatioTapChanger().getTargetV());
        }
        return uctePhaseRegulation;
    }

    private UcteAngleRegulation convertPhaseTapChanger(TwoWindingsTransformer twoWindingsTransformer) {
        LOGGER.debug("Converting iidm Phase tap changer of transformer {}", twoWindingsTransformer.getId());
        return new UcteAngleRegulation(calculateAngleDu(twoWindingsTransformer),
                calculateAngleTheta(twoWindingsTransformer),
                twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition(),
                twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                calculateAngleP(twoWindingsTransformer),
                UcteAngleRegulationType.SYMM); //TODO SYMM and ASYM
    }

    double calculatePhaseDu(TwoWindingsTransformer twoWindingsTransformer) {
        double du = 0;
        boolean passedBy0 = false;
        for (int i = twoWindingsTransformer.getRatioTapChanger().getLowTapPosition();
             i < twoWindingsTransformer.getRatioTapChanger().getHighTapPosition();
             i++) {
            if (i != 0) {
                double rho = twoWindingsTransformer.getRatioTapChanger().getStep(i).getRho();
                du += (100 * (1 / rho - 1)) / i;
            } else { //i == 0
                passedBy0 = true;
            }
        }
        if (passedBy0) {
            return du /
                    (twoWindingsTransformer.getRatioTapChanger().getHighTapPosition() -
                            twoWindingsTransformer.getRatioTapChanger().getLowTapPosition() - 1);
        } else {
            return du /
                    (twoWindingsTransformer.getRatioTapChanger().getHighTapPosition() -
                            twoWindingsTransformer.getRatioTapChanger().getLowTapPosition());
        }
    }

    double calculatePhaseDu(ThreeWindingsTransformer threeWindingsTransformer) {
        double du = 0;
        boolean passedBy0 = false;
        for (int i = threeWindingsTransformer.getLeg2().getRatioTapChanger().getLowTapPosition();
             i < threeWindingsTransformer.getLeg2().getRatioTapChanger().getHighTapPosition();
             i++) {
            if (i != 0) {
                double rho = threeWindingsTransformer.getLeg2().getRatioTapChanger().getStep(i).getRho();
                du += (100 * (1 / rho - 1)) / i;
            } else { //i == 0
                passedBy0 = true;
            }
        }
        if (passedBy0) {
            return du /
                    (threeWindingsTransformer.getLeg2().getRatioTapChanger().getHighTapPosition() -
                            threeWindingsTransformer.getLeg2().getRatioTapChanger().getLowTapPosition() - 1);
        } else {
            return du /
                    (threeWindingsTransformer.getLeg2().getRatioTapChanger().getHighTapPosition() -
                            threeWindingsTransformer.getLeg2().getRatioTapChanger().getLowTapPosition());
        }
    }

    float calculateAngleDu(TwoWindingsTransformer twoWindingsTransformer) { //TODO need to find a way to calculate this value
        return 0;
    }

    float calculateAngleTheta(TwoWindingsTransformer twoWindingsTransformer) { //TODO need to find a way to calculate this value
        return 0;
    }

    float calculateAngleP(TwoWindingsTransformer twoWindingsTransformer) { //TODO need to find a way to calculate this value
        return 0;
    }

    private void convertLines(UcteNetwork ucteNetwork, Network network) {
        LOGGER.debug("Converting iidm lines");
        network.getLineStream().forEach(line -> convertLine(ucteNetwork, line));
        LOGGER.debug("iidm lines converted");
    }

    private UcteNetwork createUcteNetwork(Network network) {
        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        network.getSubstationStream().forEach(substation ->
                substation.getVoltageLevelStream().forEach(voltageLevel -> {
                    convertBuses(ucteNetwork, voltageLevel);
                    convertTwoWindingTransformers(network, ucteNetwork);
                    convertThreeWindingTransformers(network, ucteNetwork);
                    convertSwitches(ucteNetwork, voltageLevel);
                    voltageLevel.getDanglingLineStream().forEach(danglingLine -> convertDanglingLine(ucteNetwork, danglingLine));
                })
        );
        convertLines(ucteNetwork, network);
        return ucteNetwork;
    }

    private void convertDanglingLine(UcteNetwork ucteNetwork, DanglingLine danglingLine) {
        LOGGER.debug("Converting dangling line {}", danglingLine.getId());

        ucteNetwork.addNode(new UcteNode(
                iidmIdToUcteNodeCode(danglingLine.getUcteXnodeCode()),
                "",
                UcteNodeStatus.REAL,
                UcteNodeTypeCode.PQ,
                (float) danglingLine.getTerminal().getVoltageLevel().getNominalV(),
                (float) danglingLine.getP0(),
                (float) danglingLine.getQ0(),
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null
        ));
        if (danglingLine.getId().length() == 19) { // It's (probably) a ucte id
            UcteNodeCode ucteNodeCode1 = iidmIdToUcteNodeCode(danglingLine.getId().substring(0, 8));
            UcteNodeCode ucteNodeCode2 = iidmIdToUcteNodeCode(danglingLine.getId().substring(9, 17));
            UcteElementId ucteElementId = new UcteElementId(ucteNodeCode1, ucteNodeCode2, danglingLine.getId().charAt(18));
            UcteLine ucteLine = new UcteLine(ucteElementId,
                    UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                    (float) danglingLine.getR(),
                    (float) danglingLine.getX(),
                    (float) danglingLine.getB(),
                    (int) danglingLine.getCurrentLimits().getPermanentLimit(),
                    danglingLine.getName());
            ucteNetwork.addLine(ucteLine);
        }
    }

    private void convertSwitches(UcteNetwork ucteNetwork, VoltageLevel voltageLevel) {
        Iterable<Switch> switchIterator = voltageLevel.getSwitches();
        for (Switch sw : switchIterator) {
            LOGGER.debug("Converting switch {}", sw.getId());
            if (isUcteId(sw.getId())) {
                UcteNodeCode ucteNodeCode1 = iidmIdToUcteNodeCode(sw.getId().substring(0, 9));
                UcteNodeCode ucteNodeCode2 = iidmIdToUcteNodeCode(sw.getId().substring(9, 18));
                UcteElementId ucteElementId = new UcteElementId(ucteNodeCode1, ucteNodeCode2, sw.getId().charAt(18));
                UcteLine ucteLine = new UcteLine(ucteElementId, UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION,
                        Float.NaN, Float.NaN, Float.NaN, null, null); //CurrentLimit for switches are not stored in iidm
                ucteNetwork.addLine(ucteLine);
            }
        }
    }

    private void convertLine(UcteNetwork ucteNetwork, Line line) {
        if (line.getId().length() > 20 && line.getId().charAt(20) == '+') { //it's a tie line
            convertTieLine(ucteNetwork, line);
            return;
        }

        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        UcteNodeCode ucteTerminal1NodeCode = convertUcteNodeCode(
                terminal1.getBusBreakerView().getConnectableBus().getId(),
                terminal1.getVoltageLevel(),
                terminal1.getVoltageLevel().getSubstation().getCountry().toString());

        UcteNodeCode ucteTerminal2NodeCode = convertUcteNodeCode(
                terminal2.getBusBreakerView().getConnectableBus().getId(),
                terminal2.getVoltageLevel(),
                terminal2.getVoltageLevel().getSubstation().getCountry().toString());

        UcteElementId lineId = new UcteElementId(ucteTerminal1NodeCode, ucteTerminal2NodeCode, '1');
        UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                (float) line.getR(), (float) line.getX(), (float) line.getB1() + (float) line.getB2(), (int) line.getCurrentLimits1().getPermanentLimit(), null);

        ucteNetwork.addLine(ucteLine);
    }

    private void convertBuses(UcteNetwork ucteNetwork, VoltageLevel voltageLevel) {
        VoltageLevel.BusBreakerView busBreakerView = voltageLevel.getBusBreakerView();
        busBreakerView.getBusStream().forEach(bus -> {
            LOGGER.debug("Converting bus {}", bus.getId());
            convertBus(ucteNetwork, bus);
        });
    }

    private void convertBus(UcteNetwork ucteNetwork, Bus bus) {
        VoltageLevel voltageLevel = bus.getVoltageLevel();
        long loadCount = voltageLevel.getLoadStream().count();
        long generatorCount = bus.getGeneratorStream().count();
        String country = voltageLevel.getSubstation().getCountry().toString();

        float p0 = 0;
        float q0 = 0;
        float activePowerGeneration = 0;
        float reactivePowerGeneration = 0;
        float voltageReference = 0;
        float minimumPermissibleActivePowerGeneration = Float.NaN;
        float maximumPermissibleActivePowerGeneration = Float.NaN;
        float minimumPermissibleReactivePowerGeneration = Float.NaN;
        float maximumPermissibleReactivePowerGeneration = Float.NaN;
        UcteNodeTypeCode ucteNodeTypeCode = UcteNodeTypeCode.PQ;
        UctePowerPlantType uctePowerPlantType = null;

        if (loadCount == 1) {
            Load load = (Load) voltageLevel.getLoadStream().toArray()[0];
            p0 = (float) load.getP0();
            q0 = (float) load.getQ0();
        }

        if (generatorCount == 1) { //the node is a generator
            Generator generator = (Generator) bus.getGeneratorStream().toArray()[0];
            activePowerGeneration = (float) -generator.getTargetP();
            reactivePowerGeneration = (float) -generator.getTargetQ();
            voltageReference = (float) generator.getTargetV();

            minimumPermissibleActivePowerGeneration = -generator.getMinP() >= 9999 ? Float.NaN : (float) -generator.getMinP();
            maximumPermissibleActivePowerGeneration = -generator.getMaxP() <= -9999 ? Float.NaN : (float) -generator.getMaxP();
            minimumPermissibleReactivePowerGeneration =
                    -generator.getReactiveLimits().getMinQ(activePowerGeneration) >= 9999 ? Float.NaN :
                            (float) -generator.getReactiveLimits().getMinQ(activePowerGeneration);
            maximumPermissibleReactivePowerGeneration =
                    -generator.getReactiveLimits().getMaxQ(activePowerGeneration) <= -9999 ? Float.NaN :
                            (float) -generator.getReactiveLimits().getMaxQ(activePowerGeneration);

            if (generator.isVoltageRegulatorOn() && generator.getRegulatingTerminal().isConnected()) {
                ucteNodeTypeCode = UcteNodeTypeCode.PU;
            }
            uctePowerPlantType = energySourceToUctePowerPlantType(generator.getEnergySource());
        }

        UcteNodeCode ucteNodeCode = convertUcteNodeCode(bus.getId(), voltageLevel, country);

        UcteNode ucteNode = new UcteNode(
                ucteNodeCode,
                voltageLevel.getSubstation().getName(),
                UcteNodeStatus.REAL,
                ucteNodeTypeCode,
                voltageReference,
                p0,
                q0,
                activePowerGeneration,
                reactivePowerGeneration,
                minimumPermissibleActivePowerGeneration,
                maximumPermissibleActivePowerGeneration,
                minimumPermissibleReactivePowerGeneration,
                maximumPermissibleReactivePowerGeneration,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null
        );
        ucteNode.setPowerPlantType(uctePowerPlantType);
        ucteNetwork.addNode(ucteNode);
    }

    UcteNodeCode iidmIdToUcteNodeCode(String id) {
        if (id.length() >= 8) {
            UcteCountryCode ucteCountryCode = UcteCountryCode.fromUcteCode(id.charAt(0));
            return new UcteNodeCode(
                    ucteCountryCode,
                    id.substring(1, 6),
                    voltageLevelCodeFromChar(id.charAt(6)),
                    id.charAt(7));
        } else {
            throw new IllegalArgumentException("The id length must be higher than 7");
        }
    }

    UcteVoltageLevelCode voltageLevelCodeFromChar(char code) {
        if (code == '0') {
            return UcteVoltageLevelCode.VL_750;
        } else if (code == '1') {
            return UcteVoltageLevelCode.VL_380;
        } else if (code == '2') {
            return UcteVoltageLevelCode.VL_220;
        } else if (code == '3') {
            return UcteVoltageLevelCode.VL_150;
        } else if (code == '4') {
            return UcteVoltageLevelCode.VL_120;
        } else if (code == '5') {
            return UcteVoltageLevelCode.VL_110;
        } else if (code == '6') {
            return UcteVoltageLevelCode.VL_70;
        } else if (code == '7') {
            return UcteVoltageLevelCode.VL_27;
        } else if (code == '8') {
            return UcteVoltageLevelCode.VL_330;
        } else if (code == '9') {
            return UcteVoltageLevelCode.VL_500;
        } else {
            throw new IllegalArgumentException("This code doesn't refer to a voltage level");
        }
    }

    UcteVoltageLevelCode iidmVoltageToUcteVoltageLevelCode(double nominalV) {
        if (nominalV == 27) {
            return UcteVoltageLevelCode.VL_27;
        } else if (nominalV == 70) {
            return UcteVoltageLevelCode.VL_70;
        } else if (nominalV == 110) {
            return UcteVoltageLevelCode.VL_110;
        } else if (nominalV == 120) {
            return UcteVoltageLevelCode.VL_120;
        } else if (nominalV == 150) {
            return UcteVoltageLevelCode.VL_150;
        } else if (nominalV == 220) {
            return UcteVoltageLevelCode.VL_220;
        } else if (nominalV == 330) {
            return UcteVoltageLevelCode.VL_330;
        } else if (nominalV == 380) {
            return UcteVoltageLevelCode.VL_380;
        } else if (nominalV == 500) {
            return UcteVoltageLevelCode.VL_500;
        } else if (nominalV == 750) {
            return UcteVoltageLevelCode.VL_750;
        } else {
            throw new IllegalArgumentException("This voltage doesn't refer to a voltage level");
        }
    }

    UctePowerPlantType energySourceToUctePowerPlantType(EnergySource energySource) {
        if (EnergySource.HYDRO == energySource) {
            return UctePowerPlantType.H;
        } else if (EnergySource.NUCLEAR == energySource) {
            return UctePowerPlantType.N;
        } else if (EnergySource.THERMAL == energySource) {
            return UctePowerPlantType.C;
        } else if (EnergySource.WIND == energySource) {
            return UctePowerPlantType.W;
        } else {
            return UctePowerPlantType.F;
        }
    }

    UcteNodeCode convertUcteNodeCode(String id, VoltageLevel voltageLevel, String country) {
        UcteNodeCode ucteNodeCode;
        if (iidmIdToUcteId.containsKey(id)) {
            return iidmIdToUcteId.get(id);
        }
        if (isUcteNodeId(id)) { // the ID is already an UCTE id
            ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(country),
                    id.substring(1, 6),
                    iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                    id.charAt(7)
            );
        } else {
            ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(country),
                    voltageLevel.getSubstation().getName(),
                    iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                    '1'
            );
            iidmIdToUcteId.put(id, ucteNodeCode);
        }
        return ucteNodeCode;
    }

    boolean isUcteId(String id) {
        return id != null &&
                id.length() >= 17 &&
                isUcteNodeId(id.substring(0, 8)) &&
                isUcteNodeId(id.substring(9, 17)) &&
                id.charAt(8) == ' ' &&
                id.charAt(17) == ' ';
    }

    boolean isUcteNodeId(String id) {
        return id != null &&
                id.length() == 8 &&
                isUcteCountryCode(id.charAt(0)) &&
                isVoltageLevel(id.charAt(6));
    }

    UcteElementId convertUcteElementId(UcteNodeCode ucteNodeCode, UcteNodeCode ucteNodeCode2, TwoWindingsTransformer twoWindingsTransformer, Terminal terminal1, Terminal terminal2) {
        if (isUcteNodeId(terminal1.getBusBreakerView().getConnectableBus().getId()) &&
                isUcteNodeId(terminal2.getBusBreakerView().getConnectableBus().getId())) {
            return new UcteElementId(
                    ucteNodeCode,
                    ucteNodeCode2,
                    twoWindingsTransformer.getId().charAt(18));
        } else {
            return new UcteElementId(
                    ucteNodeCode,
                    ucteNodeCode2,
                    '1');
        }
    }

    boolean isUcteCountryCode(char character) {
        try {
            UcteCountryCode ucteCountryCode = UcteCountryCode.fromUcteCode(character);
            LOGGER.debug("isUcteCountryCode {} : {}", character, ucteCountryCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    boolean isVoltageLevel(char character) {
        return (int) character >= 48 && (int) character <= 57;
    }

}
