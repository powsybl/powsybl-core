/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class ReportConstants {

    public static final String SEVERITY_KEY = "reportSeverity";
    public static final String TIMESTAMP_KEY = "reportTimestamp";
    public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final Locale DEFAULT_TIMESTAMP_LOCALE = Locale.US;
    public static final DateTimeFormatter DEFAULT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN, DEFAULT_TIMESTAMP_LOCALE);
    public static final ReportNodeVersion CURRENT_VERSION = ReportNodeVersion.V_2_1;

    private ReportConstants() {
    }
}
