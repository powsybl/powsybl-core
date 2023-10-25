/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An in-memory implementation of {@link Reporter}.
 *
 * <p>Being an implementation of {@link Reporter}, instances of <code>ReporterModel</code> are not thread-safe.
 * A reporterModel is not meant to be shared with other threads nor to be saved as a class parameter, but should instead
 * be passed on in methods through their arguments. Respecting this ensures that
 * <ol>
 *   <li>sub-reporters always are in the same order</li>
 *   <li>reports always are in the same order</li>
 * </ol>
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReporterModel extends AbstractReporter {

    private final List<ReporterModel> subReporters = new ArrayList<>();
    private final List<Report> reports = new ArrayList<>();

    /**
     * ReporterModel constructor, with no associated values.
     * @param taskKey the key identifying the corresponding task
     * @param defaultName the name or message describing the corresponding task
     */
    public ReporterModel(String taskKey, String defaultName) {
        this(taskKey, defaultName, Collections.emptyMap());
    }

    /**
     * ReporterModel constructor, with no associated values.
     * @param taskKey the key identifying the corresponding task
     * @param defaultName the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param taskValues a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultName or within the reports message of created ReporterModel
     */
    public ReporterModel(String taskKey, String defaultName, Map<String, TypedValue> taskValues) {
        super(taskKey, defaultName, taskValues);
    }

    @Override
    public ReporterModel createSubReporter(String taskKey, String defaultName, Map<String, TypedValue> values) {
        ReporterModel subReporter = new ReporterModel(taskKey, defaultName, values);
        addSubReporter(subReporter);
        return subReporter;
    }

    /**
     * Add a reporterModel to the sub-reporters of current reporterModel.
     * @param reporterModel the reporterModel to add
     */
    public void addSubReporter(ReporterModel reporterModel) {
        subReporters.add(reporterModel);
    }

    @Override
    public void report(Report report) {
        reports.add(report);
    }

    public Collection<Report> getReports() {
        return Collections.unmodifiableCollection(reports);
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public Map<String, TypedValue> getTaskValues() {
        return Collections.unmodifiableMap(taskValues);
    }

    public List<ReporterModel> getSubReporters() {
        return Collections.unmodifiableList(subReporters);
    }

    public void export(Path path) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            export(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void export(Writer writer) {
        try {
            printTaskReport(this, writer, "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printTaskReport(ReporterModel reportTree, Writer writer, String prefix) throws IOException {
        writer.append(prefix).append("+ ").append(formatMessage(reportTree.getDefaultName(), reportTree.getTaskValues())).append(System.lineSeparator());
        for (Report report : reportTree.getReports()) {
            writer.append(prefix).append("   ").append(formatReportMessage(report, reportTree.getTaskValues())).append(System.lineSeparator());
        }
        for (ReporterModel subReporter : reportTree.getSubReporters()) {
            printTaskReport(subReporter, writer, prefix + "  ");
        }
    }

    public static ReporterModel parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode taskKeyNode = reportTree.get("taskKey");
        String taskKey = codec.readValue(taskKeyNode.traverse(), String.class);

        JsonNode taskValuesNode = reportTree.get("taskValues");
        Map<String, TypedValue> taskValues = taskValuesNode == null ? Collections.emptyMap() : codec.readValue(taskValuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultName = dictionary.getOrDefault(taskKey, "(missing task key in dictionary)");
        ReporterModel reporter = new ReporterModel(taskKey, defaultName, taskValues);

        JsonNode reportsNode = reportTree.get("reports");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reporter.reports.add(Report.parseJsonNode(jsonNode, dictionary, codec));
            }
        }

        JsonNode subReportersNode = reportTree.get("subReporters");
        if (subReportersNode != null) {
            for (JsonNode jsonNode : subReportersNode) {
                reporter.addSubReporter(ReporterModel.parseJsonNode(jsonNode, dictionary, codec));
            }
        }

        return reporter;
    }
}
