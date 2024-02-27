/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * An abstract class providing the default method implementations related to key / messageTemplate, for {@link ReportNode} implementations.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReportNode implements ReportNode {

    private final String key;
    private final String messageTemplate;

    protected AbstractReportNode(String key, String messageTemplate) {
        this.key = Objects.requireNonNull(key);
        this.messageTemplate = messageTemplate;
    }

    @Override
    public ReportNode report(String key, String messageTemplate) {
        return report(key, messageTemplate, Collections.emptyMap());
    }

    @Override
    public ReportNode report(String key, String messageTemplate, String valueKey, Object value) {
        return this.report(key, messageTemplate, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNode report(String key, String messageTemplate, String valueKey, Object value, String type) {
        return report(key, messageTemplate, Map.of(valueKey, new TypedValue(value, type)));
    }

    @Override
    public String getKey() {
        return key;
    }

    protected String getMessageTemplate() {
        return messageTemplate;
    }

    @Override
    public String getMessage() {
        return new StringSubstitutor(vk -> getValueAsString(vk).orElse(null)).replace(messageTemplate);
    }

    public Optional<String> getValueAsString(String valueKey) {
        return getValue(valueKey).map(TypedValue::getValue).map(Object::toString);
    }

    @Override
    public void print(Writer writer, String indentationStart) throws IOException {
        Collection<ReportNode> children = getChildren();
        if (children.isEmpty()) {
            print(writer, indentationStart, "");
        } else {
            print(writer, indentationStart, "+ ");
            String childrenIndent = indentationStart + "   ";
            for (ReportNode child : children) {
                child.print(writer, childrenIndent);
            }
        }
    }

    protected void print(Writer writer, String indent, String prefix) throws IOException {
        writer.append(indent).append(prefix).append(getMessage()).append(System.lineSeparator());
    }
}
