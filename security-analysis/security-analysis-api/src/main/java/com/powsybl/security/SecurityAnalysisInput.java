/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Network;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;

import java.util.Objects;

/**
 *
 * Input data/configuration for a {@link SecurityAnalysis} computation.
 * It is designed to be mutable, as it may be customized by {@link SecurityAnalysisPreprocessor}s.
 * However, all fields must always be non {@literal null}.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisInput extends AbstractSecurityAnalysisInput<SecurityAnalysisInput> {

    private SecurityAnalysisParameters parameters;

    public SecurityAnalysisInput(Network network, String variantId) {
        this(new NetworkVariant(network, variantId));
    }

    public SecurityAnalysisInput(NetworkVariant networkVariant) {
        super(networkVariant);
        this.parameters = new SecurityAnalysisParameters();
    }

    /**
     * Get specified {@link SecurityAnalysisParameters}.
     */
    public SecurityAnalysisParameters getParameters() {
        return parameters;
    }

    public SecurityAnalysisInput setParameters(SecurityAnalysisParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
        return self();
    }

    @Override
    protected SecurityAnalysisInput self() {
        return this;
    }
}
