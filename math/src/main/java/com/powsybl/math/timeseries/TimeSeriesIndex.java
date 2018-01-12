/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TimeSeriesIndex {

    static int checkVersion(int version) {
        if (version < 0) {
            throw new IllegalArgumentException("Bad version " + version);
        }
        return version;
    }

    int getPointCount();

    long getTimeAt(int point);

    String getType();

    void writeJson(JsonGenerator generator);
}
