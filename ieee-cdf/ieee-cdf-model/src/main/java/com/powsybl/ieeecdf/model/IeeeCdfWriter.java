/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

import static com.powsybl.ieeecdf.model.writer.IeeeCdfBranchWriter.writeBranches;
import static com.powsybl.ieeecdf.model.writer.IeeeCdfBusWriter.writeBuses;
import static com.powsybl.ieeecdf.model.writer.IeeeCdfInterchangeDataWriter.writeInterchangeData;
import static com.powsybl.ieeecdf.model.writer.IeeeCdfLossZoneWriter.writeLossZone;
import static com.powsybl.ieeecdf.model.writer.IeeeCdfTieLineWriter.writeTieLines;
import static com.powsybl.ieeecdf.model.writer.IeeeCdfTitleWriter.writeTitle;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfWriter {

    private final IeeeCdfModel model;

    public IeeeCdfWriter(IeeeCdfModel model) {
        this.model = Objects.requireNonNull(model);
    }

    public void write(BufferedWriter writer) throws IOException {

        writeTitle(writer, model.getTitle());
        writeBuses(writer, model.getBuses());
        writeBranches(writer, model.getBranches());
        writeLossZone(writer, model.getLossZones());
        writeInterchangeData(writer, model.getInterchangeData());
        writeTieLines(writer, model.getTieLines());

        writer.write("END OF DATA");
        writer.newLine();
    }
}
