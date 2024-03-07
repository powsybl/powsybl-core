/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.ucte.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteReader.class);

    private boolean firstCommendBlockRead = false;

    private void readCommentBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
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
                parseRecords(parser, network, reportNode);
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
        double voltageReference = parser.parseDouble(26, 32);
        double activeLoad = parser.parseDouble(33, 40);
        double reactiveLoad = parser.parseDouble(41, 48);
        double activePowerGeneration = parser.parseDouble(49, 56);
        double reactivePowerGeneration = parser.parseDouble(57, 64);
        double minimumPermissibleActivePowerGeneration = parser.parseDouble(65, 72);
        double maximumPermissibleActivePowerGeneration = parser.parseDouble(73, 80);
        double minimumPermissibleReactivePowerGeneration = parser.parseDouble(81, 88);
        double maximumPermissibleReactivePowerGeneration = parser.parseDouble(89, 96);
        double staticOfPrimaryControl = parser.parseDouble(97, 102);
        double nominalPowerPrimaryControl = parser.parseDouble(103, 110);
        double threePhaseShortCircuitPower = parser.parseDouble(111, 118);
        double xrRatio = parser.parseDouble(119, 126);
        UctePowerPlantType powerPlantType = parser.parseEnumValue(127, UctePowerPlantType.class);

        UcteNode node = new UcteNode(id, geographicalName, status, typeCode, voltageReference,
                                     activeLoad, reactiveLoad, activePowerGeneration,
                                     reactivePowerGeneration, minimumPermissibleActivePowerGeneration,
                                     maximumPermissibleActivePowerGeneration, minimumPermissibleReactivePowerGeneration,
                                     maximumPermissibleReactivePowerGeneration, staticOfPrimaryControl,
                                     nominalPowerPrimaryControl, threePhaseShortCircuitPower, xrRatio, powerPlantType);
        network.addNode(node);
    }

    private void readNodeBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.trace("Reading node block");
        String countryIsoCode = null;
        while (parser.nextLine()) {
            UcteRecordType recordType = parser.scanRecordType();
            if (recordType != null) {
                if (recordType == UcteRecordType.Z) {
                    countryIsoCode = parser.parseString(3, 5);
                } else {
                    parseRecords(parser, network, reportNode);
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
        String elementId = parser.parseString(0, 19);
        return UcteElementId.parseUcteElementId(elementId).orElseThrow(() -> new UcteIoException("Invalid element ID: " + elementId));
    }

    private void parseLine(UcteRecordParser parser, UcteNetwork network) {
        UcteElementId id = parseElementId(parser);
        UcteElementStatus status = UcteElementStatus.fromCode(parser.parseInt(20));
        double resistance = parser.parseDouble(22, 28);
        double reactance = parser.parseDouble(29, 35);
        double susceptance = parser.parseDouble(36, 44) * Math.pow(10, -6);
        Integer currentLimit = parser.parseInt(45, 51);
        String elementName = parser.parseString(52, 64);

        UcteLine l = new UcteLine(id, status, resistance, reactance, susceptance, currentLimit, elementName);
        network.addLine(l);
    }

    private void readLineBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.trace("Reading line block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network, reportNode);
            } else {
                parseLine(parser, network);
            }
        }
    }

    private void parseTransformer(UcteRecordParser parser, UcteNetwork network) {
        UcteElementId id = parseElementId(parser);
        UcteElementStatus status = UcteElementStatus.fromCode(parser.parseInt(20));
        double ratedVoltage1 = parser.parseDouble(22, 27);
        double ratedVoltage2 = parser.parseDouble(28, 33);
        double nominalPower = parser.parseDouble(34, 39);
        double resistance = parser.parseDouble(40, 46);
        double reactance = parser.parseDouble(47, 53);
        double susceptance = parser.parseDouble(54, 62) * Math.pow(10, -6);
        double conductance = parser.parseDouble(63, 69) * Math.pow(10, -6);
        Integer currentLimit = parser.parseInt(70, 76);
        String elementName = parser.parseString(77, 89);

        UcteTransformer transfo = new UcteTransformer(id, status, resistance, reactance, susceptance, currentLimit, elementName,
                                                      ratedVoltage1, ratedVoltage2, nominalPower, conductance);
        network.addTransformer(transfo);
    }

    private void readTransformerBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.trace("Reading transformer block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network, reportNode);
            } else {
                parseTransformer(parser, network);
            }
        }
    }

    private UctePhaseRegulation parsePhaseRegulation(UcteRecordParser parser) {
        double du = parser.parseDouble(20, 25);
        Integer n = parser.parseInt(26, 28);
        Integer np = parser.parseInt(29, 32);
        double u = parser.parseDouble(33, 38);
        if (!Double.isNaN(du) || n != null || np != null || !Double.isNaN(u)) {
            return new UctePhaseRegulation(du, n, np, u);
        }
        return null;
    }

    private UcteAngleRegulation parseAngleRegulation(UcteRecordParser parser) {
        double du = parser.parseDouble(39, 44);
        double theta = parser.parseDouble(45, 50);
        Integer n = parser.parseInt(51, 53);
        Integer np = parser.parseInt(54, 57);
        double p = parser.parseDouble(58, 63);
        UcteAngleRegulationType type = parser.parseEnumValue(64, 68, UcteAngleRegulationType.class);
        if (!Double.isNaN(du) || !Double.isNaN(theta) || n != null || np != null || !Double.isNaN(p) || type != null) {
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

    private void readRegulationBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.trace("Reading regulation block");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network, reportNode);
            } else {
                parseRegulation(parser, network);
            }
        }
    }

    private void readTtBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.warn("TT block not supported");
        reportNode.newReportNode().withMessageTemplate("UnsupportedTTBlock", "TT block not supported").add();
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network, reportNode);
            } else {
                // TODO
            }
        }
    }

    private void readExchangeBlock(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        LOGGER.warn("E block not supported");
        while (parser.nextLine()) {
            if (parser.scanRecordType() != null) {
                parseRecords(parser, network, reportNode);
            } else {
                // TODO
            }
        }
    }

    private void parseRecords(UcteRecordParser parser, UcteNetwork network, ReportNode reportNode) throws IOException {
        do {
            UcteRecordType recordType = parser.scanRecordType();
            if (recordType != null) {
                switch (recordType) {
                    case C:
                        readCommentBlock(parser, network, reportNode);
                        break;
                    case N:
                        readNodeBlock(parser, network, reportNode);
                        break;
                    case L:
                        readLineBlock(parser, network, reportNode);
                        break;
                    case T:
                        readTransformerBlock(parser, network, reportNode);
                        break;
                    case R:
                        readRegulationBlock(parser, network, reportNode);
                        break;
                    case TT:
                        readTtBlock(parser, network, reportNode);
                        break;
                    case E:
                        readExchangeBlock(parser, network, reportNode);
                        break;
                    default:
                        throw new UcteIoException("Unknown record type " + recordType);
                }
            } else {
                LOGGER.warn("Skipping line '{}'", parser.getLine());
            }
        } while (parser.nextLine());
    }

    public UcteNetwork read(BufferedReader reader, ReportNode reportNode) throws IOException {

        ReportNode readReportNode = reportNode.newReportNode().withMessageTemplate("UcteReading", "Reading UCTE network file").add();
        long start = System.currentTimeMillis();
        UcteNetwork network = new UcteNetworkImpl();
        UcteRecordParser parser = new UcteRecordParser(reader);
        parseRecords(parser, network, readReportNode);
        LOGGER.debug("UCTE file read in {} ms", System.currentTimeMillis() - start);

        network.fix(readReportNode);

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
