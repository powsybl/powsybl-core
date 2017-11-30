/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.it>
 */
public class SecurityAnalyzer {

    public enum Format {
        CSV,
        JSON
    }

    private final LimitViolationFilter filter;

    private final ComputationManager computationManager;

    private final int priority;

    private final SecurityAnalysisFactory securityAnalysisFactory;

    private final ContingenciesProviderFactory contingenciesProviderFactory;

    /**
     * @deprecated use SecurityAnalyzer(LimitViolationFilter, ComputationManager, int) instead.
     */
    @Deprecated
    public SecurityAnalyzer(ComputationManager computationManager, int priority) {
        this(new LimitViolationFilter(), computationManager, priority);
    }

    public SecurityAnalyzer(LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        this.filter = Objects.requireNonNull(filter);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.priority = priority;

        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        securityAnalysisFactory = defaultConfig.newFactoryImpl(SecurityAnalysisFactory.class);
        contingenciesProviderFactory = defaultConfig.newFactoryImpl(ContingenciesProviderFactory.class);
    }

    public SecurityAnalysisResult analyze(Network network, Path contingenciesFile) {
        Objects.requireNonNull(network);

        ContingenciesProvider contingenciesProvider = contingenciesFile != null
                ? contingenciesProviderFactory.create(contingenciesFile) : new EmptyContingencyListProvider();

        return analyze(network, contingenciesProvider);
    }

    public SecurityAnalysisResult analyze(String filename, InputStream networkData, InputStream contingencies) {
        Objects.requireNonNull(networkData);
        Objects.requireNonNull(filename);

        Network network = Importers.loadNetwork(filename, networkData);
        if (network == null) {
            throw new PowsyblException("Error loading network");
        }

        ContingenciesProvider contingenciesProvider = contingencies != null
                ? contingenciesProviderFactory.create(contingencies) : new EmptyContingencyListProvider();

        return analyze(network, contingenciesProvider);
    }

    public SecurityAnalysisResult analyze(Network network, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(contingenciesProvider);

        network.getStateManager().allowStateMultiThreadAccess(true);

        SecurityAnalysis securityAnalysis = securityAnalysisFactory.create(network, filter, computationManager, priority);

        return securityAnalysis.runAsync(contingenciesProvider).join();
    }

}
