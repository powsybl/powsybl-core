/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.security.AbstractSecurityAnalysisRunParameters;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parameters used in {@link DynamicSecurityAnalysisProvider#run} called in {@link DynamicSecurityAnalysis} API
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisRunParameters extends AbstractSecurityAnalysisRunParameters<DynamicSecurityAnalysisRunParameters> {

    private static final Supplier<DynamicSecurityAnalysisParameters> DEFAULT_SA_PARAMETERS_SUPPLIER = DynamicSecurityAnalysisParameters::load;

    private DynamicSecurityAnalysisParameters dynamicSecurityAnalysisParameters;
    private EventModelsSupplier eventModelsSupplier = EventModelsSupplier.empty();

    /**
     * Returns a {@link DynamicSecurityAnalysisRunParameters} instance with default value on each field.
     * @return the SecurityAnalysisRunParameters instance.
     */
    public static DynamicSecurityAnalysisRunParameters getDefault() {
        return new DynamicSecurityAnalysisRunParameters()
                .setFilter(DEFAULT_FILTER_SUPPLIER.get())
                .setDynamicSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link DynamicSecurityAnalysisParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_SA_PARAMETERS_SUPPLIER} before returning it.
     */
    public DynamicSecurityAnalysisParameters getDynamicSecurityAnalysisParameters() {
        if (dynamicSecurityAnalysisParameters == null) {
            setDynamicSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get());
        }
        return dynamicSecurityAnalysisParameters;
    }

    /**
     * Sets the security analysis parameters, see {@link DynamicSecurityAnalysisParameters}.
     */
    public DynamicSecurityAnalysisRunParameters setDynamicSecurityAnalysisParameters(DynamicSecurityAnalysisParameters dynamicSecurityAnalysisParameters) {
        Objects.requireNonNull(dynamicSecurityAnalysisParameters, "Security analysis parameters should not be null");
        this.dynamicSecurityAnalysisParameters = dynamicSecurityAnalysisParameters;
        return self();
    }

    public EventModelsSupplier getEventModelsSupplier() {
        return eventModelsSupplier;
    }

    public DynamicSecurityAnalysisRunParameters setEventModelsSupplier(EventModelsSupplier eventModelsSupplier) {
        this.eventModelsSupplier = eventModelsSupplier;
        return self();
    }

    @Override
    protected DynamicSecurityAnalysisRunParameters self() {
        return this;
    }
}
