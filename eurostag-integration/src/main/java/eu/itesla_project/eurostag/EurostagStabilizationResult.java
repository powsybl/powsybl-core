/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import eu.itesla_project.modules.simulation.SimulationState;
import eu.itesla_project.modules.simulation.StabilizationResult;
import eu.itesla_project.modules.simulation.StabilizationStatus;

import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class EurostagStabilizationResult implements StabilizationResult {

    private final StabilizationStatus status;

    private final Map<String, String> metrics;

    private final EurostagState state;

    EurostagStabilizationResult(StabilizationStatus status, Map<String, String> metrics, EurostagState state) {
        this.status = status;
        this.metrics = metrics;
        this.state = state;
    }

    @Override
    public StabilizationStatus getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public SimulationState getState() {
        return state;
    }

}
