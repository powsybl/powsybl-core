/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.test;

import java.io.InputStream;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at gmail.com>}
 */
public enum ComparisonType {

    XML(ComparisonUtils::assertXmlEquals),
    TEXT(ComparisonUtils::assertTxtEquals);

    public static final String PROPERTY = "powsybl.test.comparison_type";

    public static final String ENV_VAR = "POWSYBL_TEST_COMPARISON_TYPE";

    static final ComparisonType FALLBACK = XML;

    private final BiConsumer<InputStream, InputStream> comparator;

    ComparisonType(BiConsumer<InputStream, InputStream> comparator) {
        this.comparator = comparator;
    }

    public BiConsumer<InputStream, InputStream> getComparator() {
        return comparator;
    }

    public static ComparisonType getDefault() {
        String raw = System.getProperty(PROPERTY);
        if (raw == null || raw.isBlank()) {
            raw = System.getenv(ENV_VAR);
        }
        if (raw == null || raw.isBlank()) {
            return FALLBACK; // XML by default
        }
        return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

}
