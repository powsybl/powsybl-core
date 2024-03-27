/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.fixed.FixedWidthWriter;
import com.univocity.parsers.fixed.FixedWidthWriterSettings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfWriter {

    private final IeeeCdfModel model;

    public IeeeCdfWriter(IeeeCdfModel model) {
        this.model = Objects.requireNonNull(model);
    }

    private static <T> void writeRecords(Writer writer, List<T> beans, Class<T> aClass) {
        FixedWidthWriterSettings settings = new FixedWidthWriterSettings();
        settings.setWriteLineSeparatorAfterRecord(true);
        BeanWriterProcessor<T> processor = new BeanWriterProcessor<>(aClass);
        settings.setRowWriterProcessor(processor);
        new FixedWidthWriter(writer, settings).processRecords(beans);
    }

    public void write(BufferedWriter writer) throws IOException {

        writeRecords(writer, Collections.singletonList(model.getTitle()), IeeeCdfTitle.class);

        writer.write(String.format("BUS DATA FOLLOWS                            %d ITEMS", model.getBuses().size()));
        writer.newLine();
        writeRecords(writer, model.getBuses(), IeeeCdfBus.class);
        writer.write("-999");
        writer.newLine();

        writer.write(String.format("BRANCH DATA FOLLOWS                         %d ITEMS", model.getBranches().size()));
        writer.newLine();
        writeRecords(writer, model.getBranches(), IeeeCdfBranch.class);
        writer.write("-999");
        writer.newLine();

        writer.write(String.format("LOSS ZONES FOLLOWS                     %d ITEMS", model.getLossZones().size()));
        writer.newLine();
        writeRecords(writer, model.getLossZones(), IeeeCdfLossZone.class);
        writer.write("-99");
        writer.newLine();

        writer.write(String.format("INTERCHANGE DATA FOLLOWS                 %d ITEMS", model.getInterchangeData().size()));
        writer.newLine();
        writeRecords(writer, model.getInterchangeData(), IeeeCdfInterchangeData.class);
        writer.write("-9");
        writer.newLine();

        writer.write(String.format("TIE LINES FOLLOWS                     %d ITEMS", model.getTieLines().size()));
        writer.newLine();
        writeRecords(writer, model.getTieLines(), IeeeCdfTieLine.class);
        writer.write("-999");
        writer.newLine();

        writer.write("END OF DATA");
        writer.newLine();
    }
}
