/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dynamic.execution;

import com.powsybl.computation.ComputationManager;
import com.powsybl.security.SecurityAnalysisReport;

import java.util.concurrent.CompletableFuture;

/**
 *
 * Represents a dynamic security analysis to be executed on inputs typically provided as files and
 * text-formatted options. The actual execution may happen inside this JVM,
 * based on an underlying {@link com.powsybl.security.dynamic.DynamicSecurityAnalysis} implementation,
 * or be forwarded to external processes through a {@link ComputationManager}.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSecurityAnalysisExecution {

    CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager,
                                                      DynamicSecurityAnalysisExecutionInput data);
}
