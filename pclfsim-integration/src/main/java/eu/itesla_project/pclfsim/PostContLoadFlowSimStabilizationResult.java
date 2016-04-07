/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.modules.simulation.SimulationState;
import eu.itesla_project.modules.simulation.StabilizationResult;
import eu.itesla_project.modules.simulation.StabilizationStatus;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PostContLoadFlowSimStabilizationResult implements StabilizationResult {

    private final PostContLoadFlowSimState state;

    PostContLoadFlowSimStabilizationResult(PostContLoadFlowSimState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public StabilizationStatus getStatus() {
        return StabilizationStatus.COMPLETED;
    }

    @Override
    public Map<String, String> getMetrics() {
        return ImmutableMap.of();
    }

    @Override
    public SimulationState getState() {
        return state;
    }

}
