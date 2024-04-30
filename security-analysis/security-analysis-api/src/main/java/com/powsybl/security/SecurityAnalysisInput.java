/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;

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
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisInput {

    private final NetworkVariant networkVariant;
    private Set<SecurityAnalysisInterceptor> interceptors;
    private LimitViolationFilter filter;
    private ContingenciesProvider contingencies;
    private SecurityAnalysisParameters parameters;

    public SecurityAnalysisInput(Network network, String variantId) {
        this(new NetworkVariant(network, variantId));
    }

    public SecurityAnalysisInput(NetworkVariant networkVariant) {
        this.networkVariant = Objects.requireNonNull(networkVariant);
        this.interceptors = new HashSet<>();
        this.filter = new LimitViolationFilter();
        this.contingencies = ContingenciesProviders.emptyProvider();
        this.parameters = new SecurityAnalysisParameters();
    }

    /**
     * Get specified {@link SecurityAnalysisParameters}.
     */
    public SecurityAnalysisParameters getParameters() {
        return parameters;
    }

    /**
     * Get specified {@link ContingenciesProvider}.
     */
    public ContingenciesProvider getContingenciesProvider() {
        return contingencies;
    }

    public LimitViolationFilter getFilter() {
        return filter;
    }

    public Set<SecurityAnalysisInterceptor> getInterceptors() {
        return Collections.unmodifiableSet(interceptors);
    }

    public SecurityAnalysisInput setContingencies(ContingenciesProvider contingencies) {
        Objects.requireNonNull(contingencies);
        this.contingencies = contingencies;
        return this;
    }

    public SecurityAnalysisInput setParameters(SecurityAnalysisParameters parameters) {
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
}
