/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.ast.NodeCalc;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface CalculatedTimeSeriesDslLoader {

    Map<String, NodeCalc> load(String script, ReadOnlyTimeSeriesStore store);

    static CalculatedTimeSeriesDslLoader find() {
        List<CalculatedTimeSeriesDslLoader> loaders = ServiceLoader.load(CalculatedTimeSeriesDslLoader.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        if (loaders.isEmpty()) {
            throw new PowsyblException("No calculated time series DSL loader found");
        } else if (loaders.size() > 1) {
            throw new PowsyblException("Several calculated time series DSL loaders found, only one is authorized");
        }
        return loaders.get(0);
    }
}
