/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.ast.NodeCalc;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface CalculatedTimeSeriesDslLoader {

    Map<String, NodeCalc> load(String script, ReadOnlyTimeSeriesStore store);

    static CalculatedTimeSeriesDslLoader find() {
        return ServiceLoader.load(CalculatedTimeSeriesDslLoader.class).stream()
                .findFirst()
                .orElseThrow(() -> new PowsyblException("No calculated time series DSL loader found"))
                .get();
    }
}
