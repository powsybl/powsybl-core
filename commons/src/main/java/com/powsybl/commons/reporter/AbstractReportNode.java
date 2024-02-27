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
 * An abstract class providing some default method implementations for {@link ReportNode} implementations.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReportNode implements ReportNode {

    private final String key;
    private final String defaultText;
    private final Deque<Map<String, TypedValue>> valuesDeque;

    protected AbstractReportNode(String key, String defaultText, Map<String, TypedValue> values, Deque<Map<String, TypedValue>> inheritedValuesDeque) {
        this.key = Objects.requireNonNull(key);
        this.defaultText = defaultText;
        this.valuesDeque = new ArrayDeque<>(inheritedValuesDeque);
        this.valuesDeque.addFirst(new HashMap<>());
        Objects.requireNonNull(values).forEach(this::addValue);
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

    private void addValue(String key, TypedValue typedValue) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(typedValue);
        valuesDeque.getFirst().put(key, typedValue);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getMessage() {
        return defaultText;
    }

    @Override
    public Deque<Map<String, TypedValue>> getValuesDeque() {
        return valuesDeque;
    }

    @Override
    public Optional<TypedValue> getValue(String valueKey) {
        return getValuesDeque().stream()
                .map(m -> m.get(valueKey))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public Optional<String> getValueAsString(String valueKey) {
        return getValue(valueKey).map(TypedValue::getValue).map(Object::toString);
    }

    protected void print(Writer writer, String indent, String prefix) throws IOException {
        String formattedText = formatMessage(getMessage());
        writer.append(indent).append(prefix).append(formattedText).append(System.lineSeparator());
    }

    /**
     * Format given message by replacing value references by the corresponding values.
     * The values in the given message have to be referred to with their corresponding key, using the <code>${key}</code> syntax.
     * {@link org.apache.commons.text.StringSubstitutor} is used for the string replacements.
     *
     * @param message the message to be formatted
     * @return the resulting formatted string
     */
    protected String formatMessage(String message) {
        return new StringSubstitutor(vk -> getValueAsString(vk).orElse(null)).replace(message);
    }
}
