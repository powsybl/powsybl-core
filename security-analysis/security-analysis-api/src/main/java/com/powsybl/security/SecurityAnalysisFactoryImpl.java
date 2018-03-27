/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisFactoryImpl implements SecurityAnalysisFactory {

    @Override
    public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
        return create(network, new LimitViolationFilter(), computationManager, priority);
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        LoadFlowFactory loadFlowFactory = defaultConfig.newFactoryImpl(LoadFlowFactory.class);
        return new SecurityAnalysisImpl(network, filter, computationManager, loadFlowFactory);
    }
}
