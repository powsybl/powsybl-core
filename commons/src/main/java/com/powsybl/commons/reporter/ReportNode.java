/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A <code>Reporter</code> allows building up functional reports with a hierarchy reflecting task/subtasks of processes.
 * The enclosed reports are based on {@link ReportNode} class.
 *
 * <p>A <code>Reporter</code> can create sub-reporters to separate from current reports the reports from that task.
 * Each sub-reporter is defined by a key identifying the corresponding task, a default <code>String</code> describing
 * the corresponding task, and {@link TypedValue} values indexed by their keys. These values may be referred to
 * by their key in the description of the sub-reporter, or in its reports, using the <code>${key}</code> syntax,
 * in order to be later replaced by {@link org.apache.commons.text.StringSubstitutor} for instance when formatting
 * the string for the end user.
 *
 * <p>Instances of <code>Reporter</code> are not thread-safe.
 * When a new thread is created, a new Reporter should be provided to the process in that thread.
 * A reporter is not meant to be shared with other threads nor to be saved as a class parameter, but should instead
 * be passed on in methods through their arguments.
 *
 * <p>The <code>Reporter</code> can be used for multilingual support. Indeed, each <code>Reporter</code> name and
 * {@link ReportNode} message can be translated based on their key and using the value keys in the desired order.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNode {

    /**
     * A No-op implementation of <code>Reporter</code>
     */
    ReportNode NO_OP = new NoOpImpl();

    /**
     * Create a new child MessageNode with a message and its associated values
     * @param key the key identifying that child MessageNode
     * @param defaultMessage functional log, which may contain references to the provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the defaultMessage
     *               or within children of the created child MessageNode.
     * @return the new sub-reporter
     */
    ReportNode report(String key, String defaultMessage, Map<String, TypedValue> values);

    /**
     * Create a new child MessageNode with a message and no associated values
     * @param reporterKey the key identifying that sub-reporter
     * @param defaultMessage description of the corresponding task
     * @return the new sub-reporter
     */
    ReportNode report(String reporterKey, String defaultMessage);

    /**
     * Create a new child MessageNode with a message and one untyped associated value
     * @param reporterKey the key identifying that sub-reporter
     * @param defaultMessage description of the corresponding task, which may contain references to the provided value
     * @param valueKey the key for the value which follows
     * @param value the value which may be referred to within the defaultMessage or within the reports message of the
     *              created sub-reporter
     * @return the new sub-reporter
     */
    ReportNode report(String reporterKey, String defaultMessage, String valueKey, Object value);

    /**
     * Create a new child MessageNode with a message and one associated typed value
     * @param reporterKey the key identifying that sub-reporter
     * @param defaultMessage description of the corresponding task, which may contain references to the provided typed value
     * @param valueKey the key for the value which follows
     * @param value the value which may be referred to within the defaultMessage or within the reports message of the
     *              created sub-reporter
     * @param type the string representing the type of the value provided
     * @return the new sub-reporter
     */
    ReportNode report(String reporterKey, String defaultMessage, String valueKey, Object value, String type);

    /**
     * Add a ReportNode as a child of current ReportNode.
     * @param reportNode the ReportNode to add
     */
    void report(ReportNode reportNode);

    /**
     * Get the key of current node, each message template should have a unique key
     * @return the key
     */
    String getKey();

    /**
     * Get the default text of current node
     * @return the default text
     */
    String getDefaultText();

    /**
     * Get the values map of current node
     * @return the values map
     */
    Map<String, TypedValue> getValues();

    /**
     * Get the value corresponding to the given key in current node context
     * @param valueKey the key to request
     * @return the value
     */
    TypedValue getValue(String valueKey);

    /**
     * Get the ReportNode children of current node
     * @return the ReportNode children
     */
    Collection<ReportNode> getChildren();

    /**
     * Serialize the current report node and fills the dictionary map provided
     * @param generator the jsonGenerator to use for serialization
     * @param dictionary the dictionary to fill for the message templates
     */
    void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException;

    /**
     * Print to given writer the current report node and its lineage
     * @param writer the writer to write to
     * @param indent the indentation String to use
     * @param inheritedValueMap the inherited value map
     */
    void print(Writer writer, String indent, Deque<Map<String, TypedValue>> inheritedValueMap) throws IOException;

    /**
     * A default no-op implementation
     */
    class NoOpImpl extends AbstractReportNode {
        public NoOpImpl() {
            super("noOp", "NoOp", Collections.emptyMap());
        }

        @Override
        public ReportNode report(String key, String defaultMessage, Map<String, TypedValue> values) {
            return new NoOpImpl();
        }

        @Override
        public void report(ReportNode reportNode) {
            // No-op
        }

        @Override
        public Collection<ReportNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
            // No-op
        }

        @Override
        public void print(Writer writer, String indent, Deque<Map<String, TypedValue>> inheritedValueMap) throws IOException {
            // No-op
        }
    }
}
