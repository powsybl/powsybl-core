/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TreeContextImpl implements TreeContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeContextImpl.class);
    private final SortedMap<String, String> dictionary = new TreeMap<>();
    private final Locale locale;
    private final DateTimeFormatter timestampFormatter;

    public TreeContextImpl() {
        this(ReportConstants.DEFAULT_LOCALE, ReportConstants.DEFAULT_TIMESTAMP_PATTERN);
    }

    public TreeContextImpl(Locale locale, String timestampPattern) {
        this.locale = Objects.requireNonNullElse(locale, ReportConstants.DEFAULT_LOCALE);
        this.timestampFormatter = createDateTimeFormatter(timestampPattern, locale);
    }

    private static DateTimeFormatter createDateTimeFormatter(String timestampPattern, Locale locale) {
        if (timestampPattern == null && locale == null) {
            return ReportConstants.DEFAULT_TIMESTAMP_FORMATTER;
        }
        if (timestampPattern == null) {
            return DateTimeFormatter.ofPattern(ReportConstants.DEFAULT_TIMESTAMP_PATTERN, locale);
        }
        if (locale == null) {
            return DateTimeFormatter.ofPattern(timestampPattern, ReportConstants.DEFAULT_LOCALE);
        }
        return DateTimeFormatter.ofPattern(timestampPattern, locale);
    }

    @Override
    public Map<String, String> getDictionary() {
        return Collections.unmodifiableMap(dictionary);
    }

    @Override
    public DateTimeFormatter getDefaultTimestampFormatter() {
        return timestampFormatter;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public synchronized void merge(TreeContext otherContext) {
        otherContext.getDictionary().forEach(this::addDictionaryEntry);
    }

    public synchronized void addDictionaryEntry(String key, String messageTemplate) {
        dictionary.merge(key, messageTemplate, (prevMsg, newMsg) -> mergeEntries(key, prevMsg, newMsg));
    }

    private static String mergeEntries(String key, String previousMessageTemplate, String newMessageTemplate) {
        if (!previousMessageTemplate.equals(newMessageTemplate)) {
            LOGGER.warn("Same key {} for two non-equal message templates: '{}' / '{}'. Keeping the first one.", key, previousMessageTemplate, newMessageTemplate);
        }
        return previousMessageTemplate;
    }
}
