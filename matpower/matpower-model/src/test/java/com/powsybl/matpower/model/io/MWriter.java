/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MBranch;
import com.powsybl.matpower.model.MBus;
import com.powsybl.matpower.model.MGen;
import com.powsybl.matpower.model.MatpowerModel;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class MWriter {

    private MWriter() {
    }

    private static <T> void writeRecords(Writer writer, List<T> beans, Class<T> aClass) {
        TsvWriterSettings settings = new TsvWriterSettings();
        settings.getFormat().setLineSeparator(";\n");
        BeanWriterProcessor<T> processor = new BeanWriterProcessor<>(aClass);
        settings.setRowWriterProcessor(processor);
        new TsvWriter(writer, settings).processRecords(beans);
    }

    public static void write(MatpowerModel model, Path file) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(file);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            write(model, writer);
        }
    }

    public static void write(MatpowerModel model, BufferedWriter writer) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(writer);

        writer.write(String.format("function mpc = %s", model.getCaseName()));
        writer.newLine();

        writer.write(String.format("mpc.version = '%s'", model.getVersion()));
        writer.newLine();

        writer.write("%% system MVA base");
        writer.newLine();
        writer.write(String.format("mpc.baseMVA = %f", model.getBaseMva()));
        writer.newLine();

        writer.newLine();
        writer.write("%% bus data");
        writer.newLine();
        writer.write("%\tbus_i\ttype\tPd\tQd\tGs\tBs\tarea\tVm\tVa\tbaseKV\tzone\tVmax\tVmin");
        writer.newLine();
        writer.write("mpc.bus = [");
        writer.newLine();
        writeRecords(writer, model.getBuses().stream().filter(MBus.class::isInstance).map(MBusAnnotated.class::cast).collect(Collectors.toList()), MBusAnnotated.class);
        writer.write("];");
        writer.newLine();

        writer.write("%% generator data");
        writer.newLine();
        writer.write("%\tbus\tPg\tQg\tQmax\tQmin\tVg\tmBase\tstatus\tPmax\tPmin\tPc1\tPc2\tQc1min\tQc1max\tQc2min\tQc2max\tramp_agc\tramp_10\tramp_30\tramp_q\tapf");
        writer.newLine();
        writer.write("mpc.gen = [");
        writer.newLine();
        writeRecords(writer, model.getGenerators().stream().filter(MGen.class::isInstance).map(MGenAnnotated.class::cast).collect(Collectors.toList()), MGenAnnotated.class);
        writer.write("];");
        writer.newLine();

        writer.newLine();
        writer.write("%% branch data");
        writer.newLine();
        writer.write("%\tfbus\ttbus\tr\tx\tb\trateA\trateB\trateC\tratio\tangle\tstatus\tangmin\tangmax");
        writer.newLine();
        writer.write("mpc.branch = [");
        writer.newLine();
        writeRecords(writer, model.getBranches().stream().filter(MBranch.class::isInstance).map(MBranchAnnotated.class::cast).collect(Collectors.toList()), MBranchAnnotated.class);
        writer.write("];");
        writer.newLine();
    }
}
