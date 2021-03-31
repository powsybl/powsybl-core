/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TreeReporter extends AbstractReporter {

    private final List<TreeReporter> childReporters = new ArrayList<>();
    private final List<Report> reports = new ArrayList<>();

    public TreeReporter(String taskKey, String defaultName) {
        this(taskKey, defaultName, Collections.emptyMap());
    }

    public TreeReporter(String taskKey, String defaultName, Map<String, TypedValue> taskValues) {
        super(taskKey, defaultName, taskValues);
    }

    @Override
    public TreeReporter createChild(String taskKey, String defaultName, Map<String, TypedValue> values) {
        TreeReporter childReporter = new TreeReporter(taskKey, defaultName, values);
        childReporters.add(childReporter);
        return childReporter;
    }

    public void addChild(TreeReporter treeReporter) {
        childReporters.add(treeReporter);
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

    public List<TreeReporter> getChildReporters() {
        return Collections.unmodifiableList(childReporters);
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

    private void printTaskReport(TreeReporter reportTree, Writer writer, String prefix) throws IOException {
        writer.append(prefix).append("+ ").append(formatMessage(reportTree.getDefaultName(), reportTree.getTaskValues())).append(System.lineSeparator());
        for (Report report : reportTree.getReports()) {
            writer.append(prefix).append("   ").append(formatReportMessage(report, reportTree.getTaskValues())).append(System.lineSeparator());
        }
        for (TreeReporter childReporter : reportTree.getChildReporters()) {
            printTaskReport(childReporter, writer, prefix + "  ");
        }
    }

    public static TreeReporter parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectMapper mapper) {
        String taskKey = mapper.convertValue(reportTree.get("taskKey"), String.class);

        JsonNode taskValuesNode = reportTree.get("taskValues");
        Map<String, TypedValue> taskValues = taskValuesNode == null ? Collections.emptyMap() : mapper.convertValue(taskValuesNode, new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultName = dictionary.getOrDefault(taskKey, "(missing task key in dictionary)");
        TreeReporter reporter = new TreeReporter(taskKey, defaultName, taskValues);

        JsonNode reportsNode = reportTree.get("reports");
        if (reportsNode != null) {
            reportsNode.forEach(jsonNode -> reporter.reports.add(Report.parseJsonNode(jsonNode, dictionary, mapper)));
        }

        JsonNode childReportersNode = reportTree.get("childReporters");
        if (childReportersNode != null) {
            childReportersNode.forEach(jsonNode -> reporter.childReporters.add(TreeReporter.parseJsonNode(jsonNode, dictionary, mapper)));
        }

        return reporter;
    }
}
