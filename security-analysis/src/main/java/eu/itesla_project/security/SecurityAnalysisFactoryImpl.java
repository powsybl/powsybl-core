/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.commons.io.ComponentDefaultConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisFactoryImpl implements SecurityAnalysisFactory {
    @Override
    public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
        ComponentDefaultConfig defaultConfig = new ComponentDefaultConfig();
        try {
            LoadFlowFactory loadFlowFactory = defaultConfig.findFactoryImplClass(LoadFlowFactory.class).newInstance();
            return new SecurityAnalysisImpl(network, computationManager, loadFlowFactory);
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
