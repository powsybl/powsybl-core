/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.powsybl.ieeecdf.model.writer.IeeeCdfBranchWriter;
import com.powsybl.ieeecdf.model.writer.IeeeCdfBusWriter;
import com.powsybl.ieeecdf.model.writer.IeeeCdfInterchangeDataWriter;
import com.powsybl.ieeecdf.model.writer.IeeeCdfLossZoneWriter;
import com.powsybl.ieeecdf.model.writer.IeeeCdfTieLineWriter;
import com.powsybl.ieeecdf.model.writer.IeeeCdfTitleWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfWriter {

    private final IeeeCdfModel model;

    public IeeeCdfWriter(IeeeCdfModel model) {
        this.model = Objects.requireNonNull(model);
    }

    public void write(BufferedWriter writer) throws IOException {

        IeeeCdfTitleWriter.writeTitle(writer, model.getTitle());
        IeeeCdfBusWriter.writeBuses(writer, model.getBuses());
        IeeeCdfBranchWriter.writeBranches(writer, model.getBranches());
        IeeeCdfLossZoneWriter.writeLossZone(writer, model.getLossZones());
        IeeeCdfInterchangeDataWriter.writeInterchangeData(writer, model.getInterchangeData());
        IeeeCdfTieLineWriter.writeTieLines(writer, model.getTieLines());

        writer.write("END OF DATA");
        writer.newLine();
    }
}
