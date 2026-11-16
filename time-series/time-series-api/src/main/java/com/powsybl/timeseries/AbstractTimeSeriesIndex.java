/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractTimeSeriesIndex implements TimeSeriesIndex {

    /**
     * @deprecated Replaced by {@link #getInstantAt(int)}}
     */
    @Deprecated(since = "6.7.0")
    @Override
    public long getTimeAt(int point) {
        return getInstantAt(point).toEpochMilli();
    }

    @Override
    public String toJson() {
        return toJson(ExportFormat.MILLISECONDS);
    }

    @Override
    public String toJson(ExportFormat format) {
        return JsonUtil.toJson(generator -> this.writeJson(generator, format));
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        writeJson(generator, ExportFormat.MILLISECONDS);
    }
}
