/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation;

import java.util.Map;
import java.util.Objects;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultImpl implements DynamicSimulationResult {

    private final boolean status;
    private final Map<String, String> metrics;
    private final String logs;

    public DynamicSimulationResultImpl(boolean status, Map<String, String> metrics, String logs) {
        this.status = status;
        this.metrics = Objects.requireNonNull(metrics);
        this.logs = logs;
    }

    @Override
    public boolean isOk() {
        return status;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public String getLogs() {
        return logs;
    }

}
