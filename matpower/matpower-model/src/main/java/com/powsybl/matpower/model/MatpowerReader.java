/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public final class MatpowerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerReader.class);

    public static final String MATPOWER_STRUCT_NAME = "mpc";
    public static final MatpowerFormatVersion MATPOWER_SUPPORTED_VERSION = MatpowerFormatVersion.V2;
    public static final int MATPOWER_BUSES_COLUMNS = 13;
    public static final int MATPOWER_BRANCHES_COLUMNS = 13;
    public static final int MATPOWER_DCLINES_COLUMNS = 17;

    private MatpowerReader() {
    }

    public static MatpowerModel read(Path file, String caseName) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            return read(stream, caseName);
        }
    }

    public static MatpowerModel read(InputStream iStream, String caseName) throws IOException {
        Objects.requireNonNull(iStream);

        MatpowerModel model = null;
        try (MatFile mat = Mat5.newReader(Sources.wrapInputStream(iStream)).setEntryFilter(entry -> entry.getName().equals(MATPOWER_STRUCT_NAME)).readMat()) {
            if (mat.getNumEntries() == 0) {
                throw new IllegalStateException("no MATPOWER data: expected structure named '" + MATPOWER_STRUCT_NAME + "' not found.");
            }
            Struct mpcStruct = mat.getStruct(MATPOWER_STRUCT_NAME);
            Set<String> mpcNames = Sets.newHashSet("version", "baseMVA", "bus", "gen", "branch");
            Set<String> fieldNames = new HashSet<>(mpcStruct.getFieldNames());
            if (!fieldNames.containsAll(mpcNames)) {
                throw new IllegalStateException("expected MATPOWER variables not found: " + mpcNames);
            }
            MatpowerFormatVersion version = MatpowerFormatVersion.fromString(mpcStruct.get("version").toString().replace("'", ""));
            if (version != MATPOWER_SUPPORTED_VERSION) {
                throw new IllegalStateException("unsupported MATPOWER version: " + version);
            }

            double baseMVA = mpcStruct.getMatrix("baseMVA").getDouble(0);
            Matrix buses = mpcStruct.getMatrix("bus");
            Matrix generators = mpcStruct.getMatrix("gen");
            Matrix branches = mpcStruct.getMatrix("branch");
            Cell busesNames = null;
            if (fieldNames.contains("bus_name")) {
                busesNames = mpcStruct.getCell("bus_name");
            }
            Matrix dcLines = null;
            if (fieldNames.contains("dcline")) {
                dcLines = mpcStruct.getMatrix("dcline");
            }

            int busColumns = buses.getDimensions()[1];
            int generatorColumns = generators.getDimensions()[1];
            int branchColumns = branches.getDimensions()[1];
            Integer dcLineColumns = dcLines != null ? dcLines.getDimensions()[1] : null;
            VersionToRead versionToRead = checkNumberOfColumns(busColumns, generatorColumns, branchColumns, dcLineColumns);

            model = new MatpowerModel(caseName);
            model.setVersion(version);
            model.setBaseMva(baseMVA);

            readBuses(buses, busesNames, model);
            readGenerators(generators, versionToRead.generatorVersion, model);
            readBranches(branches, model);
            readDcLines(dcLines, model);
        }

        return model;
    }

    record VersionToRead(MatpowerFormatVersion generatorVersion) {
    }

    static VersionToRead checkNumberOfColumns(int busColumns, int generatorColumns, int branchColumns, Integer dcLineColumns) {
        if (busColumns < MATPOWER_BUSES_COLUMNS) {
            throw new PowsyblException("Unexpected number of columns for buses, expected at least " + MATPOWER_BUSES_COLUMNS + " columns, but got " + busColumns);
        }
        if (generatorColumns < MatpowerFormatVersion.V1.getGeneratorColumns()) {
            throw new PowsyblException("Unexpected number of columns for generators, expected at least " + MatpowerFormatVersion.V1.getGeneratorColumns() + " columns, but got " + generatorColumns);
        }
        MatpowerFormatVersion generatorVersionToRead;
        if (generatorColumns < MatpowerFormatVersion.V2.getGeneratorColumns()) {
            LOGGER.warn("It is not expected in Matpower v2 format to have less than {} columns for generators, reading {} columns instead as for v1 format",
                    MatpowerFormatVersion.V2.getGeneratorColumns(), MatpowerFormatVersion.V1.getGeneratorColumns());
            generatorVersionToRead = MatpowerFormatVersion.V1;
        } else {
            generatorVersionToRead = MatpowerFormatVersion.V2;
        }
        if (branchColumns < MATPOWER_BRANCHES_COLUMNS) {
            throw new PowsyblException("Unexpected number of columns for branches, expected at least " + MATPOWER_BRANCHES_COLUMNS + " columns, but got " + branchColumns);
        }
        if (dcLineColumns != null && dcLineColumns < MATPOWER_DCLINES_COLUMNS) {
            throw new PowsyblException("Unexpected number of columns for DC lines, expected at least " + MATPOWER_DCLINES_COLUMNS + " columns, but got " + dcLineColumns);
        }
        return new VersionToRead(generatorVersionToRead);
    }

    private static void readBuses(Matrix buses, Cell busesNames, MatpowerModel model) {
        for (int row = 0; row < buses.getDimensions()[0]; row++) {
            MBus bus = new MBus();
            bus.setNumber(buses.getInt(row, 0));
            if (busesNames != null) {
                String name = busesNames.getChar(row).getString();
                bus.setName(name);
            }
            bus.setType(MBus.Type.fromInt(buses.getInt(row, 1)));
            bus.setRealPowerDemand(buses.getDouble(row, 2));
            bus.setReactivePowerDemand(buses.getDouble(row, 3));
            bus.setShuntConductance(buses.getDouble(row, 4));
            bus.setShuntSusceptance(buses.getDouble(row, 5));
            bus.setAreaNumber(buses.getInt(row, 6));
            bus.setVoltageMagnitude(buses.getDouble(row, 7));
            bus.setVoltageAngle(buses.getDouble(row, 8));
            bus.setBaseVoltage(buses.getDouble(row, 9));
            bus.setLossZone(buses.getInt(row, 10));
            bus.setMaximumVoltageMagnitude(buses.getDouble(row, 11));
            bus.setMinimumVoltageMagnitude(buses.getDouble(row, 12));

            model.addBus(bus);
        }
    }

    private static void readGenerators(Matrix generators, MatpowerFormatVersion generatorVersionToRead, MatpowerModel model) {
        for (int row = 0; row < generators.getDimensions()[0]; row++) {
            MGen gen = new MGen();
            gen.setNumber(generators.getInt(row, 0));
            gen.setRealPowerOutput(generators.getDouble(row, 1));
            gen.setReactivePowerOutput(generators.getDouble(row, 2));
            gen.setMaximumReactivePowerOutput(generators.getDouble(row, 3));
            gen.setMinimumReactivePowerOutput(generators.getDouble(row, 4));
            gen.setVoltageMagnitudeSetpoint(generators.getDouble(row, 5));
            gen.setTotalMbase(generators.getDouble(row, 6));
            gen.setStatus(generators.getInt(row, 7));
            gen.setMaximumRealPowerOutput(generators.getDouble(row, 8));
            gen.setMinimumRealPowerOutput(generators.getDouble(row, 9));
            if (generatorVersionToRead == MatpowerFormatVersion.V2) {
                gen.setPc1(generators.getDouble(row, 10));
                gen.setPc2(generators.getDouble(row, 11));
                gen.setQc1Min(generators.getDouble(row, 12));
                gen.setQc1Max(generators.getDouble(row, 13));
                gen.setQc2Min(generators.getDouble(row, 14));
                gen.setQc2Max(generators.getDouble(row, 15));
                gen.setRampAgc(generators.getDouble(row, 16));
                gen.setRampTenMinutes(generators.getDouble(row, 17));
                gen.setRampThirtyMinutes(generators.getDouble(row, 18));
                gen.setRampQ(generators.getDouble(row, 19));
                gen.setApf(generators.getDouble(row, 20));
            }

            model.addGenerator(gen);
        }
    }

    private static void readBranches(Matrix branches, MatpowerModel model) {
        for (int row = 0; row < branches.getDimensions()[0]; row++) {
            MBranch branch = new MBranch();
            branch.setFrom(branches.getInt(row, 0));
            branch.setTo(branches.getInt(row, 1));
            branch.setR(branches.getDouble(row, 2));
            branch.setX(branches.getDouble(row, 3));
            branch.setB(branches.getDouble(row, 4));
            branch.setRateA(branches.getDouble(row, 5));
            branch.setRateB(branches.getDouble(row, 6));
            branch.setRateC(branches.getDouble(row, 7));
            branch.setRatio(branches.getDouble(row, 8));
            branch.setPhaseShiftAngle(branches.getDouble(row, 9));
            branch.setStatus(branches.getInt(row, 10));
            branch.setAngMin(branches.getDouble(row, 11));
            branch.setAngMax(branches.getDouble(row, 12));
            model.addBranch(branch);
        }
    }

    private static void readDcLines(Matrix dcLines, MatpowerModel model) {
        if (dcLines == null) {
            return;
        }
        for (int row = 0; row < dcLines.getDimensions()[0]; row++) {
            MDcLine dcLine = new MDcLine();

            dcLine.setFrom(dcLines.getInt(row, 0));
            dcLine.setTo(dcLines.getInt(row, 1));
            dcLine.setStatus(dcLines.getInt(row, 2));
            dcLine.setPf(dcLines.getDouble(row, 3));
            dcLine.setPt(dcLines.getDouble(row, 4));
            dcLine.setQf(dcLines.getDouble(row, 5));
            dcLine.setQt(dcLines.getDouble(row, 6));
            dcLine.setVf(dcLines.getDouble(row, 7));
            dcLine.setVt(dcLines.getDouble(row, 8));
            dcLine.setPmin(dcLines.getDouble(row, 9));
            dcLine.setPmax(dcLines.getDouble(row, 10));
            dcLine.setQminf(dcLines.getDouble(row, 11));
            dcLine.setQmaxf(dcLines.getDouble(row, 12));
            dcLine.setQmint(dcLines.getDouble(row, 13));
            dcLine.setQmaxt(dcLines.getDouble(row, 14));
            dcLine.setLoss0(dcLines.getDouble(row, 15));
            dcLine.setLoss1(dcLines.getDouble(row, 16));

            model.addDcLine(dcLine);
        }
    }
}
