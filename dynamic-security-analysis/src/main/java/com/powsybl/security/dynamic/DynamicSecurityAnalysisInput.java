/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dynamic;

import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.AbstractSecurityAnalysisInput;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;

import java.util.Objects;

/**
 *
 * Input data/configuration for a {@link DynamicSecurityAnalysis} computation.
 * It is designed to be mutable, as it may be customized by {@link SecurityAnalysisPreprocessor}s.
 * However, all fields must always be non {@literal null}.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisInput extends AbstractSecurityAnalysisInput<DynamicSecurityAnalysisInput> {

    private final DynamicModelsSupplier dynamicModels;
    private EventModelsSupplier eventModels;
    private DynamicSecurityAnalysisParameters parameters;

    public DynamicSecurityAnalysisInput(Network network, String variantId, DynamicModelsSupplier dynamicModelsSupplier) {
        this(new NetworkVariant(network, variantId), dynamicModelsSupplier);
    }

    public DynamicSecurityAnalysisInput(NetworkVariant networkVariant, DynamicModelsSupplier dynamicModelsSupplier) {
        super(networkVariant);
        Objects.requireNonNull(dynamicModelsSupplier);
        this.dynamicModels = dynamicModelsSupplier;
        this.parameters = new DynamicSecurityAnalysisParameters();
    }

    public DynamicSecurityAnalysisParameters getParameters() {
        return parameters;
    }

    public DynamicModelsSupplier getDynamicModels() {
        return dynamicModels;
    }

    public DynamicSecurityAnalysisInput setParameters(DynamicSecurityAnalysisParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
        return self();
    }

    @Override
    protected DynamicSecurityAnalysisInput self() {
        return this;
    }
}
