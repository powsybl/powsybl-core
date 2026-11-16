/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.*;
import us.hebi.matlab.mat.util.Casts;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public final class MatpowerWriter {

    private MatpowerWriter() {
    }

    private static Struct fillMatStruct(Struct struct, MatpowerModel model, boolean withBusNames) {

        Matrix busesM = fillBusesMatrix(model.getBuses());
        Cell busesNames = fillBusesNames(model.getBuses(), withBusNames);

        Matrix gensM = fillGeneratorsMatrix(model.getGenerators(), model.getVersion());
        Matrix branchesM = fillBranchesMatrix(model.getBranches());
        Matrix dcLinesM = fillDcLinesMatrix(model.getDcLines());

        struct.set("version", Mat5.newString(model.getVersion().toString()))
                .set("baseMVA", Mat5.newScalar(model.getBaseMva()))
                .set("bus", busesM)
                .set("gen", gensM)
                .set("branch", branchesM);
        if (dcLinesM != null) {
            struct.set("dcline", dcLinesM);
        }
        if (busesNames != null) {
            struct.set("bus_name", busesNames);
        }
        return struct;
    }

    private static Matrix fillBusesMatrix(List<MBus> buses) {
        Matrix busesM = Mat5.newMatrix(buses.size(), MatpowerReader.MATPOWER_BUSES_COLUMNS);

        for (int row = 0; row < buses.size(); row++) {
            MBus bus = buses.get(row);
            busesM.setInt(row, 0, bus.getNumber());
            busesM.setInt(row, 1, bus.getType().getValue());
            busesM.setDouble(row, 2, bus.getRealPowerDemand());
            busesM.setDouble(row, 3, bus.getReactivePowerDemand());
            busesM.setDouble(row, 4, bus.getShuntConductance());
            busesM.setDouble(row, 5, bus.getShuntSusceptance());
            busesM.setInt(row, 6, bus.getAreaNumber());
            busesM.setDouble(row, 7, bus.getVoltageMagnitude());
            busesM.setDouble(row, 8, bus.getVoltageAngle());
            busesM.setDouble(row, 9, bus.getBaseVoltage());
            busesM.setInt(row, 10, bus.getLossZone());
            busesM.setDouble(row, 11, bus.getMaximumVoltageMagnitude());
            busesM.setDouble(row, 12, bus.getMinimumVoltageMagnitude());
        }
        return busesM;
    }

    private static Cell fillBusesNames(List<MBus> buses, boolean withBusNames) {
        Cell busesNames = null;
        if (withBusNames) {
            for (int row = 0; row < buses.size(); row++) {
                MBus bus = buses.get(row);
                if (bus.getName() != null) {
                    if (busesNames == null) {
                        busesNames = Mat5.newCell(buses.size(), 1);
                    }
                    char[] chars = bus.getName().toCharArray();
                    Char mChar = Mat5.newChar(1, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        mChar.setChar(i, chars[i]);
                    }
                    busesNames.set(row, 0, mChar);
                }
            }
        }
        return busesNames;
    }

    private static Matrix fillGeneratorsMatrix(List<MGen> gens, MatpowerFormatVersion version) {
        Matrix gensM = Mat5.newMatrix(gens.size(), version.getGeneratorColumns());

        for (int row = 0; row < gens.size(); row++) {
            gensM.setInt(row, 0, gens.get(row).getNumber());
            gensM.setDouble(row, 1, gens.get(row).getRealPowerOutput());
            gensM.setDouble(row, 2, gens.get(row).getReactivePowerOutput());
            gensM.setDouble(row, 3, gens.get(row).getMaximumReactivePowerOutput());
            gensM.setDouble(row, 4, gens.get(row).getMinimumReactivePowerOutput());
            gensM.setDouble(row, 5, gens.get(row).getVoltageMagnitudeSetpoint());
            gensM.setDouble(row, 6, gens.get(row).getTotalMbase());
            gensM.setInt(row, 7, gens.get(row).getStatus());
            gensM.setDouble(row, 8, gens.get(row).getMaximumRealPowerOutput());
            gensM.setDouble(row, 9, gens.get(row).getMinimumRealPowerOutput());
            gensM.setDouble(row, 10, gens.get(row).getPc1());
            gensM.setDouble(row, 11, gens.get(row).getPc2());
            gensM.setDouble(row, 12, gens.get(row).getQc1Min());
            gensM.setDouble(row, 13, gens.get(row).getQc1Max());
            gensM.setDouble(row, 14, gens.get(row).getQc2Min());
            gensM.setDouble(row, 15, gens.get(row).getQc2Max());
            gensM.setDouble(row, 16, gens.get(row).getRampAgc());
            gensM.setDouble(row, 17, gens.get(row).getRampTenMinutes());
            gensM.setDouble(row, 18, gens.get(row).getRampThirtyMinutes());
            gensM.setDouble(row, 19, gens.get(row).getRampQ());
            gensM.setDouble(row, 20, gens.get(row).getApf());
        }
        return gensM;
    }

    private static Matrix fillBranchesMatrix(List<MBranch> branches) {
        Matrix branchesM = Mat5.newMatrix(branches.size(), MatpowerReader.MATPOWER_BRANCHES_COLUMNS);

        for (int row = 0; row < branches.size(); row++) {
            branchesM.setInt(row, 0, branches.get(row).getFrom());
            branchesM.setInt(row, 1, branches.get(row).getTo());
            branchesM.setDouble(row, 2, branches.get(row).getR());
            branchesM.setDouble(row, 3, branches.get(row).getX());
            branchesM.setDouble(row, 4, branches.get(row).getB());
            branchesM.setDouble(row, 5, branches.get(row).getRateA());
            branchesM.setDouble(row, 6, branches.get(row).getRateB());
            branchesM.setDouble(row, 7, branches.get(row).getRateC());
            branchesM.setDouble(row, 8, branches.get(row).getRatio());
            branchesM.setDouble(row, 9, branches.get(row).getPhaseShiftAngle());
            branchesM.setInt(row, 10, branches.get(row).getStatus());
            branchesM.setDouble(row, 11, branches.get(row).getAngMin());
            branchesM.setDouble(row, 12, branches.get(row).getAngMax());
        }
        return branchesM;
    }

    private static Matrix fillDcLinesMatrix(List<MDcLine> dcLines) {
        if (dcLines.isEmpty()) {
            return null;
        }

        Matrix dcLinesM = Mat5.newMatrix(dcLines.size(), MatpowerReader.MATPOWER_DCLINES_COLUMNS);

        for (int row = 0; row < dcLines.size(); row++) {
            dcLinesM.setInt(row, 0, dcLines.get(row).getFrom());
            dcLinesM.setInt(row, 1, dcLines.get(row).getTo());
            dcLinesM.setInt(row, 2, dcLines.get(row).getStatus());
            dcLinesM.setDouble(row, 3, dcLines.get(row).getPf());
            dcLinesM.setDouble(row, 4, dcLines.get(row).getPt());
            dcLinesM.setDouble(row, 5, dcLines.get(row).getQf());
            dcLinesM.setDouble(row, 6, dcLines.get(row).getQt());
            dcLinesM.setDouble(row, 7, dcLines.get(row).getVf());
            dcLinesM.setDouble(row, 8, dcLines.get(row).getVt());
            dcLinesM.setDouble(row, 9, dcLines.get(row).getPmin());
            dcLinesM.setDouble(row, 10, dcLines.get(row).getPmax());
            dcLinesM.setDouble(row, 11, dcLines.get(row).getQminf());
            dcLinesM.setDouble(row, 12, dcLines.get(row).getQmaxf());
            dcLinesM.setDouble(row, 13, dcLines.get(row).getQmint());
            dcLinesM.setDouble(row, 14, dcLines.get(row).getQmaxt());
            dcLinesM.setDouble(row, 15, dcLines.get(row).getLoss0());
            dcLinesM.setDouble(row, 16, dcLines.get(row).getLoss1());
        }
        return dcLinesM;
    }

    public static void write(MatpowerModel model, OutputStream oStream, boolean withBusNames) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(oStream);
        try (WritableByteChannel channel = Channels.newChannel(oStream)) {
            try (Struct struct = fillMatStruct(Mat5.newStruct(), model, withBusNames)) {
                try (MatFile matFile = Mat5.newMatFile()) {
                    matFile.addArray(MatpowerReader.MATPOWER_STRUCT_NAME, struct);
                    channel.write(getByteBuffer(matFile));
                }
            }
        }
    }

    public static void write(MatpowerModel model, Path pFile, boolean withBusNames) throws IOException {
        Objects.requireNonNull(pFile);
        write(model, Files.newOutputStream(pFile), withBusNames);
    }

    private static ByteBuffer getByteBuffer(MatFile matFile) throws IOException {
        int bufferSize = Casts.sint32(matFile.getUncompressedSerializedSize());
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        matFile.writeTo(Sinks.wrap(buffer));
        buffer.flip();
        return buffer;
    }
}
