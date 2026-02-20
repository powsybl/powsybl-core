/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.elements.IeeeCdfTitle;
import com.powsybl.ieeecdf.model.reader.IeeeCdfBranchReader;
import com.powsybl.ieeecdf.model.reader.IeeeCdfBusReader;
import com.powsybl.ieeecdf.model.reader.IeeeCdfInterchangeDataReader;
import com.powsybl.ieeecdf.model.reader.IeeeCdfLossZoneReader;
import com.powsybl.ieeecdf.model.reader.IeeeCdfTieLineReader;
import com.powsybl.ieeecdf.model.reader.IeeeCdfTitleReader;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfReader {

    private static final Pattern PATTERN_BUS = Pattern.compile("BUS DATA FOLLOWS\\s+(\\d+)\\s+ITEMS");
    private static final Pattern PATTERN_BRANCH = Pattern.compile("BRANCH DATA FOLLOWS\\s+(\\d+)\\s+ITEMS");
    private static final Pattern PATTERN_LOSS_ZONE = Pattern.compile("LOSS ZONES FOLLOWS\\s+(\\d+)\\s+ITEMS");
    private static final Pattern PATTERN_INTERCHANGE = Pattern.compile("INTERCHANGE DATA FOLLOWS\\s+(\\d+)\\s+ITEMS");
    private static final Pattern PATTERN_TIE_LINE = Pattern.compile("TIE LINES FOLLOWS\\s+(\\d+)\\s+ITEMS");

    public IeeeCdfModel read(BufferedReader reader) throws IOException {
        String line;

        // Ensure malformed input does not trigger unexpected ArrayIndexOutOfBoundException
        IeeeCdfTitle title = IeeeCdfTitleReader.parseTitle(reader);
        if (title == null) {
            throw new IllegalArgumentException("Failed to parse the IeeeCdfModel");
        }

        IeeeCdfModel model = new IeeeCdfModel(title);

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("BUS DATA FOLLOWS")) {
                int expectedItemsNumber = getExpectedItemsNumber(line, PATTERN_BUS);
                model.getBuses().addAll(IeeeCdfBusReader.parseBuses(reader, expectedItemsNumber));
            } else if (line.startsWith("BRANCH DATA FOLLOWS")) {
                int expectedItemsNumber = getExpectedItemsNumber(line, PATTERN_BRANCH);
                model.getBranches().addAll(IeeeCdfBranchReader.parseBranches(reader, expectedItemsNumber));
            } else if (line.startsWith("LOSS ZONES FOLLOWS")) {
                int expectedItemsNumber = getExpectedItemsNumber(line, PATTERN_LOSS_ZONE);
                model.getLossZones().addAll(IeeeCdfLossZoneReader.parseLossZones(reader, expectedItemsNumber));
            } else if (line.startsWith("INTERCHANGE DATA FOLLOWS")) {
                int expectedItemsNumber = getExpectedItemsNumber(line, PATTERN_INTERCHANGE);
                model.getInterchangeData().addAll(IeeeCdfInterchangeDataReader.parseInterchangeData(reader, expectedItemsNumber));
            } else if (line.startsWith("TIE LINES FOLLOWS ")) {
                int expectedItemsNumber = getExpectedItemsNumber(line, PATTERN_TIE_LINE);
                model.getTieLines().addAll(IeeeCdfTieLineReader.parseTieLine(reader, expectedItemsNumber));
            }
        }

        return model;
    }

    private static int getExpectedItemsNumber(String line, Pattern pattern) {
        Matcher m = pattern.matcher(line);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new PowsyblException("Failed to parse the expected number of IEEE-CDF items in:" + line);
        }
    }
}
