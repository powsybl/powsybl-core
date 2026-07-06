/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.security.limitscaling.LimitScaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parameters used in {@link SecurityAnalysisProvider#run} called in {@link SecurityAnalysis} API
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisRunParameters extends AbstractSecurityAnalysisRunParameters<SecurityAnalysisRunParameters> {

    private static final Supplier<SecurityAnalysisParameters> DEFAULT_SA_PARAMETERS_SUPPLIER = SecurityAnalysisParameters::load;

    private SecurityAnalysisParameters securityAnalysisParameters;
    private List<LimitScaling> limitScalings = new ArrayList<>();

    /**
     * Returns a {@link SecurityAnalysisRunParameters} instance with default value on each field.
     * @return the SecurityAnalysisRunParameters instance.
     */
    public static SecurityAnalysisRunParameters getDefault() {
        return new SecurityAnalysisRunParameters()
                .setFilter(DEFAULT_FILTER_SUPPLIER.get())
                .setSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link SecurityAnalysisParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_SA_PARAMETERS_SUPPLIER} before returning it.
     */
    public SecurityAnalysisParameters getSecurityAnalysisParameters() {
        if (securityAnalysisParameters == null) {
            setSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get());
        }
        return securityAnalysisParameters;
    }

    public List<LimitScaling> getLimitReductions() {
        return limitScalings;
    }

    /**
     * Sets the security analysis parameters, see {@link SecurityAnalysisParameters}.
     */
    public SecurityAnalysisRunParameters setSecurityAnalysisParameters(SecurityAnalysisParameters securityAnalysisParameters) {
        Objects.requireNonNull(securityAnalysisParameters, "Security analysis parameters should not be null");
        this.securityAnalysisParameters = securityAnalysisParameters;
        return self();
    }

    /**
     * Sets the list of the limit reductions to apply, see {@link LimitScaling}
     */
    public SecurityAnalysisRunParameters setLimitReductions(List<LimitScaling> limitScalings) {
        Objects.requireNonNull(limitScalings, "LimitReductions list should not be null");
        this.limitScalings = limitScalings;
        return self();
    }

    public SecurityAnalysisRunParameters addLimitReduction(LimitScaling limitScaling) {
        Objects.requireNonNull(limitScaling, "LimitScaling should not be null");
        limitScalings.add(limitScaling);
        return self();
    }

    @Override
    protected SecurityAnalysisRunParameters self() {
        return this;
    }
}
