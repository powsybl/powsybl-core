/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.execution;

import com.google.common.io.ByteSource;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.execution.AbstractSecurityAnalysisExecutionInput;

import java.util.Objects;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisExecutionInput extends AbstractSecurityAnalysisExecutionInput<DynamicSecurityAnalysisExecutionInput> {

    private DynamicSecurityAnalysisParameters parameters;
    private ByteSource dynamicModelsSource;

    public DynamicSecurityAnalysisParameters getParameters() {
        return parameters;
    }

    public ByteSource getDynamicModelsSource() {
        return dynamicModelsSource;
    }

    public DynamicSecurityAnalysisExecutionInput setParameters(DynamicSecurityAnalysisParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
        return self();
    }

    public DynamicSecurityAnalysisExecutionInput setDynamicModelsSource(ByteSource dynamicModelsSource) {
        this.dynamicModelsSource = Objects.requireNonNull(dynamicModelsSource);
        return self();
    }

    @Override
    protected DynamicSecurityAnalysisExecutionInput self() {
        return this;
    }
}
