/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvMetricsDb implements MetricsDb {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvMetricsDb.class);

    private static final char CSV_SEPARATOR = ';';

    private final Path dbDir;

    private final boolean flush;

    private final Map<String, BufferedWriter> writers = new HashMap<>();

    public static CsvMetricsDb load(String dbName) {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("csv-metrics-db");
        Path dir = config.getPathProperty("directory");
        boolean flush = config.getBooleanProperty("flush", false);
        try {
            return new CsvMetricsDb(dir, flush, dbName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CsvMetricsDb(Path dir, boolean flush, String dbName) throws IOException {
        dbDir = dir.resolve(dbName);
        this.flush = flush;
        Files.createDirectories(dbDir);
    }

    private Path toMetricsCsvFile(String workflowId) {
        return dbDir.resolve(workflowId + "-metrics.csv");
    }

    @Override
    public synchronized void create(String workflowId) {
    }

    @Override
    public synchronized void remove(String workflowId) {
        Objects.requireNonNull(workflowId);
        try {
            Files.deleteIfExists(toMetricsCsvFile(workflowId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void store(String workflowId, String target, String moduleName, Map<String, String> metrics) {
        Objects.requireNonNull(workflowId);
        Objects.requireNonNull(target);
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(metrics);
        try {
            BufferedWriter writer = writers.get(workflowId);
            if (writer == null) {
                writer = Files.newBufferedWriter(toMetricsCsvFile(workflowId), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                writers.put(workflowId, writer);
            }
            for (Map.Entry<String, String> metric : metrics.entrySet()) {
                writer.write(target);
                writer.write(CSV_SEPARATOR);
                writer.write(moduleName);
                writer.write(CSV_SEPARATOR);
                writer.write(metric.getKey());
                writer.write(CSV_SEPARATOR);
                writer.write(metric.getValue());
                writer.newLine();
            }
            if (flush) {
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flush(String workflowId) throws IOException {
        BufferedWriter writer = writers.remove(workflowId);
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public synchronized void exportCsv(String workflowId, Writer writer, char delimiter) {
        Objects.requireNonNull(workflowId);
        Objects.requireNonNull(writer);
        try {
            flush(workflowId);

            Table<String, String, String> table = HashBasedTable.create();
            try (BufferedReader metricsReader = Files.newBufferedReader(toMetricsCsvFile(workflowId), StandardCharsets.UTF_8)) {
                String line;
                while ((line = metricsReader.readLine()) != null) {
                    String[] tokens = line.split(Character.toString(CSV_SEPARATOR));
                    if (tokens.length != 4) {
                        LOGGER.warn("Invalid line '{}'", line);
                        continue;
                    }
                    String target = tokens[0];
                    String moduleName = tokens[1];
                    String metricName = tokens[2];
                    String metricValue = tokens[3];
                    table.put(target, (moduleName.length() > 0 ? moduleName + ":" : "") + metricName, metricValue);
                }
            }
            writer.write("target");
            writer.write(delimiter);
            List<String> columnKeys = new ArrayList<>(new TreeSet<>(table.columnKeySet()));
            for (String columnKey : columnKeys) {
                writer.write(columnKey);
                writer.write(delimiter);
            }
            writer.write("\n");
            for (Map.Entry<String, Map<String, String>> entry : table.rowMap().entrySet()) {
                String target = entry.getKey();
                Map<String, String> metrics = entry.getValue();
                writer.write(target);
                writer.write(delimiter);
                for (String columnKey : columnKeys) {
                    String value = metrics.get(columnKey);
                    if (value != null) {
                        writer.write(value);
                    }
                    writer.write(delimiter);
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        for (BufferedWriter writer : writers.values()) {
            writer.close();
        }
    }
}
