/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.contingency.ContingenciesProviderFactory;
import eu.itesla_project.contingency.mock.ContingenciesProviderMock;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.it>
 */
public class SecurityAnalyzer {

    private final ComputationManager computationManager;
    private final int priority;
    private final SecurityAnalysisFactory securityAnalysisFactory;
    private final ContingenciesProviderFactory contingenciesProviderFactory;

    public SecurityAnalyzer(ComputationManager computationManager, int priority){
        this.computationManager = Objects.requireNonNull(computationManager);
        this.priority = priority;
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        securityAnalysisFactory = defaultConfig.newFactoryImpl(SecurityAnalysisFactory.class);
        contingenciesProviderFactory = defaultConfig.newFactoryImpl(ContingenciesProviderFactory.class);
    }
    
    public SecurityAnalyzer(ComputationManager computationManager, int priority, SecurityAnalysisFactory securityAnalysisFactory, ContingenciesProviderFactory contingenciesProviderFactory) {
        this.computationManager = Objects.requireNonNull(computationManager);
        this.priority = priority;
        this.securityAnalysisFactory = Objects.requireNonNull(securityAnalysisFactory);
        this.contingenciesProviderFactory = Objects.requireNonNull(contingenciesProviderFactory);
    }

    public SecurityAnalysisResult analyze(Path caseFile, Path contingenciesFile) {
        Objects.requireNonNull(caseFile);

        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case '" + caseFile + "' not found");
        }
        network.getStateManager().allowStateMultiThreadAccess(true);

        SecurityAnalysis securityAnalysis = securityAnalysisFactory.create(network, computationManager, priority);

        ContingenciesProvider contingenciesProvider = contingenciesFile != null
                ? contingenciesProviderFactory.create(contingenciesFile) : new ContingenciesProviderMock();

        return securityAnalysis.runAsync(contingenciesProvider).join();
    }

}
