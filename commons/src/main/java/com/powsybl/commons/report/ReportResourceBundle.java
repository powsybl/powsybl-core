/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

/**
 * Interface used to load all the report ResourceBundles which are in the classpath.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportResourceBundle {
    String getBaseName();
}
