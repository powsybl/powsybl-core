/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.google.common.primitives.Doubles;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.*;
import com.powsybl.ucte.network.io.UcteWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static com.powsybl.ucte.converter.UcteImporter.ELEMENT_NAME_PROPERTY_KEY;
import static com.powsybl.ucte.network.UcteNodeCode.isUcteNodeId;
import static com.powsybl.ucte.network.UcteNodeCode.parseUcteNodeCode;
import static com.powsybl.ucte.network.UcteVoltageLevelCode.voltageLevelCodeFromIidmVoltage;

/**
 *
 * @author Abdelsalem HEDHILI < abdelsalem.hedhili at rte-france.com >
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    HashMap<String, UcteNodeCode> iidmIdToUcteNodeCodeId = new HashMap<>();
    private HashMap<String, UcteElementId> iidmIdToUcteElementId = new HashMap<>();
    private String generatedGeographicalName = "aaaa";

    @Override
    public String getFormat() {
        return "UCTE";
    }

    @Override
    public String getComment() {
        return "IIDM to UCTE converter";
    }



    /**
     * {@inheritDoc}
     */
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

    /**
     * @param network the model
     * @return the UcteNetwork corresponding to the iidm network
     */
    private UcteNetwork createUcteNetwork(Network network) {
        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        network.getSubstationStream().forEach(substation ->
                substation.getVoltageLevelStream().forEach(voltageLevel -> {
                    convertBuses(ucteNetwork, voltageLevel);
                    convertSwitches(ucteNetwork, voltageLevel);
                    voltageLevel.getDanglingLineStream().forEach(danglingLine -> convertDanglingLine(ucteNetwork, danglingLine));
                })
        );
        convertLines(ucteNetwork, network);
        convertTwoWindingsTransformers(network, ucteNetwork);
        convertThreeWindingTransformers(network, ucteNetwork);
        return ucteNetwork;
    }

    /**
     * Iterate through the given {@link VoltageLevel}, get the buses and calls the method to convert them
     *
     * @param ucteNetwork the target network in ucte
     * @param voltageLevel the iidm VoltageLevel containing the buses we want to convert
     * @see UcteExporter#convertBus(UcteNetwork, Bus)
     */
    private void convertBuses(UcteNetwork ucteNetwork, VoltageLevel voltageLevel) {
        VoltageLevel.BusBreakerView busBreakerView = voltageLevel.getBusBreakerView();
        busBreakerView.getBusStream().forEach(bus -> {
            LOGGER.debug("Converting bus {}", bus.getId());
            convertBus(ucteNetwork, bus);
        });
    }

    /**
     * Gets the needed information from the bus to create the corresponding {@link UcteNode},
     * create the {@link UcteNode} and add it to the {@link UcteNetwork}
     *
     * @param ucteNetwork the target network in ucte
     * @param bus the bus to convert to UCTE
     */
    private void convertBus(UcteNetwork ucteNetwork, Bus bus) {
        VoltageLevel voltageLevel = bus.getVoltageLevel();
        long loadCount = voltageLevel.getLoadStream().count();
        Load load = loadCount == 1 ? (Load) voltageLevel.getLoadStream().toArray()[0] : null;
        long generatorCount = bus.getGeneratorStream().count();
        String country = voltageLevel.getSubstation().getCountry().toString();
        String geographicalName = null;
        float p0 = 0;
        float q0 = 0;
        float activePowerGeneration = 0;
        float reactivePowerGeneration = 0;
        float voltageReference = Float.NaN;
        float minimumPermissibleActivePowerGeneration = Float.NaN;
        float maximumPermissibleActivePowerGeneration = Float.NaN;
        float minimumPermissibleReactivePowerGeneration = Float.NaN;
        float maximumPermissibleReactivePowerGeneration = Float.NaN;
        UcteNodeTypeCode ucteNodeTypeCode = UcteNodeTypeCode.PQ;
        UctePowerPlantType uctePowerPlantType = null;

        if (!Double.isNaN(bus.getV())) {
            voltageReference = (float) bus.getV();
        }

        if (load != null && load.getTerminal().getBusBreakerView().getBus().getId().equals(bus.getId())) {
            p0 = (float) load.getP0();
            q0 = (float) load.getQ0();
        }

        if (generatorCount == 1) { //the node is a generator
            Generator generator = (Generator) bus.getGeneratorStream().toArray()[0];
            activePowerGeneration = (float) -generator.getTargetP();
            reactivePowerGeneration = (float) -generator.getTargetQ();
            voltageReference = (float) generator.getTargetV();

            minimumPermissibleActivePowerGeneration = -generator.getMinP() >= UcteNode.getDefaultPowerLimit() ? Float.NaN : (float) -generator.getMinP();
            maximumPermissibleActivePowerGeneration = -generator.getMaxP() <= -UcteNode.getDefaultPowerLimit() ? Float.NaN : (float) -generator.getMaxP();
            minimumPermissibleReactivePowerGeneration =
                    -generator.getReactiveLimits().getMinQ(activePowerGeneration) >= UcteNode.getDefaultPowerLimit() ? Float.NaN :
                            (float) -generator.getReactiveLimits().getMinQ(activePowerGeneration);
            maximumPermissibleReactivePowerGeneration =
                    -generator.getReactiveLimits().getMaxQ(activePowerGeneration) <= -UcteNode.getDefaultPowerLimit() ? Float.NaN :
                            (float) -generator.getReactiveLimits().getMaxQ(activePowerGeneration);

            if (generator.isVoltageRegulatorOn() && generator.getRegulatingTerminal().isConnected()) {
                ucteNodeTypeCode = UcteNodeTypeCode.PU;
            }
            uctePowerPlantType = energySourceToUctePowerPlantType(generator.getEnergySource());
        }

        if (bus.getProperties().containsKey(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY)) {
            geographicalName = bus.getProperties().getProperty(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY);
        }

        UcteNodeCode ucteNodeCode = createUcteNodeCode(bus.getId(), voltageLevel, country);

        UcteNode ucteNode = new UcteNode(
                ucteNodeCode,
                geographicalName,
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

    /**
     * Iterates through the {@link Line}s of the iidm {@link Network} and call the method to convert them
     *
     * @param ucteNetwork the target network in ucte
     * @param network the iidm network containing the buses we want to convert
     * @see UcteExporter#convertLine(UcteNetwork, Line)
     */
    private void convertLines(UcteNetwork ucteNetwork, Network network) {
        LOGGER.debug("Converting iidm lines");
        network.getLineStream().forEach(line -> convertLine(ucteNetwork, line));
        LOGGER.debug("iidm lines converted");
    }

    /**
     *  Checks if the line is a regular line and if so, get the needed information to create the corresponding {@link UcteLine}
     * and adds it to the {@link UcteNetwork}. Otherwise, if the line is a {@link TieLine} calls another method to convert it
     * ({@link UcteExporter#convertTieLine(UcteNetwork, Line)})
     *
     * @param ucteNetwork The target network in ucte
     * @param line The line to convert to {@link UcteLine}
     * @see UcteExporter#convertTieLine(UcteNetwork, Line)
     */
    private void convertLine(UcteNetwork ucteNetwork, Line line) {
        if (line.isTieLine()) {
            LOGGER.debug("exporting tie line {}", line.getId());
            convertTieLine(ucteNetwork, line);
            return;
        }

        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        UcteNodeCode ucteTerminal1NodeCode = createUcteNodeCode(
                terminal1.getBusBreakerView().getConnectableBus().getId(),
                terminal1.getVoltageLevel(),
                terminal1.getVoltageLevel().getSubstation().getCountry().toString());

        UcteNodeCode ucteTerminal2NodeCode = createUcteNodeCode(
                terminal2.getBusBreakerView().getConnectableBus().getId(),
                terminal2.getVoltageLevel(),
                terminal2.getVoltageLevel().getSubstation().getCountry().toString());

        String elementName = line.getProperties().containsKey(ELEMENT_NAME_PROPERTY_KEY) ?
                line.getProperties().getProperty(ELEMENT_NAME_PROPERTY_KEY) : null;

        UcteElementId lineId = convertUcteElementId(ucteTerminal1NodeCode, ucteTerminal2NodeCode, line.getId(), terminal1, terminal2);
        UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                (float) line.getR(), (float) line.getX(), (float) line.getB1() + (float) line.getB2(),
                (int) line.getCurrentLimits1().getPermanentLimit(), elementName);

        ucteNetwork.addLine(ucteLine);
    }

    /**
     * Gets the needed information from the dangling line to create the Xnode ({@link UcteNode} and then calls
     * {@link UcteExporter#createLineFromDanglingLine(UcteNetwork, DanglingLine)} to create the {@link UcteLine} and to add it to
     * the given ucteNetwork
     *
     * @param ucteNetwork The target network in ucte
     * @param danglingLine The danglingLine to convert to UCTE
     */
    private void convertDanglingLine(UcteNetwork ucteNetwork, DanglingLine danglingLine) {
        LOGGER.debug("Converting dangling line {}", danglingLine.getId());
        Optional<UcteNodeCode> optUcteXNodeCode = parseUcteNodeCode(danglingLine.getUcteXnodeCode());
        if (optUcteXNodeCode.isPresent()) {
            String geographicalName = danglingLine.getProperties().containsKey(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY) ?
                    danglingLine.getProperties().getProperty(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY) : null;
            float referenceVoltage = danglingLine.getProperties().containsKey(UcteImporter.REFERENCE_VOLTAGE) ?
                    Float.parseFloat(danglingLine.getProperties().getProperty(UcteImporter.REFERENCE_VOLTAGE)) : Float.NaN;

            ucteNetwork.addNode(new UcteNode(
                    optUcteXNodeCode.get(),
                    geographicalName,
                    UcteNodeStatus.EQUIVALENT,
                    UcteNodeTypeCode.PQ,
                    referenceVoltage,
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
            createLineFromDanglingLine(ucteNetwork, danglingLine);
        }
    }

    /**
     * Checks if the dangling line given contains an UCTE-compliant id. If it's the case, it creates the dangling line
     * and adds it to the ucteNetwork. Otherwise, it calls {@link UcteExporter#createLineFromNonCompliantIdDanglingLine(UcteNetwork, DanglingLine)}
     * to do specific handling to convert the dangling line
     *
     * @param ucteNetwork The target network in ucte
     * @param danglingLine The danglingLine to convert to UCTE
     * @see UcteExporter#createLineFromNonCompliantIdDanglingLine(UcteNetwork, DanglingLine)
     */
    private void createLineFromDanglingLine(UcteNetwork ucteNetwork, DanglingLine danglingLine) {
        if (danglingLine.getId().length() == 19) { // It's probably a ucte id
            Optional<UcteNodeCode> optUcteNodeCode1 = parseUcteNodeCode(danglingLine.getId().substring(0, 8));
            Optional<UcteNodeCode> optUcteNodeCode2 = parseUcteNodeCode(danglingLine.getId().substring(9, 17));
            if (optUcteNodeCode1.isPresent() && optUcteNodeCode2.isPresent()) { //it is a ucte id
                String elementName = danglingLine.getProperties().containsKey(ELEMENT_NAME_PROPERTY_KEY) ?
                        danglingLine.getProperties().getProperty(ELEMENT_NAME_PROPERTY_KEY) : null;
                UcteElementId ucteElementId = new UcteElementId(optUcteNodeCode1.get(), optUcteNodeCode2.get(), danglingLine.getId().charAt(18));
                UcteLine ucteLine = new UcteLine(ucteElementId,
                        UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                        (float) danglingLine.getR(),
                        (float) danglingLine.getX(),
                        (float) danglingLine.getB(),
                        (int) danglingLine.getCurrentLimits().getPermanentLimit(),
                        elementName);
                ucteNetwork.addLine(ucteLine);
            } else { //it is not a ucte id
                createLineFromNonCompliantIdDanglingLine(ucteNetwork, danglingLine);
            }
        } else {  //it is not a ucte id
            createLineFromNonCompliantIdDanglingLine(ucteNetwork, danglingLine);
        }
    }

    void createLineFromNonCompliantIdDanglingLine(UcteNetwork ucteNetwork, DanglingLine danglingLine) {
        String ucteXnodeCode = null;
        if (danglingLine.getUcteXnodeCode() != null) {
            ucteXnodeCode = danglingLine.getUcteXnodeCode();
        } else { //FIXME how to handle the case when the ucteXnodeCode is not there ?
            ucteXnodeCode = new UcteNodeCode(
                    UcteCountryCode.XX, //OK
                    "aaaaaa", //FIXME
                    UcteVoltageLevelCode.VL_500, //FIXME
                    '1').toString(); //FIXME
        }
        UcteNodeCode ucteNodeCode1 = createUcteNodeCode(ucteXnodeCode,
                danglingLine.getTerminal().getVoltageLevel(),
                UcteCountryCode.XX.toString());
        UcteNodeCode ucteNodeCode2 = createUcteNodeCode(danglingLine.getTerminal().getBusBreakerView().getConnectableBus().getId(),
                danglingLine.getTerminal().getBusBreakerView().getConnectableBus().getVoltageLevel(),
                danglingLine.getTerminal().getBusBreakerView().getConnectableBus().getVoltageLevel().getSubstation().getCountry().toString());
        UcteElementId ucteElementId = generateUcteElementId(danglingLine.getId(), ucteNodeCode1, ucteNodeCode2);

        UcteLine ucteLine = new UcteLine(ucteElementId,
                UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION,
                (float) danglingLine.getR(),
                (float) danglingLine.getX(),
                (float) danglingLine.getB(),
                (int) danglingLine.getCurrentLimits().getPermanentLimit(),
                null);
        ucteNetwork.addLine(ucteLine);
    }

    /**
     * Iterates through the voltageLevel to get all the switches and create the corresponding {@link UcteLine} for
     * each switch. If the switch id is not UCTE-compliant we go through different steps to generate an Ucte-compliant id.
     * Add the  {@link UcteLine}s created (corresponding to the switches to the ucteNetwork
     *
     * @param ucteNetwork The target UcteNetwork
     * @param voltageLevel The VoltageLevel containing the switches we want to convert.
     * @see UcteExporter#createUcteNodeCode(String, VoltageLevel, String)
     * @see UcteExporter#generateUcteElementId(String, UcteNodeCode, UcteNodeCode)
     */
    private void convertSwitches(UcteNetwork ucteNetwork, VoltageLevel voltageLevel) {
        Iterable<Switch> switchIterator = voltageLevel.getBusBreakerView().getSwitches();
        for (Switch sw : switchIterator) {
            LOGGER.debug("Converting switch {}", sw.getId());

            Optional<UcteNodeCode> optUcteNodeCode1 = parseUcteNodeCode(sw.getId().substring(0, 8));
            Optional<UcteNodeCode> optUcteNodeCode2 = parseUcteNodeCode(sw.getId().substring(9, 17));

            UcteElementStatus switchStatus = sw.isOpen() ? UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION :
                    UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION;

            if (optUcteNodeCode1.isPresent() && optUcteNodeCode2.isPresent()) {
                String elementName = sw.getProperties().containsKey(ELEMENT_NAME_PROPERTY_KEY) ?
                        sw.getProperties().getProperty(ELEMENT_NAME_PROPERTY_KEY) : null;
                UcteElementId ucteElementId = new UcteElementId(optUcteNodeCode1.get(), optUcteNodeCode2.get(), sw.getId().charAt(18));
                UcteLine ucteLine = new UcteLine(ucteElementId, switchStatus,
                        0, 0, 0, null, elementName);
                setSwitchCurrentLimit(ucteLine, sw);
                ucteNetwork.addLine(ucteLine);
            } else {
                Bus bus1 = voltageLevel.getBusBreakerView().getBus1(sw.getId());
                Bus bus2 = voltageLevel.getBusBreakerView().getBus2(sw.getId());
                UcteNodeCode ucteNodeCode1 = createUcteNodeCode(bus1.getId(), voltageLevel, voltageLevel.getSubstation().getCountry().toString());
                UcteNodeCode ucteNodeCode2 = createUcteNodeCode(bus2.getId(), voltageLevel, voltageLevel.getSubstation().getCountry().toString());
                UcteElementId ucteElementId = generateUcteElementId(sw.getId(), ucteNodeCode1, ucteNodeCode2);
                UcteLine ucteLine = new UcteLine(ucteElementId, switchStatus, 0, 0, 0, null, null);
                ucteNetwork.addLine(ucteLine);
            }
        }
    }

    /**
     * Converts the {@link Line} (being a {@link TieLine}) to two {@link UcteLine}
     * and a Xnode ({@link UcteExporter#createXnodeFromTieLine(UcteNetwork, UcteNodeCode, Line)}.
     * Then, adds them in the UcteNetwork.
     * If the tie line has a non-compliant UCTE id, calls {@link UcteExporter#createTieLineWithGeneratedIds(UcteNetwork, Line)}
     * to do the specific handling
     *
     * @param ucteNetwork The target UcteNetwork
     * @param line The TieLine we want to convert
     * @see UcteExporter#createXnodeFromTieLine(UcteNetwork, UcteNodeCode, Line)
     * @see UcteExporter#createTieLineWithGeneratedIds(UcteNetwork, Line)
     */
    private void convertTieLine(UcteNetwork ucteNetwork, Line line) {
        if (isUcteTieLineId(line)) {
            MergedXnode mergedXnode = line.getExtension(MergedXnode.class);
            String id1 = mergedXnode.getLine1Name().substring(0, 8); //first node line 1
            String id2 = mergedXnode.getLine1Name().substring(9, 17); //second node line 1
            String id3 = mergedXnode.getLine2Name().substring(0, 8); //first node line 2
            String id4 = mergedXnode.getLine2Name().substring(9, 17); //second node line 2

            Optional<UcteNodeCode> optUcteNodeCode1 = parseUcteNodeCode(id1);
            Optional<UcteNodeCode> optUcteNodeCode2 = parseUcteNodeCode(id2);
            Optional<UcteNodeCode> optUcteNodeCode3 = parseUcteNodeCode(id3);
            Optional<UcteNodeCode> optUcteNodeCode4 = parseUcteNodeCode(id4);

            if (optUcteNodeCode1.isPresent() && optUcteNodeCode2.isPresent() && optUcteNodeCode3.isPresent() && optUcteNodeCode4.isPresent()) {
                ArrayList<UcteNodeCode> ucteNodeCodeList = new ArrayList<>();
                ucteNodeCodeList.add(optUcteNodeCode1.get());
                ucteNodeCodeList.add(optUcteNodeCode2.get());
                ucteNodeCodeList.add(optUcteNodeCode3.get());
                ucteNodeCodeList.add(optUcteNodeCode4.get());

                ucteNodeCodeList.forEach(ucteNodeCode ->
                        createXnodeFromTieLine(ucteNetwork, ucteNodeCode, line)
                );

                String elementName1 = line.getProperties().containsKey(UcteImporter.ELEMENT_NAME_PROPERTY_KEY + "_1") ?
                        line.getProperties().getProperty(UcteImporter.ELEMENT_NAME_PROPERTY_KEY + "_1") : null;
                String elementName2 = line.getProperties().containsKey(UcteImporter.ELEMENT_NAME_PROPERTY_KEY + "_2") ?
                        line.getProperties().getProperty(UcteImporter.ELEMENT_NAME_PROPERTY_KEY + "_2") : null;

                UcteElementId ucteElementId1 = new UcteElementId(ucteNodeCodeList.get(0), ucteNodeCodeList.get(1), line.getId().charAt(18));
                UcteElementId ucteElementId2 = new UcteElementId(ucteNodeCodeList.get(2), ucteNodeCodeList.get(3), line.getId().charAt(40));

                UcteLine ucteLine1 = new UcteLine(ucteElementId1, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2,
                        (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), elementName1);
                UcteLine ucteLine2 = new UcteLine(ucteElementId2, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2,
                        (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), elementName2);

                ucteNetwork.addLine(ucteLine1);
                ucteNetwork.addLine(ucteLine2);
            } else {
                createTieLineWithGeneratedIds(ucteNetwork, line);
            }
        } else {
            createTieLineWithGeneratedIds(ucteNetwork, line);
        }
    }

    /**
     * Creates an UCTE compliant id for the {@link UcteLine}, creates the two UcteLine corresponding to the tie line and adds it to the UcteNetwork
     *
     * @param ucteNetwork The target UcteNetwork
     * @param line The Tie line with the non-compliant UCTE id
     * @see UcteExporter#createUcteNodeCode
     * @see UcteExporter#generateUcteElementId(String, UcteNodeCode, UcteNodeCode)
     */
    private void createTieLineWithGeneratedIds(UcteNetwork ucteNetwork, Line line) {
        MergedXnode mergedXnode = line.getExtension(MergedXnode.class);
        UcteNodeCode ucteNodeCode1 = createUcteNodeCode(line.getTerminal1().getBusBreakerView().getBus().getId(),
                line.getTerminal1().getBusBreakerView().getBus().getVoltageLevel(),
                line.getTerminal1().getBusBreakerView().getBus().getVoltageLevel().getSubstation().getCountry().toString());
        UcteNodeCode ucteNodeCode2 = createUcteNodeCode(line.getTerminal2().getBusBreakerView().getBus().getId(),
                line.getTerminal2().getBusBreakerView().getBus().getVoltageLevel(),
                line.getTerminal2().getBusBreakerView().getBus().getVoltageLevel().getSubstation().getCountry().toString());
        UcteNodeCode ucteNodeCodeXnode = createUcteNodeCode(mergedXnode.getCode(),
                line.getTerminal1().getBusBreakerView().getBus().getVoltageLevel(),
                UcteCountryCode.XX.toString());
        createXnodeFromTieLine(ucteNetwork, ucteNodeCodeXnode, line);
        UcteElementId ucteElementId1 = generateUcteElementId(line.getId() + "_1", ucteNodeCodeXnode, ucteNodeCode1);
        UcteElementId ucteElementId2 = generateUcteElementId(line.getId() + "_2", ucteNodeCodeXnode, ucteNodeCode2);

        UcteLine ucteLine1 = new UcteLine(ucteElementId1, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2,
                (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), null);
        UcteLine ucteLine2 = new UcteLine(ucteElementId2, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, (float) line.getR() / 2,
                (float) line.getX() / 2, (float) line.getB1(), (int) line.getCurrentLimits1().getPermanentLimit(), null);
        ucteNetwork.addLine(ucteLine1);
        ucteNetwork.addLine(ucteLine2);
    }

    /**
     * Checks if the ucteNodeCode is the NodeCode corresponding to a Xnode. If so, create the Xnode and adds it to the ucteNetwork
     * We only want to add the xnodes here since the other "regular" nodes are already created and put in the network with the
     * {@link UcteExporter#convertBus(UcteNetwork, Bus)}.
     *
     * @param ucteNetwork The target UcteNetwork
     * @param ucteNodeCode the UcteNodeCode needed to create a UcteNode
     * @param line The tie line related to the Xnode
     */
    private void createXnodeFromTieLine(UcteNetwork ucteNetwork, UcteNodeCode ucteNodeCode, Line line) {
        String geographicalName = "";
        float referenceVoltage = Float.NaN;
        if (ucteNodeCode.toString().equals(line.getExtension(MergedXnode.class).getCode())) {
            geographicalName = getGeographicalNameProperty(line);
            referenceVoltage = getReferenceVoltageProperty(line);
        }
        if (ucteNodeCode.getUcteCountryCode() == UcteCountryCode.XX) {
            ucteNetwork.addNode(
                    new UcteNode(
                            ucteNodeCode,
                            geographicalName,
                            UcteNodeStatus.EQUIVALENT,
                            UcteNodeTypeCode.PQ,
                            referenceVoltage,
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
    }

    /**
     * Iterates through the {@link TwoWindingsTransformer}s and call the method to convert them
     * ({@link UcteExporter#convertTwoWindingsTransformer(UcteNetwork, TwoWindingsTransformer)})
     *
     * @param network the iidm Network containing the TwoWindingsTransformers we want to convert
     * @param ucteNetwork The target UcteNetwork
     */
    private void convertTwoWindingsTransformers(Network network, UcteNetwork ucteNetwork) {
        LOGGER.debug("Converting two winding transformers");
        network.getTwoWindingsTransformerStream()
                .forEach(twoWindingsTransformer -> convertTwoWindingsTransformer(ucteNetwork, twoWindingsTransformer));
        LOGGER.debug("two winding transformers converted");
    }

    /**
     * Converts the {@link TwoWindingsTransformer} into a {@link UcteTransformer} and adds it to the ucteNetwork.
     * Also creates the adds the linked {@link UcteRegulation}
     *
     * @param ucteNetwork The target UcteNetwork
     * @param twoWindingsTransformer The two windings transformer we want to convert
     * @see UcteExporter#convertRegulation(UcteNetwork, UcteElementId, TwoWindingsTransformer)
     */
    private void convertTwoWindingsTransformer(UcteNetwork ucteNetwork, TwoWindingsTransformer twoWindingsTransformer) {
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();
        UcteNodeCode ucteNodeCode1 = createUcteNodeCode(
                terminal1.getBusBreakerView().getConnectableBus().getId(),
                terminal1.getVoltageLevel(),
                terminal1.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode2 = createUcteNodeCode(
                terminal2.getBusBreakerView().getConnectableBus().getId(),
                terminal2.getVoltageLevel(),
                terminal2.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteElementId ucteElementId = convertUcteElementId(ucteNodeCode2, ucteNodeCode1, twoWindingsTransformer.getId(), terminal1, terminal2);
        String elementName = twoWindingsTransformer.getProperties().containsKey(ELEMENT_NAME_PROPERTY_KEY) ?
                twoWindingsTransformer.getProperties().getProperty(ELEMENT_NAME_PROPERTY_KEY) : null;
        UcteTransformer ucteTransformer = new UcteTransformer(
                ucteElementId,
                UcteElementStatus.fromCode(1),
                (float) twoWindingsTransformer.getR(),
                (float) twoWindingsTransformer.getX(),
                (float) twoWindingsTransformer.getB(),
                (int) twoWindingsTransformer.getCurrentLimits2().getPermanentLimit(),
                elementName,
                (float) twoWindingsTransformer.getRatedU2(),
                (float) twoWindingsTransformer.getRatedU1(),
                100,        //TODO Find a representation for the nominal power
                (float) twoWindingsTransformer.getG());
        convertRegulation(ucteNetwork, ucteElementId, twoWindingsTransformer);
        ucteNetwork.addTransformer(ucteTransformer);

    }

    /**
     * Creates and adds to the ucteNetwork the {@link UcteRegulation} linked to the TwoWindingsTransformer.
     * <li>{@link RatioTapChanger} into {@link UctePhaseRegulation}</li>
     * <li>{@link PhaseTapChanger} into {@link UcteAngleRegulation}</li>
     *
     * @param ucteNetwork The target UcteNetwork
     * @param ucteElementId The UcteElementId corresponding to the TwoWindingsTransformer
     * @param twoWindingsTransformer The TwoWindingTransformer we want to convert
     */
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

    /**
     * Creates and adds to the ucteNetwork the {@link UcteRegulation} linked to the ThreeWindingsTransformer
     *
     * @param ucteNetwork The target UcteNetwork
     * @param ucteElementId The UcteElementId corresponding to the ThreeWindingsTransformer
     * @param threeWindingsTransformer The ThreeWindingsTransformer we want to convert
     */
    private void convertRegulation(UcteNetwork ucteNetwork, UcteElementId ucteElementId, ThreeWindingsTransformer threeWindingsTransformer) {
        if (threeWindingsTransformer.getLeg2().getRatioTapChanger() != null) {
            UctePhaseRegulation uctePhaseRegulation = convertRatioTapChanger(threeWindingsTransformer);
            UcteRegulation ucteRegulation = new UcteRegulation(ucteElementId, uctePhaseRegulation, null);
            ucteNetwork.addRegulation(ucteRegulation);
        }
    }

    /**
     * Iterates through the {@link ThreeWindingsTransformer}s and call the method to convert them
     * ({@link UcteExporter#convertTwoWindingsTransformer(UcteNetwork, TwoWindingsTransformer)})
     *
     * @param network the iidm Network containing the TwoWindingsTransformers we want to convert
     * @param ucteNetwork The target UcteNetwork
     */
    private void convertThreeWindingTransformers(Network network, UcteNetwork ucteNetwork) {
        LOGGER.debug("Converting iidm ThreeWindingTransformers");
        network.getThreeWindingsTransformerStream()
                .forEach(threeWindingsTransformer -> convertThreeWindingTransformer(ucteNetwork, threeWindingsTransformer));
        LOGGER.debug("iidm ThreeWindingTransformers converted");
    }


    /**
     * Converts the {@link TwoWindingsTransformer} into three {@link UcteTransformer} linked to a created fictious UcteNode and adds them to the ucteNetwork.
     * Also creates the adds the linked {@link UcteRegulation} (for only one of them)
     *
     * @param ucteNetwork The target UcteNetwork
     * @param twt The three windings transformer we want to convert
     * @see UcteExporter#convertRegulation(UcteNetwork, UcteElementId, TwoWindingsTransformer)
     */
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

        UcteNodeCode ucteNodeCode1 = createUcteNodeCode(
                t1.getBusBreakerView().getConnectableBus().getId(),
                t1.getVoltageLevel(),
                t1.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode2 = createUcteNodeCode(
                t2.getBusBreakerView().getConnectableBus().getId(),
                t2.getVoltageLevel(),
                t2.getVoltageLevel().getSubstation().getCountry().toString()
        );

        UcteNodeCode ucteNodeCode3 = createUcteNodeCode(
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
                100,        //TODO Find a representation for the nominal power
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

    /**
     * Creates the {@link UcteRegulation} linked to the twoWindingsTransformer
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the UctePhaseRegulation needed to create a {@link UcteRegulation}
     * @see UcteExporter#calculatePhaseDu(TwoWindingsTransformer)
     */
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

    /**
     * Creates the {@link UcteRegulation} linked to the ThreeWindingsTransformer
     *
     * @param threeWindingsTransformer The ThreeWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the UctePhaseRegulation needed to create a {@link UcteRegulation}
     * @see UcteExporter#calculatePhaseDu(ThreeWindingsTransformer)
     */
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

    /**
     * Determines the UcteAngleRegulationType and depending on it, creates the UcteAngleRegulation
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the PhaseTapChanger we want to convert
     * @return the UcteAngleRegulation needed to create a {@link UcteRegulation}
     * @see UcteAngleRegulation
     * @see UcteExporter#findRegulationType(TwoWindingsTransformer)
     */
    private UcteAngleRegulation convertPhaseTapChanger(TwoWindingsTransformer twoWindingsTransformer) {
        LOGGER.debug("Converting iidm Phase tap changer of transformer {}", twoWindingsTransformer.getId());
        UcteAngleRegulationType ucteAngleRegulationType = findRegulationType(twoWindingsTransformer);
        if (ucteAngleRegulationType == UcteAngleRegulationType.SYMM) {
            return new UcteAngleRegulation((float) calculateSymmAngleDu(twoWindingsTransformer),
                    90,
                    twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(),
                    twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                    calculateAngleP(twoWindingsTransformer),
                    ucteAngleRegulationType);
        } else {
            return new UcteAngleRegulation((float) calculateAsymmAngleDu(twoWindingsTransformer),
                    (float) calculateAsymmAngleTheta(twoWindingsTransformer),
                    twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(),
                    twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                    calculateAngleP(twoWindingsTransformer),
                    ucteAngleRegulationType);
        }
    }

    /**
     * calculate the δu(%) for the phase regulation of the two windings transformer
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the δu needed to create a {@link UctePhaseRegulation}
     */
    double calculatePhaseDu(TwoWindingsTransformer twoWindingsTransformer) {
        double rhoMax = 0;
        double rhoMin = 0;
        int tapNumber =  twoWindingsTransformer.getRatioTapChanger().getStepCount();
        double[] rhoList = new double[tapNumber];
        int j = 0;
        for (int i = twoWindingsTransformer.getRatioTapChanger().getLowTapPosition();
             i <= twoWindingsTransformer.getRatioTapChanger().getHighTapPosition(); i++) {
            rhoList[j] = twoWindingsTransformer.getRatioTapChanger().getStep(i).getRho();
            j++;
        }
        rhoMax = Doubles.max(rhoList);
        rhoMin = Doubles.min(rhoList);
        double res = 100 * (1 / rhoMin - 1 / rhoMax) / (tapNumber - 1);
        return BigDecimal.valueOf(res).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * calculate the δu(%) for the phase regulation of the three windings transformer
     *
     * @param threeWindingsTransformer The ThreeWindingsTransformer containing the RatioTapChanger we want to convert
     * @return the δu needed to create a {@link UctePhaseRegulation}
     */
    double calculatePhaseDu(ThreeWindingsTransformer threeWindingsTransformer) {
        double rhoMax = 0;
        double rhoMin = 0;
        RatioTapChanger leg2RatioTapChanger = threeWindingsTransformer.getLeg2().getRatioTapChanger();
        int tapNumber =  leg2RatioTapChanger.getStepCount();
        double[] rhoList = new double[tapNumber];
        int j = 0;
        boolean passedBy0 = false;
        for (int i = leg2RatioTapChanger.getLowTapPosition(); i <= leg2RatioTapChanger.getHighTapPosition(); i++) {
            rhoList[j] = leg2RatioTapChanger.getStep(i).getRho();
            j++;
        }
        rhoMax = Doubles.max(rhoList);
        rhoMin = Doubles.min(rhoList);
        double res = 100 * (1 / rhoMin - 1 / rhoMax) / (tapNumber - 1);
        return BigDecimal.valueOf(res).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type symmetrical
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */
    private double calculateSymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        double alphaMin = 0;
        double alphaMax = 0;
        int tabNumber = twoWindingsTransformer.getPhaseTapChanger().getStepCount();
        double[] alphaList = new double[tabNumber];
        int j = 0;
        for (int i = twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition();
             i <= twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(); i++) {
            alphaList[j] = twoWindingsTransformer.getPhaseTapChanger().getStep(i).getAlpha();
            j++;
        }
        alphaMin = Doubles.min(alphaList);
        alphaMax = Doubles.max(alphaList);

        return 2 * (Math.tan(alphaMax / 2) - Math.tan(alphaMin / 2)) / (tabNumber - 1);  //FIXME : is this right ? it doesn't give the wanted result anyway
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type asymmetrical
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */
    private double calculateAsymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        double lowPositionAlpha = -phaseTapChanger.getStep(lowTapPosition).getAlpha();
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = -phaseTapChanger.getStep(highTapPosition).getAlpha();
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionRho);
        double xb = highPositionRho + Math.cos(highPositionAlpha);
        double yb = highPositionRho + Math.sin(highPositionAlpha);
        double distance = Math.sqrt(Math.pow(xb - xa, 2) + Math.pow(yb - ya, 2));
        return 100 * distance / (highTapPosition + Math.abs(lowTapPosition));   //FIXME : is this right ? it doesn't give the wanted result anyway
    }

    /**
     * Calculate the Θ for the angle regulation of the two windings transformer
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the Θ needed to create a {@link UcteAngleRegulation}
     */
    private double calculateAsymmAngleTheta(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        double lowPositionAlpha = -phaseTapChanger.getStep(lowTapPosition).getAlpha();
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = -phaseTapChanger.getStep(highTapPosition).getAlpha();
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionRho);
        double xb = highPositionRho + Math.cos(highPositionAlpha);
        double yb = highPositionRho + Math.sin(highPositionAlpha);
        return Math.atan((yb - ya) / (xb - xa));    //FIXME : is this right ? it doesn't give the wanted result anyway
    }

    /**
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return P (MW) of the angle regulation for the two windings transformer
     */
    private float calculateAngleP(TwoWindingsTransformer twoWindingsTransformer) { //TODO where do I find P ?
        return 0;
    }

    /**
     * Give the type of the UcteAngleRegulation
     *
     * @param twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return The type of the UcteAngleRegulation
     */
    private UcteAngleRegulationType findRegulationType(TwoWindingsTransformer twoWindingsTransformer) {
        if (isSymm(twoWindingsTransformer)) {
            return UcteAngleRegulationType.SYMM;
        } else {
            return UcteAngleRegulationType.ASYM;
        }
    }

    private boolean isSymm(TwoWindingsTransformer twoWindingsTransformer) {
        for (int i = twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition();
             i < twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(); i++) {
            if (twoWindingsTransformer.getPhaseTapChanger().getStep(i).getRho() != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isAsymm(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        double lowPositionAlpha = -phaseTapChanger.getStep(lowTapPosition).getAlpha();
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = -phaseTapChanger.getStep(highTapPosition).getAlpha();
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionRho);
        double xb = highPositionRho + Math.cos(highPositionAlpha);
        double yb = highPositionRho + Math.sin(highPositionAlpha);
        double slope  = (yb - ya) / (xb - xa); // so we have ya = slope * xa + intercept
        double intercept = -(slope * xa) + ya;

        Double distanceBetweenPoints = null;
        for (int i = phaseTapChanger.getLowTapPosition();
            i < phaseTapChanger.getHighTapPosition() - 1; i++) {
            double rho1 = 1 / phaseTapChanger.getStep(i).getRho();
            double rho2 = 1 / phaseTapChanger.getStep(i + 1).getRho();
            double alpha1 = -phaseTapChanger.getStep(i).getAlpha();
            double alpha2 = -phaseTapChanger.getStep(i + 1).getAlpha();
            double x1 = rho1 * Math.cos(alpha1);
            double y1 = rho1 * Math.sin(alpha1);
            double x2 = rho2 * Math.cos(alpha2);
            double y2 = rho2 * Math.sin(alpha2);
            if (!isOnLine(x1, y1, slope, intercept) || !isOnLine(x2, y2, slope, intercept)) {
                return false;
            }
            if (distanceBetweenPoints == null) {
                distanceBetweenPoints = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            } else {
                if (distanceBetweenPoints != Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * return true if the point (x,y) is on the line : slope * x + intercept
     */
    private static boolean isOnLine(double x, double y, double slope, double intercept) {
        double calculatedY = slope * x + intercept;
        return calculatedY == y;
    }

    UcteNodeCode createUcteNodeCode(String id, VoltageLevel voltageLevel, String country) {
        UcteNodeCode ucteNodeCode;
        if (iidmIdToUcteNodeCodeId.containsKey(id)) {
            return iidmIdToUcteNodeCodeId.get(id);
        }
        Optional<UcteNodeCode> optionalUcteNodeCode = parseUcteNodeCode(id);
        if (optionalUcteNodeCode.isPresent()) { // the ID is already an UCTE id
            iidmIdToUcteNodeCodeId.put(id, optionalUcteNodeCode.get());
            return optionalUcteNodeCode.get();
        } else {
            convertIidmIdToUcteNodeCode(id, voltageLevel, country);
            ucteNodeCode = iidmIdToUcteNodeCodeId.get(id);
        }
        return ucteNodeCode;
    }

    /**
     * create a unique UcteNodeCode using the voltage level and the country given and
     * store it to {@link UcteExporter#iidmIdToUcteNodeCodeId} so we don't need to create it every time
     * and ensure  its uniqueness
     *
     * @param id the orignal id of the iidm component we want to convert
     * @param voltageLevel the {@link VoltageLevel} of the component we want to convert
     * @param country the string corresponding to the country where the iidm component is
     */
    private void convertIidmIdToUcteNodeCode(String id, VoltageLevel voltageLevel, String country) {
        UcteNodeCode ucteNodeCode = null;
        String voltageNameOrId = voltageLevel.getName() != null ? voltageLevel.getName() : voltageLevel.getId();
        int busbar = 'a';
        do {
            if (busbar <= 'z') {
                ucteNodeCode = new UcteNodeCode(
                        UcteCountryCode.valueOf(country),
                        voltageNameOrId,
                        UcteVoltageLevelCode.VL_500,  // FIXME where do i find the voltageLvlCode since voltageLevelCodeFromIidmVoltage(voltageLevel.getNominalV()) is not good and a bus.getV() may be null
                        //voltageLevelCodeFromIidmVoltage(voltageLevel.getNominalV()),
                        (char) busbar
                );
            } else {
                LOGGER.warn("There is more than 26 nodes with the the id starting by '{}'", ucteNodeCode);
                generateUcteNodeCode(id, voltageLevel, country);
                return;
            }
            busbar++;
        } while (iidmIdToUcteNodeCodeId.values().contains(ucteNodeCode));
        iidmIdToUcteNodeCodeId.put(id, ucteNodeCode);
    }

    /**
     * create a unique UcteNodeCode using the voltage level and the country given and
     * store it to {@link UcteExporter#iidmIdToUcteNodeCodeId} so we don't need to create it every time
     * and ensure  its uniqueness.
     * UcteNodeCode id = 1 character representing the country + 2 characters for the substation (or "aa" if the substation
     * doesn't have a name) + 3 character generated from "aaa" to "zzz" + 1 character for the voltage level code and a character
     * generated from "a" to "z". The goal is to create a ucte compliant id from any iidm component with non ucte compliant ids
     *
     * @param id the orignal id of the iidm component we want to convert
     * @param voltageLevel the {@link VoltageLevel} of the component we want to convert
     * @param country the string corresponding to the country where the iidm component is
     */
    void generateUcteNodeCode(String id, VoltageLevel voltageLevel, String country) {
        UcteNodeCode ucteNodeCode = null;
        String substationName = null;

        if (voltageLevel.getSubstation().getName() != null && voltageLevel.getSubstation().getName().length() >= 2) {
            substationName = voltageLevel.getSubstation().getName().substring(0, 2);
        } else {
            substationName = "aa";
        }
        do {
            generatedGeographicalName = incrementGeneratedGeographicalName(generatedGeographicalName);
            ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(country),
                    substationName + generatedGeographicalName.substring(0, 3),
                    voltageLevelCodeFromIidmVoltage(voltageLevel.getNominalV()), // FIXME where do i find the voltageLvlCode since voltageLevelCodeFromIidmVoltage(voltageLevel.getNominalV()) is not good and a bus.getV() may be null
                    generatedGeographicalName.charAt(3)
            );
        } while (iidmIdToUcteNodeCodeId.values().contains(ucteNodeCode));
        iidmIdToUcteNodeCodeId.put(id, ucteNodeCode);
    }

    /**
     * create a unique UcteElementId with the given information in parameters
     *
     */
    UcteElementId convertUcteElementId(UcteNodeCode ucteNodeCode1, UcteNodeCode ucteNodeCode2, String id, Terminal terminal1, Terminal terminal2) {
        if (isUcteNodeId(terminal1.getBusBreakerView().getConnectableBus().getId()) &&
                isUcteNodeId(terminal2.getBusBreakerView().getConnectableBus().getId())) {
            return new UcteElementId(
                    ucteNodeCode1,
                    ucteNodeCode2,
                    id.charAt(18));
        } else {
            return generateUcteElementId(id, ucteNodeCode1, ucteNodeCode2);
        }
    }

    /**
     * increment the last character of the id from 'a' to 'z' until the id is unique, and return it
     *
     * @param id the id of the original iidm component to convert
     * @param ucteNodeCode1 the ucteNodeCode used to create the UcteElementId
     * @param ucteNodeCode2 the ucteNodeCode used to create the UcteElementId
     * @return the generated UcteElementId
     */
    private UcteElementId generateUcteElementId(String id, UcteNodeCode ucteNodeCode1, UcteNodeCode ucteNodeCode2) {
        if (iidmIdToUcteElementId.containsKey(id)) {
            return iidmIdToUcteElementId.get(id);
        } else {
            int orderCode = 'a';
            UcteElementId ucteElementId = null;
            do {
                if (orderCode <= 'z') {
                    ucteElementId = new UcteElementId(ucteNodeCode1, ucteNodeCode2, (char) orderCode);
                } else {
                    LOGGER.warn("There is more than 26 element with the the id starting by '{}'", ucteElementId);
                    return null;
                }
                orderCode++;
            } while (iidmIdToUcteElementId.values().contains(ucteElementId));
            iidmIdToUcteElementId.put(id, ucteElementId);
            return ucteElementId;
        }
    }

    /**
     * check if the id of the line match with the id of a tie line in ucte
     *
     * @param line is a tie line ?
     * @return true if the parameter is a tie line
     */
    boolean isUcteTieLineId(Line line) {
        MergedXnode mergedXnode = line.getExtension(MergedXnode.class);

        return mergedXnode != null
                && mergedXnode.getLine1Name() != null
                && mergedXnode.getLine2Name() != null
                && mergedXnode.getLine1Name().length() >= 19
                && mergedXnode.getLine2Name().length() >= 19
                && isUcteNodeId(mergedXnode.getLine1Name().substring(0, 8))
                && isUcteNodeId(mergedXnode.getLine1Name().substring(9, 17))
                && isUcteNodeId(mergedXnode.getLine2Name().substring(0, 8))
                && isUcteNodeId(mergedXnode.getLine2Name().substring(9, 17));
    }

    private void setSwitchCurrentLimit(UcteLine ucteLine, Switch sw) {
        if (sw.getProperties().containsKey(UcteImporter.CURRENT_LIMIT_PROPERTY_KEY)) {
            try {
                ucteLine.setCurrentLimit(Integer.parseInt(sw.getProperties().getProperty(UcteImporter.CURRENT_LIMIT_PROPERTY_KEY)));
            } catch (NumberFormatException exception) {
                ucteLine.setCurrentLimit(UcteImporter.DEFAULT_SWICH_MAX_CURRENT);
                LOGGER.warn("Switch {}: No current limit, set value to {}", sw.getId(), UcteImporter.DEFAULT_SWICH_MAX_CURRENT);
            }
        } else {
            ucteLine.setCurrentLimit(UcteImporter.DEFAULT_SWICH_MAX_CURRENT);
            LOGGER.warn("Switch {}: No current limit, set value to {}", sw.getId(), UcteImporter.DEFAULT_SWICH_MAX_CURRENT);
        }
    }

    String getGeographicalNameProperty(Line line) {
        if (line.getProperties().containsKey(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY)) {
            return line.getProperties().getProperty(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY);
        }
        return "";
    }

    float getReferenceVoltageProperty(Line line) {
        if (line.getProperties().containsKey(UcteImporter.REFERENCE_VOLTAGE)) {
            return Float.parseFloat(line.getProperties().getProperty(UcteImporter.REFERENCE_VOLTAGE));
        }
        return Float.NaN;
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

    /**
     * "increment" the string given in parameter by 1 : "aaa" gives "aab", "aaz" gives "aba" etc...
     *
     * @param toIncrement the string to increment
     * @return a string "incremented" by 1
     */
    String incrementGeneratedGeographicalName(String toIncrement) {
        if (toIncrement.length() < 4) {
            throw new IllegalArgumentException("The string to increment is not long enough (should be at least 4)");
        }
        char charAt3 = toIncrement.charAt(3);
        char charAt2 = toIncrement.charAt(2);
        char charAt1 = toIncrement.charAt(1);
        char charAt0 = toIncrement.charAt(0);

        if (charAt3 == 'z') {
            if (charAt2 == 'z') {
                if (charAt1 == 'z') {
                    if (charAt0 == 'z') {
                        throw new UcteException("This network cannot be exported, too much identical ids");
                    } else {
                        charAt3 = 'a';
                        charAt2 = 'a';
                        charAt1 = 'a';
                        charAt0++;
                    }
                } else {
                    charAt3 = 'a';
                    charAt2 = 'a';
                    charAt1++;
                }
            } else {
                charAt3 = 'a';
                charAt2++;
            }
        } else {
            charAt3++;
        }
        return String.valueOf(charAt0) + String.valueOf(charAt1) + String.valueOf(charAt2) + String.valueOf(charAt3);
    }
}
