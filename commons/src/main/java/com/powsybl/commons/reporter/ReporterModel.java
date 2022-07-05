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
    private final List<ReportMessage> reportMessages = new ArrayList<>();

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultTitle the name or message describing the corresponding task
     */
    public ReporterModel(String key, String defaultTitle) {
        this(key, defaultTitle, Collections.emptyMap());
    }

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultTitle the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultTitle or within the reports message of created ReporterModel
     */
    public ReporterModel(String key, String defaultTitle, Map<String, TypedValue> values) {
        super(key, defaultTitle, values);
    }

    @Override
    public ReporterModel createSubReporter(String reporterKey, String defaultTitle, Map<String, TypedValue> values) {
        ReporterModel subReporter = new ReporterModel(reporterKey, defaultTitle, values);
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
    public void report(ReportMessage reportMessage) {
        reportMessages.add(reportMessage);
    }

    public Collection<ReportMessage> getReportMessages() {
        return Collections.unmodifiableCollection(reportMessages);
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public String getKey() {
        return key;
    }

    public Map<String, TypedValue> getValues() {
        return Collections.unmodifiableMap(values);
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
        writer.append(prefix).append("+ ").append(formatMessage(reportTree.getDefaultTitle(), reportTree.getValues())).append(System.lineSeparator());
        for (ReportMessage reportMessage : reportTree.getReportMessages()) {
            writer.append(prefix).append("   ").append(formatReportMessage(reportMessage, reportTree.getValues())).append(System.lineSeparator());
        }
        for (ReporterModel subReporter : reportTree.getSubReporters()) {
            printTaskReport(subReporter, writer, prefix + "  ");
        }
    }

    public static ReporterModel parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode keyNode = reportTree.get("key");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = reportTree.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultTitle = dictionary.getOrDefault(key, "(missing task key in dictionary)");
        ReporterModel reporter = new ReporterModel(key, defaultTitle, values);

        JsonNode reportsNode = reportTree.get("reportMessages");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reporter.reportMessages.add(ReportMessage.parseJsonNode(jsonNode, dictionary, codec));
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
