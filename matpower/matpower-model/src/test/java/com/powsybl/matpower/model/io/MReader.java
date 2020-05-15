/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MatpowerModel;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import static com.powsybl.matpower.model.MatpowerReader.MATPOWER_SUPPORTED_VERSION;

/**
 * A simple parser for MATPOWER .m files, used in tests.
 * It is not a generic .m parser, but it should be able to read files exported by MATPOWER savecase function.
 * Example files .m, found in MATPOWER distribution, should be interpreted correctly, too.
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class MReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MReader.class);

    enum MatpowerSection {
        BUS,
        BRANCH,
        GENERATOR
    }

    private MReader() {
    }

    public static MatpowerModel read(InputStream iStream) throws IOException {
        Objects.requireNonNull(iStream);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8))) {
            return read(reader);
        }
    }

    public static MatpowerModel read(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
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

    public static MatpowerModel read(Path file) throws IOException {
        Objects.requireNonNull(file);
        return read(Files.newInputStream(file));
    }

    private static String processCaseName(String str) {
        String str2 = str.replace(';', ' ');
        final StringTokenizer st = new StringTokenizer(str2, " ");
        st.nextToken(); // function
        st.nextToken(); // mpc
        st.nextToken(); // =
        return st.nextToken();
    }

    private static String processMatlabAssignment(String str) {
        Objects.requireNonNull(str);
        String str2 = str.replace(';', ' ');
        final StringTokenizer st = new StringTokenizer(str2, " ");
        st.nextToken(); // mpc.XYZ
        st.nextToken(); // =
        return st.nextToken();
    }

    private static String processMatlabStringAssignment(String str) {
        return processMatlabAssignment(str).replace("'", "");
    }

    private static boolean canSkipLine(String line) {
        return line.startsWith("%") || (line.trim().length() == 0);
    }

    private static void processBaseMva(String line, MatpowerModel model) {
        double baseMva = Double.parseDouble(processMatlabAssignment(line));
        model.setBaseMva(baseMva);
    }

    private static MatpowerSection processEndSection(MatpowerModel model, MatpowerSection section, List<String> lines) {
        if (section != null) {
            parseLines(lines, model, section);
            lines.clear();
            return null;
        }
        return section;
    }

    private static void processVersion(String line, MatpowerModel model) {
        String version = processMatlabStringAssignment(line);
        if (!version.equals(MATPOWER_SUPPORTED_VERSION)) {
            throw new IllegalStateException("unsupported MATPOWER version file: " + version);
        }
        model.setVersion(version);
    }

    private static void parseLines(List<String> lines, MatpowerModel model, MatpowerSection section) {
        switch (section) {
            case BUS:
                model.getBuses().addAll(parseLines(lines, MBusAnnotated.class));
                break;
            case GENERATOR:
                model.getGenerators().addAll(parseLines(lines, MGenAnnotated.class));
                break;
            case BRANCH:
                model.getBranches().addAll(parseLines(lines, MBranchAnnotated.class));
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
