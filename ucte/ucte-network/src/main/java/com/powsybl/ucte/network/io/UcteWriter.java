/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import com.powsybl.ucte.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteWriter.class);

    private final UcteNetwork network;

    public UcteWriter(UcteNetwork network) {
        this.network = network;
    }

    private void writeCommentBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing comment block");
        writer.writeString("##C", 0, 3);
        if (network.getVersion() != null) {
            writer.writeString(" " + network.getVersion().getDate(), 3, 14);
        }
        writer.newLine();
        for (String comment : network.getComments()) {
            writer.writeString(comment, 0, comment.length());
            writer.newLine();
        }
    }

    private static void writeNodeCode(UcteNodeCode id, UcteRecordWriter writer, int beginIndex) {
        writer.writeChar(id.getUcteCountryCode().getUcteCode(), beginIndex);
        writer.writeString(id.getGeographicalSpot(), beginIndex + 1, beginIndex + 6);
        writer.writeEnumOrdinal(id.getVoltageLevelCode(), beginIndex + 6);
        writer.writeChar(id.getBusbar(), beginIndex + 7);
    }

    private void writeNodeBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing node block");
        writer.writeString("##N", 0, 3);
        writer.newLine();
        Map<UcteCountryCode, List<UcteNode>> nodesByCountry = new EnumMap<>(UcteCountryCode.class);
        for (UcteNode node : network.getNodes()) {
            List<UcteNode> nodes = nodesByCountry.get(node.getCode().getUcteCountryCode());
            if (nodes == null) {
                nodes = new ArrayList<>();
                nodesByCountry.put(node.getCode().getUcteCountryCode(), nodes);
            }
            nodes.add(node);
        }

        nodesByCountry.forEach((ucteCountryCode, ucteNodes) ->
                ucteNodes.sort(Comparator.comparing(UcteNode::getCode)));
        for (Map.Entry<UcteCountryCode, List<UcteNode>> entry : nodesByCountry.entrySet()) {
            UcteCountryCode countryCode = entry.getKey();
            List<UcteNode> nodes = entry.getValue();
            writer.writeString("##Z" + countryCode, 0, 5);
            writer.newLine();
            for (UcteNode node : nodes) {
                writeNodeCode(node.getCode(), writer, 0);
                writer.writeString(node.getGeographicalName(), 9, 21);
                writer.writeEnumOrdinal(node.getStatus(), 22);
                writer.writeEnumOrdinal(node.getTypeCode(), 24);
                writer.writeFloat(node.getVoltageReference(), 26, 32);
                writer.writeFloat(node.getActiveLoad(), 33, 40);
                writer.writeFloat(node.getReactiveLoad(), 41, 48);
                writer.writeFloat(node.getActivePowerGeneration(), 49, 56);
                writer.writeFloat(node.getReactivePowerGeneration(), 57, 64);
                writer.writeFloat(node.getMinimumPermissibleActivePowerGeneration(), 65, 72);
                writer.writeFloat(node.getMaximumPermissibleActivePowerGeneration(), 73, 80);
                writer.writeFloat(node.getMinimumPermissibleReactivePowerGeneration(), 81, 88);
                writer.writeFloat(node.getMaximumPermissibleReactivePowerGeneration(), 89, 96);
                writer.writeFloat(node.getStaticOfPrimaryControl(), 97, 102);
                writer.writeFloat(node.getNominalPowerPrimaryControl(), 103, 110);
                writer.writeFloat(node.getThreePhaseShortCircuitPower(), 111, 118);
                writer.writeFloat(node.getXrRatio(), 119, 126);
                writer.writeEnumValue(node.getPowerPlantType(), 127);
                writer.newLine();
            }
        }
    }

    private static void writeElementId(UcteElementId id, UcteRecordWriter writer) {
        writeNodeCode(id.getNodeCode1(), writer, 0);
        writeNodeCode(id.getNodeCode2(), writer, 9);
        writer.writeChar(id.getOrderCode(), 18);
    }

    private void writeLineBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing line block");
        writer.writeString("##L", 0, 3);
        writer.newLine();
        network.getLines().stream().sorted(Comparator.comparing(UcteLine::getId))
                .forEach(ucteLine -> writeLine(writer, ucteLine));

    }

    private void writeLine(UcteRecordWriter writer, UcteLine ucteLine) {
        writeElementId(ucteLine.getId(), writer);
        writer.writeInteger(ucteLine.getStatus().getCode(), 20);
        writer.writeFloat(ucteLine.getResistance(), 22, 28);
        writer.writeFloat(ucteLine.getReactance(), 29, 35);
        writer.writeFloat((float) (ucteLine.getSusceptance() / Math.pow(10, -6)), 36, 44);
        writer.writeInteger(ucteLine.getCurrentLimit(), 45, 51);
        writer.writeString(ucteLine.getElementName(), 52, 64);
        try {
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeTransformerBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing transformer block");
        writer.writeString("##T", 0, 3);
        writer.newLine();
        ArrayList<UcteTransformer> transformersList = new ArrayList<>(network.getTransformers());
        transformersList.sort(Comparator.comparing(UcteTransformer::getId));
        for (UcteTransformer t : transformersList) {
            writeElementId(t.getId(), writer);
            writer.writeInteger(t.getStatus().getCode(), 20);
            writer.writeFloat(t.getRatedVoltage1(), 22, 27);
            writer.writeFloat(t.getRatedVoltage2(), 28, 33);
            writer.writeFloat(t.getNominalPower(), 34, 39);
            writer.writeFloat(t.getResistance(), 40, 46);
            writer.writeFloat(t.getReactance(), 47, 53);
            writer.writeFloat((float) (t.getSusceptance() / Math.pow(10, -6)), 54, 62);
            writer.writeFloat((float) (t.getConductance() / Math.pow(10, -6)), 63, 69);
            writer.writeInteger(t.getCurrentLimit(), 70, 76);
            writer.writeString(t.getElementName(), 77, 89);
            writer.newLine();
        }
    }

    private void writePhaseRegulation(UctePhaseRegulation pr, UcteRecordWriter writer) {
        writer.writeFloat(pr != null ? pr.getDu() : Float.NaN, 20, 25);
        writer.writeInteger(pr != null ? pr.getN() : null, 26, 28);
        writer.writeInteger(pr != null ? pr.getNp() : null, 29, 32);
        writer.writeFloat(pr != null ? pr.getU() : Float.NaN, 33, 38);
    }

    private void writeAngleRegulation(UcteAngleRegulation ar, UcteRecordWriter writer) {
        writer.writeFloat(ar != null ? ar.getDu() : Float.NaN, 39, 44);
        writer.writeFloat(ar != null ? ar.getTheta() : Float.NaN, 45, 50);
        writer.writeInteger(ar != null ? ar.getN() : null, 51, 53);
        writer.writeInteger(ar != null ? ar.getNp() : null, 54, 57);
        writer.writeFloat(ar != null ? ar.getP() : Float.NaN, 58, 63);
        writer.writeEnumValue(ar != null ? ar.getType() : null, 64, 68);
    }

    private void writeRegulationBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing regulation block");
        writer.writeString("##R", 0, 3);
        writer.newLine();
        network.getRegulations().stream().sorted(Comparator.comparing(UcteRegulation::getTransfoId))
                .forEach(ucteRegulation -> writeRegulation(writer, ucteRegulation));
    }

    private void writeRegulation(UcteRecordWriter writer, UcteRegulation ucteRegulation) {
        writeElementId(ucteRegulation.getTransfoId(), writer);
        writePhaseRegulation(ucteRegulation.getPhaseRegulation(), writer);
        writeAngleRegulation(ucteRegulation.getAngleRegulation(), writer);
        try {
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(BufferedWriter bw) throws IOException {
        long start = System.currentTimeMillis();
        UcteRecordWriter rw = new UcteRecordWriter(bw);
        writeCommentBlock(rw);
        writeNodeBlock(rw);
        writeLineBlock(rw);
        writeTransformerBlock(rw);
        writeRegulationBlock(rw);
        LOGGER.debug("UCTE file written in {} ms", System.currentTimeMillis() - start);
    }

}

