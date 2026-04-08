/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
        Map<UcteCountryCode, TreeSet<UcteNode>> nodesByCountry = new EnumMap<>(UcteCountryCode.class);
        for (UcteNode node : network.getNodes()) {
            nodesByCountry.computeIfAbsent(node.getCode().getUcteCountryCode(), k -> new TreeSet<>()).add(node);
        }

        for (Map.Entry<UcteCountryCode, TreeSet<UcteNode>> entry : nodesByCountry.entrySet()) {
            UcteCountryCode countryCode = entry.getKey();
            TreeSet<UcteNode> nodes = entry.getValue();
            writer.writeString("##Z" + countryCode, 0, 5);
            writer.newLine();
            for (UcteNode node : nodes) {
                writeNodeCode(node.getCode(), writer, 0);
                writer.writeString(node.getGeographicalName(), 9, 21);
                writer.writeEnumOrdinal(node.getStatus(), 22);
                writer.writeEnumOrdinal(node.getTypeCode(), 24);
                writer.writeDouble(node.getVoltageReference(), 26, 32);
                writer.writeDouble(node.getActiveLoad(), 33, 40);
                writer.writeDouble(node.getReactiveLoad(), 41, 48);
                writer.writeDouble(node.getActivePowerGeneration(), 49, 56);
                writer.writeDouble(node.getReactivePowerGeneration(), 57, 64);
                writer.writeDouble(node.getMinimumPermissibleActivePowerGeneration(), 65, 72);
                writer.writeDouble(node.getMaximumPermissibleActivePowerGeneration(), 73, 80);
                writer.writeDouble(node.getMinimumPermissibleReactivePowerGeneration(), 81, 88);
                writer.writeDouble(node.getMaximumPermissibleReactivePowerGeneration(), 89, 96);
                writer.writeDouble(node.getStaticOfPrimaryControl(), 97, 102);
                writer.writeDouble(node.getNominalPowerPrimaryControl(), 103, 110);
                writer.writeDouble(node.getThreePhaseShortCircuitPower(), 111, 118);
                writer.writeDouble(node.getXrRatio(), 119, 126);
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
        List<UcteLine> lines = network.getLines().stream().sorted(Comparator.comparing(UcteElement::getId)).toList();
        for (UcteLine ucteLine : lines) {
            writeElementId(ucteLine.getId(), writer);
            writer.writeInteger(ucteLine.getStatus().getCode(), 20);
            writer.writeDouble(ucteLine.getResistance(), 22, 28);
            writer.writeDouble(ucteLine.getReactance(), 29, 35);
            writer.writeDouble(ucteLine.getSusceptance() / Math.pow(10, -6), 36, 44);
            writer.writeInteger(ucteLine.getCurrentLimit(), 45, 51);
            writer.writeString(ucteLine.getElementName(), 52, 64);
            writer.newLine();
        }
    }

    private void writeTransformerBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing transformer block");
        writer.writeString("##T", 0, 3);
        writer.newLine();
        List<UcteTransformer> transformersList =
                network.getTransformers().stream().sorted(Comparator.comparing(UcteTransformer::getId)).toList();
        for (UcteTransformer t : transformersList) {
            writeElementId(t.getId(), writer);
            writer.writeInteger(t.getStatus().getCode(), 20);
            writer.writeDouble(t.getRatedVoltage1(), 22, 27);
            writer.writeDouble(t.getRatedVoltage2(), 28, 33);
            writer.writeDouble(t.getNominalPower(), 34, 39);
            writer.writeDouble(t.getResistance(), 40, 46);
            writer.writeDouble(t.getReactance(), 47, 53);
            writer.writeDouble(t.getSusceptance() / Math.pow(10, -6), 54, 62);
            writer.writeDouble(t.getConductance() / Math.pow(10, -6), 63, 69);
            writer.writeInteger(t.getCurrentLimit(), 70, 76);
            writer.writeString(t.getElementName(), 77, 89);
            writer.newLine();
        }
    }

    private void writePhaseRegulation(UctePhaseRegulation pr, UcteRecordWriter writer) {
        writer.writeDouble(pr != null ? pr.getDu() : Double.NaN, 20, 25);
        writer.writeInteger(pr != null ? pr.getN() : null, 26, 28);
        writer.writeInteger(pr != null ? pr.getNp() : null, 29, 32);
        writer.writeDouble(pr != null ? pr.getU() : Double.NaN, 33, 38);
    }

    private void writeAngleRegulation(UcteAngleRegulation ar, UcteRecordWriter writer) {
        writer.writeDouble(ar != null ? ar.getDu() : Double.NaN, 39, 44);
        writer.writeDouble(ar != null ? ar.getTheta() : Double.NaN, 45, 50);
        writer.writeInteger(ar != null ? ar.getN() : null, 51, 53);
        writer.writeInteger(ar != null ? ar.getNp() : null, 54, 57);
        writer.writeDouble(ar != null ? ar.getP() : Double.NaN, 58, 63);
        writer.writeEnumValue(ar != null ? ar.getType() : null, 64, 68);
    }

    private void writeRegulationBlock(UcteRecordWriter writer) throws IOException {
        LOGGER.trace("Writing regulation block");
        writer.writeString("##R", 0, 3);
        writer.newLine();
        List<UcteRegulation> regulations = network.getRegulations().stream().sorted(Comparator.comparing(UcteRegulation::getTransfoId)).toList();
        for (UcteRegulation ucteRegulation : regulations) {
            writeRegulation(writer, ucteRegulation);
        }
    }

    private void writeRegulation(UcteRecordWriter writer, UcteRegulation ucteRegulation) throws IOException {
        writeElementId(ucteRegulation.getTransfoId(), writer);
        writePhaseRegulation(ucteRegulation.getPhaseRegulation(), writer);
        writeAngleRegulation(ucteRegulation.getAngleRegulation(), writer);
        writer.newLine();
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
