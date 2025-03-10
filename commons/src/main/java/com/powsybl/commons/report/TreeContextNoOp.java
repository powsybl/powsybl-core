/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TreeContextNoOp implements TreeContext {

    @Override
    public Map<String, String> getDictionary() {
        return Map.of();
    }

    @Override
    public DateTimeFormatter getDefaultTimestampFormatter() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void merge(TreeContext treeContext) {
        // no-op
    }
}
