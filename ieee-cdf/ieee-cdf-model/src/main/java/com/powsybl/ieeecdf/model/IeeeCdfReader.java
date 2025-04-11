/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.fixed.FixedWidthParser;
import com.univocity.parsers.fixed.FixedWidthParserSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfReader {

    enum IeeeCdfSection {
        BUS,
        BRANCH,
        LOSS_ZONES,
        INTERCHANGE_DATA,
        TIE_LINES
    }

    public IeeeCdfModel read(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        // Ensure malformed input does not trigger unexpected ArrayIndexOutOfBoundException
        List<IeeeCdfTitle> parsedLines = parseLines(Collections.singletonList(line), IeeeCdfTitle.class);
        if (parsedLines.size() == 0) {
            throw new IllegalArgumentException("Failed to parse the IeeeCdfModel");
        }

        IeeeCdfTitle title = parsedLines.get(0);
        IeeeCdfModel model = new IeeeCdfModel(title);

        IeeeCdfSection section = null;
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("BUS DATA FOLLOWS")) {
                section = IeeeCdfSection.BUS;
            } else if (line.startsWith("BRANCH DATA FOLLOWS")) {
                section = IeeeCdfSection.BRANCH;
            } else if (line.startsWith("LOSS ZONES FOLLOWS")) {
                section = IeeeCdfSection.LOSS_ZONES;
            } else if (line.startsWith("INTERCHANGE DATA FOLLOWS")) {
                section = IeeeCdfSection.INTERCHANGE_DATA;
            } else if (line.startsWith("TIE LINES FOLLOWS ")) {
                section = IeeeCdfSection.TIE_LINES;
            } else if (line.startsWith("-9")) {
                if (section != null) {
                    parseLines(lines, model, section);
                    lines.clear();
                    section = null;
                }
            } else {
                if (section != null) {
                    lines.add(line);
                }
            }
        }

        return model;
    }

    private static <T> List<T> parseLines(List<String> lines, Class<T> aClass) {
        FixedWidthParserSettings settings = new FixedWidthParserSettings();
        BeanListProcessor<T> processor = new BeanListProcessor<>(aClass);
        settings.setProcessor(processor);
        FixedWidthParser parser = new FixedWidthParser(settings);
        for (String line : lines) {
            parser.parseLine(line);
        }
        return processor.getBeans();
    }

    private void parseLines(List<String> lines, IeeeCdfModel model, IeeeCdfSection section) {
        switch (section) {
            case BUS:
                model.getBuses().addAll(parseLines(lines, IeeeCdfBus.class));
                break;
            case BRANCH:
                model.getBranches().addAll(parseLines(lines, IeeeCdfBranch.class));
                break;
            case LOSS_ZONES:
                model.getLossZones().addAll(parseLines(lines, IeeeCdfLossZone.class));
                break;
            case INTERCHANGE_DATA:
                model.getInterchangeData().addAll(parseLines(lines, IeeeCdfInterchangeData.class));
                break;
            case TIE_LINES:
                model.getTieLines().addAll(parseLines(lines, IeeeCdfTieLine.class));
                break;
            default:
                throw new IllegalStateException("Section unknown: " + section);
        }
    }
}
