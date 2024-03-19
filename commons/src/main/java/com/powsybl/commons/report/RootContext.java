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

import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class RootContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RootContext.class);
    private final SortedMap<String, String> dictionary = new TreeMap<>();

    public Map<String, String> getDictionary() {
        return Collections.unmodifiableMap(dictionary);
    }

    public synchronized void addDictionaryEntry(String key, String messageTemplate) {
        dictionary.merge(key, messageTemplate, (prevMsg, newMsg) -> mergeEntries(key, prevMsg, newMsg));
    }

    private static String mergeEntries(String key, String previousMessageTemplate, String newMessageTemplate) {
        if (!previousMessageTemplate.equals(newMessageTemplate)) {
            LOGGER.warn("Same key {} for two non-equal message templates: '{}' / '{}'", key, previousMessageTemplate, newMessageTemplate);
        }
        return newMessageTemplate;
    }

    public synchronized void merge(RootContext otherContext) {
        dictionary.putAll(otherContext.dictionary);
    }
}
