/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * Input data/configuration for a {@link SecurityAnalysis} computation.
 * It is designed to be mutable, as it may be customized by {@link SecurityAnalysisPreprocessor}s.
 * However, all fields must always be non {@literal null}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public class SecurityAnalysisInput {

    private final NetworkVariant networkVariant;
    private Set<SecurityAnalysisInterceptor> interceptors;
    private LimitViolationFilter filter;
    private LimitViolationDetector detector;
    private InputStream dynamicModels;
    private InputStream eventModels;
    private ContingenciesProvider contingencies;
    private SecurityAnalysisParametersInterface parameters;

    public SecurityAnalysisInput(Network network, String variantId) {
        this(new NetworkVariant(network, variantId));
    }

    public SecurityAnalysisInput(NetworkVariant networkVariant) {
        this.networkVariant = Objects.requireNonNull(networkVariant);
        this.interceptors = new HashSet<>();
        this.filter = new LimitViolationFilter();
        this.detector = new DefaultLimitViolationDetector();
        this.contingencies = ContingenciesProviders.emptyProvider();
        this.parameters = new SecurityAnalysisParameters();
        this.dynamicModels = null;
        this.eventModels = null;
    }

    /**
     * Get specified {@link SecurityAnalysisParameters}.
     */
    public SecurityAnalysisParameters getStaticParameters() {
        if (!isDynamic() && parameters instanceof SecurityAnalysisParameters staticParameters) {
            return staticParameters;
        }
        throw new PowsyblException("The security analysis is dynamic : SecurityAnalysisInput cannot return static parameter");
    }

    public DynamicSecurityAnalysisParameters getDynamicParameters() {
        if (isDynamic() && parameters instanceof DynamicSecurityAnalysisParameters dynamicParameters) {
            return dynamicParameters;
        }
        throw new PowsyblException("The security analysis is static : SecurityAnalysisInput cannot return dynamic parameter");
    }

    public InputStream getDynamicModels() {
        return dynamicModels;
    }

    public InputStream getEventModels() {
        return eventModels;
    }

    public boolean hasEventModels() {
        return eventModels != null;
    }

    /**
     * Get specified {@link ContingenciesProvider}.
     */
    public ContingenciesProvider getContingenciesProvider() {
        return contingencies;
    }

    /**
     * Get specified {@link LimitViolationDetector}.
     */
    public LimitViolationDetector getLimitViolationDetector() {
        return detector;
    }

    public LimitViolationFilter getFilter() {
        return filter;
    }

    public Set<SecurityAnalysisInterceptor> getInterceptors() {
        return Collections.unmodifiableSet(interceptors);
    }

    public SecurityAnalysisInput setDetector(LimitViolationDetector detector) {
        Objects.requireNonNull(detector);
        this.detector = detector;
        return this;
    }

    public SecurityAnalysisInput setDynamicModelsSupplier(InputStream dynamicModels) {
        Objects.requireNonNull(dynamicModels);
        this.dynamicModels = dynamicModels;
        return this;
    }

    public SecurityAnalysisInput setEventModelsSupplier(InputStream eventModels) {
        Objects.requireNonNull(eventModels);
        this.eventModels = eventModels;
        return this;
    }

    public SecurityAnalysisInput setContingencies(ContingenciesProvider contingencies) {
        Objects.requireNonNull(contingencies);
        this.contingencies = contingencies;
        return this;
    }

    public SecurityAnalysisInput setParameters(SecurityAnalysisParametersInterface parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
        return this;
    }

    public SecurityAnalysisInput addInterceptor(SecurityAnalysisInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
        return this;
    }

    public SecurityAnalysisInput setFilter(LimitViolationFilter filter) {
        this.filter = Objects.requireNonNull(filter);
        return this;
    }

    public NetworkVariant getNetworkVariant() {
        return networkVariant;
    }

    public boolean isDynamic() {
        if (dynamicModels == null && eventModels != null) {
            throw new PowsyblException("Event model supplier cannot be set without a dynamic model supplier");
        }
        return dynamicModels != null;
    }
}
