/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.commons.json.JsonUtil;

import java.time.Instant;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTimeSeriesIndex implements TimeSeriesIndex {

    @Override
    public Instant getInstantAt(int point) {
        return Instant.ofEpochMilli(getTimeAt(point));
    }

    @Override
    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }
}
