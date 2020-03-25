/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerReader.class);

    enum MatpowerSection {
        BUS,
        BRANCH,
        GENERATOR
    }

    public static final String MATPOWER_SUPPORTED_VERSION = "2";

    private String processCaseName(String str) {
        String str2 = str.replace(';', ' ');
        final StringTokenizer st = new StringTokenizer(str2, " ");
        st.nextToken(); // function
        st.nextToken(); // mpc
        st.nextToken(); // =
        return st.nextToken();
    }

    private String processMatlabAssignment(String str) {
        Objects.requireNonNull(str);
        String str2 = str.replace(';', ' ');
        final StringTokenizer st = new StringTokenizer(str2, " ");
        st.nextToken(); // mpc.XYZ
        st.nextToken(); // =
        return st.nextToken();
    }

    private String processMatlabStringAssignment(String str) {
        return processMatlabAssignment(str).replace("'", "");
    }

    public MatpowerModel read(InputStream iStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(iStream))) {
            return read(reader);
        }
    }

    private boolean canSkipLine(String line) {
        return line.startsWith("%") || (line.trim().length() == 0);
    }

    public MatpowerModel read(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        String title = processCaseName(line);
        MatpowerModel model = new MatpowerModel(title);

        MatpowerSection section = null;
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (canSkipLine(line)) {
                //skip comments and empty lines
            } else if (line.startsWith("mpc.version ")) {
                processVersion(line, model);
            }  else if (line.startsWith("mpc.baseMVA ")) {
                processBaseMva(line, model);
            } else if (line.startsWith("mpc.bus ")) {
                section = MatpowerSection.BUS;
            } else if (line.startsWith("mpc.gen ")) {
                section = MatpowerSection.GENERATOR;
            } else if (line.startsWith("mpc.branch ")) {
                section = MatpowerSection.BRANCH;
            } else if (line.startsWith("];")) {
                section = processEndSection(model, section, lines);
            } else {
                if (section != null) {
                    lines.add(line);
                }
            }
        }

        return model;
    }

    private void processBaseMva(String line, MatpowerModel model) {
        Double baseMva = Double.parseDouble(processMatlabAssignment(line));
        model.setBaseMva(baseMva);
    }

    private MatpowerSection processEndSection(MatpowerModel model, MatpowerSection section, List<String> lines) {
        if (section != null) {
            parseLines(lines, model, section);
            lines.clear();
            return null;
        }
        return section;
    }

    private void processVersion(String line, MatpowerModel model) {
        String version = processMatlabStringAssignment(line);
        if (!version.equals(MATPOWER_SUPPORTED_VERSION)) {
            throw new IllegalStateException("unsupported MATPOWER version file: " + version);
        }
        model.setVersion(version);
    }

    private void parseLines(List<String> lines, MatpowerModel model, MatpowerSection section) {
        switch (section) {
            case BUS:
                model.getBuses().addAll(parseLines(lines, MBus.class));
                break;
            case GENERATOR:
                model.getGenerators().addAll(parseLines(lines, MGen.class));
                break;
            case BRANCH:
                model.getBranches().addAll(parseLines(lines, MBranch.class));
                break;
            default:
                throw new IllegalStateException("Section unknown: " + section);
        }
    }

    private static <T> List<T> parseLines(List<String> lines, Class<T> aClass) {
        LOGGER.debug("Parsing data for class {}", aClass);
        BeanListProcessor<T> rowProcessor = new BeanListProcessor<>(aClass);
        TsvParserSettings settings = new TsvParserSettings();
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(false);
        settings.setLineSeparatorDetectionEnabled(true);
        settings.getFormat().setLineSeparator(";");
        TsvParser parser = new TsvParser(settings);
        lines.stream().map(String::trim).forEach(parser::parseLine);
        return rowProcessor.getBeans();
    }
}
