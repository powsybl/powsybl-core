/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.loadflow.api.LoadFlowParameters;

import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface SecurityAnalysis {

    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, LoadFlowParameters parameters);

    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId);

    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider);
}
