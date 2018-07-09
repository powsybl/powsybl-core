/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.it>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalyzer {

    private final LimitViolationFilter filter;

    private final ComputationManager computationManager;

    private final int priority;

    private final SecurityAnalysisFactory securityAnalysisFactory;

    private final ContingenciesProviderFactory contingenciesProviderFactory;

    private final Set<SecurityAnalysisInterceptor> interceptors;

    public SecurityAnalyzer(LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        this(filter, computationManager, priority, Collections.emptySet());
    }

    public SecurityAnalyzer(LimitViolationFilter filter, ComputationManager computationManager, int priority, Set<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.priority = priority;
        this.interceptors = Objects.requireNonNull(interceptors);

        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        securityAnalysisFactory = defaultConfig.newFactoryImpl(SecurityAnalysisFactory.class);
        contingenciesProviderFactory = defaultConfig.newFactoryImpl(ContingenciesProviderFactory.class);
    }

    public SecurityAnalysisResult analyze(Network network, Path contingenciesFile) {
        return analyze(network, contingenciesFile, SecurityAnalysisParameters.load());
    }

    public SecurityAnalysisResult analyze(Network network, Path contingenciesFile, SecurityAnalysisParameters parameters) {
        Objects.requireNonNull(network);

        ContingenciesProvider contingenciesProvider = contingenciesFile != null
                ? contingenciesProviderFactory.create(contingenciesFile) : new EmptyContingencyListProvider();

        return analyze(network, contingenciesProvider, parameters);
    }

    public SecurityAnalysisResult analyze(String filename, InputStream networkData, InputStream contingencies) {
        return analyze(filename, networkData, contingencies, SecurityAnalysisParameters.load());
    }

    public SecurityAnalysisResult analyze(String filename, InputStream networkData, InputStream contingencies, SecurityAnalysisParameters parameters) {
        Objects.requireNonNull(networkData);
        Objects.requireNonNull(filename);

        Network network = Importers.loadNetwork(filename, networkData);
        if (network == null) {
            throw new PowsyblException("Error loading network");
        }

        ContingenciesProvider contingenciesProvider = contingencies != null
                ? contingenciesProviderFactory.create(contingencies) : new EmptyContingencyListProvider();

        return analyze(network, contingenciesProvider, parameters);
    }

    public SecurityAnalysisResult analyze(Network network, ContingenciesProvider contingenciesProvider) {
        return analyze(network, contingenciesProvider, SecurityAnalysisParameters.load());
    }

    public SecurityAnalysisResult analyze(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(contingenciesProvider);
        Objects.requireNonNull(parameters);

        network.getStateManager().allowStateMultiThreadAccess(true);

        SecurityAnalysis securityAnalysis = securityAnalysisFactory.create(network, filter, computationManager, priority);
        interceptors.forEach(securityAnalysis::addInterceptor);

        return securityAnalysis.run(network.getStateManager().getWorkingStateId(), parameters, contingenciesProvider).join();
    }
}
