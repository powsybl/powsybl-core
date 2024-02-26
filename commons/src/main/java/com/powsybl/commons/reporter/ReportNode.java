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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A <code>ReporterNode</code> allows building up functional reports with a tree hierarchy.
 *
 * <p>Each <code>ReporterNode</code> is defined by
 * <ul>
 *     <li>a {@link String} key identifying the <strong>unique</strong> corresponding functional report; this key is
 *     used to build a dictionary of all the message templates,</li>
 *     <li>a map of {@link TypedValue} indexed by their keys,</li>
 *     <li>a default {@link String} containing the functional message in the default language, which may contain
 *     references to those values or to the values of one of its ancestors.</li>
 * </ul>
 *
 * The {@link TypedValue} values may be referred to by their key using the <code>${key}</code> syntax,
 * in order to be later replaced by {@link org.apache.commons.text.StringSubstitutor} for instance when formatting
 * the string for the end user.
 *
 * <p>Instances of <code>ReporterNode</code> are not thread-safe.
 * When a new thread is created, a new <code>ReporterNode</code> should be provided to the process in that thread.
 * A <code>ReporterNode</code> is not meant to be shared with other threads.
 * Therefore, it should not be saved as a class parameter of an object which could be used by separate threads.
 * In those cases it should instead be passed on in methods through their arguments.
 *
 * <p>The <code>ReporterNode</code> is designed for multilingual support. Indeed, each {@link ReportNode} message can be translated based on their key and using the value keys in the desired order.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNode {

    /**
     * A No-op implementation of <code>ReporterNode</code>
     */
    ReportNode NO_OP = new NoOpImpl();

    /**
     * Create a new child <code>ReporterNode</code> with a default message template and its associated values
     *
     * @param key             the key identifying the message corresponding to the <code>ReporterNode</code> to create
     * @param messageTemplate functional log, which may contain references to values using the <code>${key}</code> syntax,
     *                        the values mentioned being the provided values and the values of any
     *                        <code>ReporterNode</code> ancestor of the created <code>ReporterNode</code>
     * @param values          a map of {@link TypedValue} indexed by their key, which may be referred to within the messageTemplate
     *                        or within any descendants of the created <code>ReporterNode</code>.
     *                        Be aware that any value in this map might, in all descendants, override a value of one of
     *                        <code>ReporterNode</code> ancestors.
     * @return the created <code>ReporterNode</code>
     */
    ReportNode report(String key, String messageTemplate, Map<String, TypedValue> values);

    /**
     * Create a new child <code>ReporterNode</code> with a default message template and no associated values
     *
     * @param key             the key identifying the message corresponding to the <code>ReporterNode</code> to create
     * @param messageTemplate functional log, which may contain references to values of any <code>ReporterNode</code>
     *                        ancestor of the created <code>ReporterNode</code>, using the <code>${key}</code> syntax
     * @return the created <code>ReporterNode</code>
     */
    ReportNode report(String key, String messageTemplate);

    /**
     * Create a new child <code>ReporterNode</code> with a default message template and one untyped associated value
     *
     * @param key             the key identifying the message corresponding to the <code>ReporterNode</code> to create
     * @param messageTemplate functional log, which may contain references to the given value or to values of any
     *                        <code>ReporterNode</code> ancestor of the created <code>ReporterNode</code>, using the
     *                        <code>${key}</code> syntax
     * @param valueKey        the key for the value which follows
     * @param value           the value which may be referred to within the messageTemplate or within any descendants of
     *                        the created <code>ReporterNode</code>.
     *                        Be aware that this value might, in all descendants, override a value of one of <code>ReporterNode</code>
     *                        ancestors.
     * @return the created <code>ReporterNode</code>
     */
    ReportNode report(String key, String messageTemplate, String valueKey, Object value);

    /**
     * Create a new child <code>ReporterNode</code> with a default message template and one associated typed value
     *
     * @param key             the key identifying the message corresponding to the <code>ReporterNode</code> to create
     * @param messageTemplate functional log, which may contain references to the given value or to values of any
     *                        <code>ReporterNode</code> ancestor of the created <code>ReporterNode</code>, using the
     *                        <code>${key}</code> syntax
     * @param valueKey        the key for the value which follows
     * @param value           the value which may be referred to within the messageTemplate or within the reports message of the
     *                        created sub-reporter
     * @param type            the string representing the type of the value provided
     * @return the created <code>ReporterNode</code>
     */
    ReportNode report(String key, String messageTemplate, String valueKey, Object value, String type);

    /**
     * Add a ReportNode as a child of current ReportNode.
     * @param reportNode the ReportNode to add
     */
    void addChild(ReportNode reportNode);

    /**
     * Get the key of current node.
     * Note that each key needs to correspond to a unique message template
     * This is required in serialization, in particular due to multilingual support.
     * @return the key
     */
    String getKey();

    /**
     * Get the message of current node, replacing references with the corresponding values
     * @return the default text
     */
    String getMessage();

    /**
     * Get the {@link Deque} of values maps for current node
     * @return the {@link Deque} values map
     */
    Deque<Map<String, TypedValue>> getValuesDeque();

    /**
     * Get the value corresponding to the given key in current node context
     *
     * @param valueKey the key to request
     * @return the value
     */
    Optional<TypedValue> getValue(String valueKey);

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
     * Print to given path the current report node and its descendants
     * @param path the writer to write to
     */
    default void print(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            print(writer);
        }
    }

    /**
     * Print to given writer the current report node and its descendants
     * @param writer the writer to write to
     */
    default void print(Writer writer) throws IOException {
        print(writer, "");
    }

    /**
     * Print to given writer the current report node and its descendants
     * @param writer the writer to write to
     * @param indentationStart the string indentation to use for current node
     */
    void print(Writer writer, String indentationStart) throws IOException;

    /**
     * A default no-op implementation
     */
    class NoOpImpl implements ReportNode {

        private static final Deque<Map<String, TypedValue>> EMPTY_MAP_DEQUE = new ArrayDeque<>();

        @Override
        public ReportNode report(String key, String messageTemplate, Map<String, TypedValue> values) {
            return new NoOpImpl();
        }

        @Override
        public ReportNode report(String key, String messageTemplate) {
            return new NoOpImpl();
        }

        @Override
        public ReportNode report(String key, String messageTemplate, String valueKey, Object value) {
            return new NoOpImpl();
        }

        @Override
        public ReportNode report(String key, String messageTemplate, String valueKey, Object value, String type) {
            return new NoOpImpl();
        }

        @Override
        public void addChild(ReportNode reportNode) {
            // No-op
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public Deque<Map<String, TypedValue>> getValuesDeque() {
            return EMPTY_MAP_DEQUE;
        }

        @Override
        public Optional<TypedValue> getValue(String valueKey) {
            return Optional.empty();
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
        public void print(Writer writer) throws IOException {
            // No-op
        }

        @Override
        public void print(Writer writer, String indentationStart) throws IOException {
            // No-op
        }
    }
}
