/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.commons.io.BufferedLineParser;
import eu.itesla_project.computation.ExecutionError;
import eu.itesla_project.computation.ExecutionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagUtil.class);

    public static final String PRODUCT_NAME = "Eurostag";
    public static final String VERSION = "5.1.1";

    private static final Pattern ERROR_PATTERN = Pattern.compile("  ERR-(\\d*).(\\d*):(.*)");
    private static final Pattern ITERATION_PATTERN = Pattern.compile("     ITERA. = (\\d*).(\\d*)     NOMBRE DE MISMATCHES HORS TOLERANCE  : (\\d*)");
    private static final String DIVERGENCE_MESSAGE = "FIN DES ITERATIONS DU LOAD FLOW AVANT CONVERGENCE";

    private static final List<String> HEADER_STEADY_STATE
            = Arrays.asList("",
                            "     MACHINE  MACROBLOC         NOM VAR.        SORT. BLOC    VALEUR EQU.      (P.U.)");

    private static final List<String> HEADER_INITIAL_VALUE
            = Arrays.asList("",
                            "    MACHINE            MACROBLOC   VARIABLE    BLOC DE SOR.  INITIA      LIMITE INF.       VALEUR      LIMITE SUP.");

    private final static Supplier<JsonFactory> JSON_FACTORY_SUPPLIER = Suppliers.memoize(() -> new JsonFactory());

    private EurostagUtil() {
    }

    public static Map<String, String> createEnv(EurostagConfig config) {
        Map<String, String> env = new HashMap<>();
        if (config.getEurostagHomeDir() != null) {
            env.put("EUROSTAG", config.getEurostagHomeDir().toString());
            env.put("PATH", config.getEurostagHomeDir().toString());
            env.put("LD_LIBRARY_PATH",  config.getEurostagHomeDir().toString());
        }
        if (config.getIndexesBinDir() != null) {
            if (env.containsKey("PATH")) {
                env.put("PATH", env.get("PATH") + ":" + config.getIndexesBinDir().toString());
            } else {
                env.put("PATH", config.getIndexesBinDir().toString());
            }
        }
        return env;
    }

    static void searchErrorMessage(Path fileGz, Map<String, String> metrics, Integer contingencyNum) throws IOException {
        if (Files.exists(fileGz)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(fileGz)), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = ERROR_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String mod = matcher.group(1);
                        String num = matcher.group(2);
                        String msg = matcher.group(3);
                        StringWriter writer = new StringWriter();
                        try (JsonGenerator generator = JSON_FACTORY_SUPPLIER.get().createGenerator(writer)) {
                            generator.writeStartObject();
                            generator.writeStringField("mod", mod);
                            generator.writeStringField("num", num);
                            generator.writeStringField("msg", msg);
                            generator.writeEndObject();
                        }
                        String json = writer.toString();
                        metrics.put("error" + (contingencyNum != null ? "_" + contingencyNum : ""), json);
                        break;
                    }
                }
            }
        }
    }

    static String searchInitialValueErrors(Path fileGz) throws IOException {
        if (Files.exists(fileGz)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(fileGz)), StandardCharsets.UTF_8))) {
                class InitialValueError {
                    String machine;
                    String macrobloc;
                    String variable;
                    String blocdesort;
                    String limiteinf;
                    String valeur;
                    String limitesup;
                }

                final List<InitialValueError> errors = new ArrayList<>();

                new BufferedLineParser(reader, 2).parse(new Consumer<List<String>>() {

                    private boolean in = false;

                    @Override
                    public void accept(List<String> lines) {
                        if (lines.equals(HEADER_INITIAL_VALUE)) {
                            in = true;
                        } else if (in && lines.get(0).trim().isEmpty()) {
                            in = false;
                        }
                        if (in) {
                            String line = lines.get(0);
                            if (line.length() > 0 && line.charAt(line.length()-1) == '*') {
                                InitialValueError error = new InitialValueError();
                                error.machine = line.substring(8, 16).trim();
                                error.macrobloc = line.substring(24, 32).trim();
                                error.variable = line.substring(36, 44).trim();
                                error.blocdesort = line.substring(46, 54).trim();
                                error.limiteinf = line.substring(68, 80).trim();
                                error.valeur = line.substring(83, 95).trim();
                                error.limitesup = line.substring(98, 110).trim();
                                errors.add(error);
                            }
                        }
                    }
                });

                if (errors.size() > 0) {
                    StringWriter writer = new StringWriter();
                    try (JsonGenerator generator = JSON_FACTORY_SUPPLIER.get().createGenerator(writer)) {
                        generator.writeStartArray();
                        for (InitialValueError error : errors) {
                            generator.writeStartObject();
                            generator.writeStringField("machine", error.machine);
                            generator.writeStringField("macrobloc", error.macrobloc);
                            generator.writeStringField("variable", error.variable);
                            generator.writeStringField("blocdesort", error.blocdesort);
                            generator.writeStringField("macrobloc", error.macrobloc);
                            generator.writeStringField("limiteinf", error.limiteinf);
                            generator.writeStringField("valeur", error.valeur);
                            generator.writeStringField("limitesup", error.limitesup);
                            generator.writeEndObject();
                        }
                        generator.writeEndArray();
                    }
                    return writer.toString();
                }
            }
        }
        return null;
    }

    static String searchSteadyStateErrors(Path fileGz) throws IOException {
        if (Files.exists(fileGz)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(fileGz)), StandardCharsets.UTF_8))) {
                class SteadyStateError {
                    String machine;
                    String macrobloc;
                    String variable;
                    String blocdesort;
                    float valeurPu;
                }

                final List<SteadyStateError> errors = new ArrayList<>();

                new BufferedLineParser(reader, 2).parse(new Consumer<List<String>>() {

                    private boolean in = false;

                    @Override
                    public void accept(List<String> lines) {
                        if (lines.equals(HEADER_STEADY_STATE)) {
                            in = true;
                        } else if (in && (lines.get(0).trim().isEmpty() || "1".equals(lines.get(0)))) {
                            in = false;
                        }
                        if (in) {
                            String line = lines.get(0);
                            if (line.length() > 0) {
                                SteadyStateError error = new SteadyStateError();
                                error.machine = line.substring(5, 13).trim();
                                error.macrobloc = line.substring(16, 24).trim();
                                error.variable = line.substring(29, 43).trim();
                                error.blocdesort = line.substring(44, 56).trim();
                                String token = line.substring(73, 84).trim();
                                error.valeurPu = token.contains("*") ? Float.NaN : Float.parseFloat(token);
                                if (Float.isNaN(error.valeurPu) || error.valeurPu > 10) {
                                    errors.add(error);
                                }
                            }
                        }
                    }
                });

                if (errors.size() > 0) {
                    Collections.sort(errors, (o1, o2) -> Float.compare(o1.valeurPu, o2.valeurPu));
                    StringWriter writer = new StringWriter();
                    try (JsonGenerator generator = JSON_FACTORY_SUPPLIER.get().createGenerator(writer)) {
                        generator.writeStartArray();
                        for (SteadyStateError error : errors) {
                            generator.writeStartObject();
                            generator.writeStringField("machine", error.machine);
                            generator.writeStringField("macrobloc", error.macrobloc);
                            generator.writeStringField("variable", error.variable);
                            generator.writeStringField("blocdesort", error.blocdesort);
                            generator.writeNumberField("valeurPu", error.valeurPu);
                            generator.writeEndObject();
                        }
                        generator.writeEndArray();
                    }
                    return writer.toString();
                }
            }
        }
        return null;
    }

    static void putBadExitCode(ExecutionReport report, Map<String, String> metrics) {
        if (report.getErrors().size() > 0) {
            ExecutionError lastError = report.getErrors().get(report.getErrors().size()-1);
            metrics.put("badExitCode", Integer.toString(lastError.getExitCode()));
        }
    }

    public static class EurostagLfStatus {
        public int iterations = -1;
        public boolean diverge = false;
    }

    public static EurostagLfStatus searchLfStatusMessages(BufferedReader reader) throws IOException {
        EurostagLfStatus status = new EurostagLfStatus();
        // keep the last one
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(DIVERGENCE_MESSAGE)) {
                status.diverge = true;
            }
            Matcher matcher = ITERATION_PATTERN.matcher(line);
            if (matcher.matches()) {
                status.iterations = Integer.parseInt(matcher.group(1));
            }
        }
        return status;
    }

    public static boolean isSteadyStateReached(Path integrationStepFile, double minStepAtEndOfStabilization) throws IOException {
        if (Files.exists(integrationStepFile)) {
            try (Stream<String> stream = Files.lines(integrationStepFile)) {
                double[] values = stream.mapToDouble(Double::parseDouble).toArray();
                if (values.length <= 1) {
                    return false;
                } else {
                    // check that next to last integration step was > minStepAtEndOfStabilization s
                    // last has to be avoid because can be truncated because of the end of simulation
                    return values[values.length - 2] >= minStepAtEndOfStabilization;
                }
            }
        } else {
            return false;
        }
    }

}
