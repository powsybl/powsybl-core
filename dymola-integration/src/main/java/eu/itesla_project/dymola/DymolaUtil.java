/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.computation.ExecutionError;
import eu.itesla_project.computation.ExecutionReport;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaUtil {


    static final String PRODUCT_NAME = "Dymola";
    static final String VERSION = "2016";

    static final String DYMOLAINPUTZIPFILENAMEPREFIX ="dymolainput";
    static final String DYMOLAOUTPUTZIPFILENAMEPREFIX ="dymolaoutput";
    static final String DYMOLA_SIM_MODEL_INPUT_PREFIX ="network";
    static final String DYMOLA_SIM_MAT_OUTPUT_PREFIX ="dymolasim";


    private DymolaUtil() {
    }

    public static Map<String, String> createEnv(DymolaConfig config) {
        Map<String, String> env = new HashMap<>();
        if (config.getIndexesBinDir() != null) {
            if (env.containsKey("PATH")) {
                env.put("PATH", env.get("PATH") + ":" + config.getIndexesBinDir().toString());
            } else {
                env.put("PATH", config.getIndexesBinDir().toString());
            }
        }
        return env;
    }

    static void putBadExitCode(String report, Map<String, String> metrics) {
        if (!"".equals(report)) {
            metrics.put("badExitCode", report);
        }
    }

    static void putBadExitCode(ExecutionReport report, Map<String, String> metrics) {
        if (report.getErrors().size() > 0) {
            ExecutionError lastError = report.getErrors().get(report.getErrors().size()-1);
            metrics.put("badExitCode", Integer.toString(lastError.getExitCode()));
        }
    }

    public static void dumpStateId(Path workingDir, String stateId) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(workingDir.resolve("state.txt"), StandardCharsets.UTF_8)) {
            writer.write(stateId);
            writer.newLine();
        }
    }

    public static void dumpStateId(Path workingDir, Network network) throws IOException {
        dumpStateId(workingDir, network.getStateManager().getWorkingStateId());
    }

}

