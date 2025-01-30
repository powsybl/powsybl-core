/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A <code>ReportNode</code> allows building up functional reports with a tree hierarchy.
 *
 * <p>Each <code>ReportNode</code> is defined by
 * <ul>
 *     <li>a map of {@link TypedValue} indexed by their keys,</li>
 *     <li>a default {@link String} message template containing the functional message in the default language, which
 *     may contain references to those values or to the values of one of its ancestors,</li>
 *     <li>a {@link String} message key identifying the <strong>unique</strong> corresponding functional report template;
 *     this key is used in particular to build a dictionary of all the message templates,</li>
 *     <li>a collection of <code>ReportNode</code> children, possibly empty.</li>
 * </ul>
 *
 * The values {@link TypedValue} values should be referred to by their key in the message template, using the <code>${key}</code>
 * syntax, in order to be later replaced by {@link org.apache.commons.text.StringSubstitutor} for instance when formatting
 * the string for the end user.
 * The <code>ReportNode</code> values may be referred to within the corresponding messageTemplate, or within any of its
 * descendants. Be aware that any descendant might override a value by giving a new value to an existing key.
 * All implementations of <code>ReportNode</code> need to take that inheritance into account.
 *
 * <p>Instances of <code>ReportNode</code> are not thread-safe.
 * When a new thread is created, a new <code>ReportNode</code> should be provided to the process in that thread.
 * A <code>ReportNode</code> is not meant to be shared with other threads.
 * Therefore, it should rather not be saved as a class parameter of an object which could be used by separate threads.
 * In those cases it should instead be passed on in methods through their arguments.
 *
 * <p>The <code>ReportNode</code> is designed for multilingual support.
 * Indeed, each <code>ReportNode</code> message can be translated based on their key and using the value keys in the desired order.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNode {

    /**
     * A No-op implementation of <code>ReportNode</code>
     */
    ReportNode NO_OP = new ReportNodeNoOp();

    /**
     * Create a new builder for creating a root <code>ReportNode</code>.
     * @return a {@link ReportNodeBuilder}
     */
    static ReportNodeBuilder newRootReportNode() {
        return new ReportNodeRootBuilderImpl();
    }

    /**
     * Get the message key for current node.
     * Note that each key needs to correspond to a unique message template.
     * This allows to build up a dictionary of message templates indexed by their key.
     * @return the key
     */
    String getMessageKey();

    /**
     * Get the message template for current node.
     * @return the message template
     */
    String getMessageTemplate();

    /**
     * Get the message of current node, replacing <code>${key}</code> references in the message template with the
     * corresponding values, either contained in current node or in one of its parents.
     * @return the message
     */
    String getMessage();

    /**
     * Get the values which belong to current node (does not include the inherited values)
     * @return the values unmodifiable map
     */
    Map<String, TypedValue> getValues();

    /**
     * Get the value corresponding to the given key
     *
     * @param valueKey the key to request
     * @return the value
     */
    Optional<TypedValue> getValue(String valueKey);

    /**
     * Get the children of current node
     *
     * @return the children nodes
     */
    List<ReportNode> getChildren();

    /**
     * Create a new adder to create a <code>ReportNode</code> child.
     *
     * @return the created <code>ReportNodeAdder</code>
     */
    ReportNodeAdder newReportNode();

    /**
     * Get the {@link TreeContext} of the corresponding {@link ReportNode} tree.
     */
    TreeContext getTreeContext();

    /**
     * Add a <code>ReportNode</code> as a child of current <code>ReportNode</code>.
     *
     * @param reportRoot the <code>ReportNode</code> to add, it needs to be a root <code>ReportNode</code>
     */
    void include(ReportNode reportRoot);

    /**
     * Copy the given <code>ReportNode</code> and inserts the resulting <code>ReportNode</code> as a child of current <code>ReportNode</code>.
     *
     * @param reportNode the <code>ReportNode</code> to copy into the children of current <code>ReportNode</code>
     */
    void copy(ReportNode reportNode);

    /**
     * Serialize the current report node
     * @param generator the jsonGenerator to use for serialization
     */
    void writeJson(JsonGenerator generator) throws IOException;

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
    void print(Writer writer) throws IOException;
}
