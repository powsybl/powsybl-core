/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.powsybl.commons.Versionable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This is intended to become the base class for all dynamic simulator integrated
 * in the platform.
 * TODO: extend the API with simulation result querying
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ImpactAnalysis extends Versionable {

    void init(SimulationParameters parameters, Map<String, Object> context) throws Exception;

    ImpactAnalysisResult run(SimulationState state) throws Exception;

    ImpactAnalysisResult run(SimulationState state, Set<String> contingencyIds) throws Exception;

    CompletableFuture<ImpactAnalysisResult> runAsync(SimulationState state, Set<String> contingencyIds, ImpactAnalysisProgressListener listener);

}
