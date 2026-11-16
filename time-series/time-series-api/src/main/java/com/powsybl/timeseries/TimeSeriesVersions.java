/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class TimeSeriesVersions {

    private TimeSeriesVersions() {
    }

    /**
     * Check if the version number is allowed.
     * <p>All positive integers are allowed, including 0.</p>
     * <p>Every strictly negative integers are forbidden, except -1 (default version number for not-versioned
     * TimeSeries).</p>
     * @param version version number
     * @return the version number if it is allowed, else an exception is thrown
     */
    public static int check(int version) {
        if (version < TimeSeries.DEFAULT_VERSION_NUMBER_FOR_UNVERSIONED_TIMESERIES) {
            throw new IllegalArgumentException("Bad version " + version);
        }
        return version;
    }
}
