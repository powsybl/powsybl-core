/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

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
    private final Map<String, TypedValue> values;

    protected AbstractReportNode(String key, String defaultText, Map<String, TypedValue> values) {
        this.key = Objects.requireNonNull(key);
        this.defaultText = defaultText;
        this.values = new HashMap<>();
        Objects.requireNonNull(values).forEach(this::addValue);
    }

    @Override
    public ReportNode report(String key, String defaultMessage) {
        return report(key, defaultMessage, Collections.emptyMap());
    }

    @Override
    public ReportNode report(String reporterKey, String defaultMessage, String valueKey, Object value) {
        return this.report(reporterKey, defaultMessage, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNode report(String reporterKey, String defaultMessage, String valueKey, Object value, String type) {
        return report(reporterKey, defaultMessage, Map.of(valueKey, new TypedValue(value, type)));
    }

    private void addValue(String key, TypedValue typedValue) {
        Objects.requireNonNull(typedValue);
        values.put(key, typedValue);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultText() {
        return defaultText;
    }

    @Override
    public Map<String, TypedValue> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public TypedValue getValue(String valueKey) {
        return values.get(valueKey);
    }

    protected void printDefaultText(Writer writer, String indent, String prefix, Deque<Map<String, TypedValue>> valueMaps) throws IOException {
        String formattedText = formatMessage(getDefaultText(), valueMaps);
        writer.append(indent).append(prefix).append(formattedText).append(System.lineSeparator());
    }

    /**
     * Format given message by replacing value references by the corresponding values.
     * The values in the given message have to be referred to with their corresponding key, using the <code>${key}</code> syntax.
     * {@link org.apache.commons.text.StringSubstitutor} is used for the string replacements.
     * @param message the message to be formatted
     * @param values the key-value map used to look for the values
     * @return the resulting formatted string
     */
    protected static String formatMessage(String message, Deque<Map<String, TypedValue>> values) {
        return new StringSubstitutor(new MapsLookup(values)).replace(message);
    }

    private record MapsLookup(Deque<Map<String, TypedValue>> values) implements StringLookup {
        @Override
        public String lookup(String s) {
            return values.stream()
                    .map(m -> m.get(s))
                    .filter(Objects::nonNull)
                    .map(TypedValue::getValue)
                    .map(Object::toString)
                    .findFirst().orElse(null);
        }
    }
}
