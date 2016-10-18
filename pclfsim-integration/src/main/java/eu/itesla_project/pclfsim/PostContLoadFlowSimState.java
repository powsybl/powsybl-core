/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.simulation.SimulationState;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PostContLoadFlowSimState implements SimulationState {

    private final String baseStateId;

    private final List<LimitViolation> baseViolations;

    PostContLoadFlowSimState(String baseStateId, List<LimitViolation> baseViolations) {
        this.baseStateId = Objects.requireNonNull(baseStateId);
        this.baseViolations = Objects.requireNonNull(baseViolations);
    }

    String getBaseStateId() {
        return baseStateId;
    }

    List<LimitViolation> getBaseViolations() {
        return baseViolations;
    }

    @Override
    public String getName() {
        return baseStateId;
    }

}
