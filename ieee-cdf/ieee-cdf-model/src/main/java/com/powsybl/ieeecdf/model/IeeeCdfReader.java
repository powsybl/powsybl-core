/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static com.powsybl.ieeecdf.model.reader.IeeeCdfBranchReader.parseBranches;
import static com.powsybl.ieeecdf.model.reader.IeeeCdfBusReader.parseBuses;
import static com.powsybl.ieeecdf.model.reader.IeeeCdfInterchangeDataReader.parseInterchangeData;
import static com.powsybl.ieeecdf.model.reader.IeeeCdfLossZoneReader.parseLossZones;
import static com.powsybl.ieeecdf.model.reader.IeeeCdfTieLineReader.parseTieLine;
import static com.powsybl.ieeecdf.model.reader.IeeeCdfTitleReader.parseTitle;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfReader {

    public IeeeCdfModel read(BufferedReader reader) throws IOException {
        String line;

        // Ensure malformed input does not trigger unexpected ArrayIndexOutOfBoundException
        List<IeeeCdfTitle> parsedLines = parseTitle(reader);
        if (parsedLines.isEmpty()) {
            throw new IllegalArgumentException("Failed to parse the IeeeCdfModel");
        }

        IeeeCdfTitle title = parsedLines.getFirst();
        IeeeCdfModel model = new IeeeCdfModel(title);

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("BUS DATA FOLLOWS")) {
                model.getBuses().addAll(parseBuses(reader));
            } else if (line.startsWith("BRANCH DATA FOLLOWS")) {
                model.getBranches().addAll(parseBranches(reader));
            } else if (line.startsWith("LOSS ZONES FOLLOWS")) {
                model.getLossZones().addAll(parseLossZones(reader));
            } else if (line.startsWith("INTERCHANGE DATA FOLLOWS")) {
                model.getInterchangeData().addAll(parseInterchangeData(reader));
            } else if (line.startsWith("TIE LINES FOLLOWS ")) {
                model.getTieLines().addAll(parseTieLine(reader));
            }
        }

        return model;
    }
}
