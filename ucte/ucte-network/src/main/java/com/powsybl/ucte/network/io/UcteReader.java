/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import com.powsybl.ucte.network.UcteAngleRegulation;
import com.powsybl.ucte.network.UcteAngleRegulationType;
import com.powsybl.ucte.network.UcteLine;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteTransformer;
import com.powsybl.ucte.network.UcteNodeStatus;
import com.powsybl.ucte.network.UcteNodeTypeCode;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UctePowerPlantType;
import com.powsybl.ucte.network.UcteFormatVersion;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteElementStatus;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNetworkImpl;
import com.powsybl.ucte.network.UctePhaseRegulation;
import com.powsybl.ucte.network.UcteRegulation;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import java.io.BufferedReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteReader.class);

    private boolean firstCommendBlockRead = false;

    private void readCommentBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.trace("Reading comment block");
        if (!firstCommendBlockRead && parser.getParsedRecordTypes().size() > 1) {
            throw new UcteIoException("First block must be a comment block");
        }
        // just record first comment block
        boolean skipComments = firstCommendBlockRead;
        // only the first comment block contains the version
        if (!firstCommendBlockRead) {
            UcteFormatVersion version = UcteFormatVersion.findByDate(parser.parseString(4, 14));
            network.setVersion(version);
        }
        firstCommendBlockRead = true;
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                if (!skipComments) {
                    network.getComments().add(parser.getLine());
                }
            }
        }
    }

    private static UcteNodeCode parseNodeCode(UcteRecordParser parser, int beginIndex) {
        UcteCountryCode ucteCountryCode = UcteCountryCode.fromUcteCode(parser.parseChar(beginIndex));
        String geographicalSpot = parser.parseString(beginIndex + 1, beginIndex + 6, false);
        UcteVoltageLevelCode voltageLevelCode = parser.parseEnumOrdinal(beginIndex + 6, UcteVoltageLevelCode.class);
        Character busbar = parser.parseChar(beginIndex + 7);
        return new UcteNodeCode(ucteCountryCode, geographicalSpot, voltageLevelCode, busbar);
    }

    private void parseNode(UcteRecordParser parser, UcteNetwork network) {
        UcteNodeCode id = parseNodeCode(parser, 0);
        String geographicalName = parser.parseString(9, 21).trim();
        UcteNodeStatus status = parser.parseEnumOrdinal(22, UcteNodeStatus.class);
        UcteNodeTypeCode typeCode = parser.parseEnumOrdinal(24, UcteNodeTypeCode.class);
        float voltageReference = parser.parseFloat(26, 32);
        float activeLoad = parser.parseFloat(33, 40);
        float reactiveLoad = parser.parseFloat(41, 48);
        float activePowerGeneration = parser.parseFloat(49, 56);
        float reactivePowerGeneration = parser.parseFloat(57, 64);
        float minimumPermissibleActivePowerGeneration = parser.parseFloat(65, 72);
        float maximumPermissibleActivePowerGeneration = parser.parseFloat(73, 80);
        float minimumPermissibleReactivePowerGeneration = parser.parseFloat(81, 88);
        float maximumPermissibleReactivePowerGeneration = parser.parseFloat(89, 96);
        float staticOfPrimaryControl = parser.parseFloat(97, 102);
        float nominalPowerPrimaryControl = parser.parseFloat(103, 110);
        float threePhaseShortCircuitPower = parser.parseFloat(111, 118);
        float xrRatio = parser.parseFloat(119, 126);
        UctePowerPlantType powerPlantType = parser.parseEnumValue(127, UctePowerPlantType.class);

        UcteNode node = new UcteNode(id, geographicalName, status, typeCode, voltageReference,
                                     activeLoad, reactiveLoad, activePowerGeneration,
                                     reactivePowerGeneration, minimumPermissibleActivePowerGeneration,
                                     maximumPermissibleActivePowerGeneration, minimumPermissibleReactivePowerGeneration,
                                     maximumPermissibleReactivePowerGeneration, staticOfPrimaryControl,
                                     nominalPowerPrimaryControl, threePhaseShortCircuitPower, xrRatio, powerPlantType);
        network.addNode(node);
    }

    private void readNodeBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.trace("Reading node block");
        String countryIsoCode = null;
        while (parser.nextLine()) {
            UcteRecordType recordType = parser.scanRecordType();
            if (recordType != null) {
                if (recordType == UcteRecordType.Z) {
                    countryIsoCode = parser.parseString(3, 5);
                } else {
                    parseRecords(parser, network);
                }
            } else {
                if (countryIsoCode == null) {
                    throw new UcteIoException("A node must be define in a ##Z context");
                }
                parseNode(parser, network);
            }
        }
    }

    private static UcteElementId parseElementId(UcteRecordParser parser) {
        UcteNodeCode nodeId1 = parseNodeCode(parser, 0);
        UcteNodeCode nodeId2 = parseNodeCode(parser, 9);
        char orderCode = parser.parseChar(18);
        return new UcteElementId(nodeId1, nodeId2, orderCode);
    }

    private void parseLine(UcteRecordParser parser, UcteNetwork network) {
        UcteElementId id = parseElementId(parser);
        UcteElementStatus status = UcteElementStatus.fromCode(parser.parseInt(20));
        float resistance = parser.parseFloat(22, 28);
        float reactance = parser.parseFloat(29, 35);
        float susceptance = (float) (parser.parseFloat(36, 44) * Math.pow(10, -6));
        Integer currentLimit = parser.parseInt(45, 51);
        String elementName = parser.parseString(52, 64);

        UcteLine l = new UcteLine(id, status, resistance, reactance, susceptance, currentLimit, elementName);
        network.addLine(l);
    }

    private void readLineBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.trace("Reading line block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                parseLine(parser, network);
            }
        }
    }

    private void parseTransformer(UcteRecordParser parser, UcteNetwork network) {
        UcteElementId id = parseElementId(parser);
        UcteElementStatus status = UcteElementStatus.fromCode(parser.parseInt(20));
        float ratedVoltage1 = parser.parseFloat(22, 27);
        float ratedVoltage2 = parser.parseFloat(28, 33);
        float nominalPower = parser.parseFloat(34, 39);
        float resistance = parser.parseFloat(40, 46);
        float reactance = parser.parseFloat(47, 53);
        float susceptance = (float) (parser.parseFloat(54, 62) * Math.pow(10, -6));
        float conductance = (float) (parser.parseFloat(63, 69) * Math.pow(10, -6));
        Integer currentLimit = parser.parseInt(70, 76);
        String elementName = parser.parseString(77, 89);

        UcteTransformer transfo = new UcteTransformer(id, status, resistance, reactance, susceptance, currentLimit, elementName,
                                                      ratedVoltage1, ratedVoltage2, nominalPower, conductance);
        network.addTransformer(transfo);
    }

    private void readTransformerBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.trace("Reading transformer block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                parseTransformer(parser, network);
            }
        }
    }

    private UctePhaseRegulation parsePhaseRegulation(UcteRecordParser parser) {
        float du = parser.parseFloat(20, 25);
        Integer n = parser.parseInt(26, 28);
        Integer np = parser.parseInt(29, 32);
        float u = parser.parseFloat(33, 38);
        if (!Float.isNaN(du) || n != null || np != null || !Float.isNaN(u)) {
            return new UctePhaseRegulation(du, n, np, u);
        }
        return null;
    }

    private UcteAngleRegulation parseAngleRegulation(UcteRecordParser parser) {
        float du = parser.parseFloat(39, 44);
        float theta = parser.parseFloat(45, 50);
        Integer n = parser.parseInt(51, 53);
        Integer np = parser.parseInt(54, 57);
        float p = parser.parseFloat(58, 63);
        UcteAngleRegulationType type = parser.parseEnumValue(64, 68, UcteAngleRegulationType.class);
        if (!Float.isNaN(du) || !Float.isNaN(theta) || n != null || np != null || !Float.isNaN(p) || type != null) {
            return new UcteAngleRegulation(du, theta, n, np, p, type);
        }
        return null;
    }

    private void parseRegulation(UcteRecordParser parser, UcteNetwork network) {
        UcteElementId transfoId = parseElementId(parser);
        UctePhaseRegulation phaseRegulation = parsePhaseRegulation(parser);
        UcteAngleRegulation angleRegulation = parseAngleRegulation(parser);
        UcteRegulation regulation = new UcteRegulation(transfoId, phaseRegulation, angleRegulation);
        network.addRegulation(regulation);
    }

    private void readRegulationBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.trace("Reading regulation block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                parseRegulation(parser, network);
            }
        }
    }

    private void readTtBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.warn("TT block not supported");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                // TODO
            }
        }
    }


    private void readExchangeBlock(UcteRecordParser parser, UcteNetwork network) throws IOException {
        LOGGER.warn("E block not supported");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network);
            } else {
                // TODO
            }
        }
    }

    private void parseRecords(UcteRecordParser parser, UcteNetwork network) throws IOException {
        do {
            UcteRecordType recordType = parser.scanRecordType();
            if (recordType != null) {
                switch (recordType) {
                    case C:
                        readCommentBlock(parser, network);
                        break;
                    case N:
                        readNodeBlock(parser, network);
                        break;
                    case L:
                        readLineBlock(parser, network);
                        break;
                    case T:
                        readTransformerBlock(parser, network);
                        break;
                    case R:
                        readRegulationBlock(parser, network);
                        break;
                    case TT:
                        readTtBlock(parser, network);
                        break;
                    case E:
                        readExchangeBlock(parser, network);
                        break;
                    default:
                        throw new UcteIoException("Unknown record type " + recordType);
                }
            } else {
                LOGGER.warn("Skipping line '{}'", parser.getLine());
            }
        } while (parser.nextLine());
    }

    public UcteNetwork read(BufferedReader reader) throws IOException {
        long start = System.currentTimeMillis();
        UcteNetwork network = new UcteNetworkImpl();
        UcteRecordParser parser = new UcteRecordParser(reader);
        parseRecords(parser, network);
        LOGGER.debug("UCTE file read in {} ms", System.currentTimeMillis() - start);
        network.fix();
        return network;
    }

    public boolean checkHeader(BufferedReader reader) throws IOException {
        // just check the first record if this file is in UCT format
        UcteRecordParser parser = new UcteRecordParser(reader);
        UcteRecordType recordType = parser.scanRecordType();
        if (recordType == UcteRecordType.C) {
            UcteFormatVersion version = UcteFormatVersion.findByDate(parser.parseString(4, 14));
            return version != null;
        }
        return false;
    }

}
