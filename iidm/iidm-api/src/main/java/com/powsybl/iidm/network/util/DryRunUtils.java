/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class DryRunUtils {
    private DryRunUtils() {
    }

    public static void assertNotDryRun(boolean dryRun) {
        if (dryRun) {
            throw new UnsupportedDryRunException("Local dry-run is not supported by this method.");
        }
    }
}
