/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;

/**
 * Load flow as a computation candidate for validation.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(CandidateComputation.class)
public class LoadFlowComputation implements CandidateComputation {

    @Override
    public String getName() {
        return "loadflow";
    }

    /**
     * Returns the loadflow factory configured in "loadflow-validation" module,
     * or else the default platform loadflow factory.
     */
    private static LoadFlowFactory getLoadFlowFactory() {

        PlatformConfig platformConfig = PlatformConfig.defaultConfig();

        if (platformConfig.moduleExists("loadflow-validation")) {
            ModuleConfig config = platformConfig.getModuleConfig("loadflow-validation");
            if (config.hasProperty("load-flow-factory")) {
                try {
                    return config.getClassProperty("load-flow-factory", LoadFlowFactory.class).newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new PowsyblException("Could not instantiate load flow factory.", e);
                }
            }
        }

        return ComponentDefaultConfig.load().newFactoryImpl(LoadFlowFactory.class);
    }

    @Override
    public void run(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(computationManager);

        LoadFlowParameters parameters = LoadFlowParameters.load();
        LoadFlow loadFlow = getLoadFlowFactory().create(network, computationManager, 0);
        loadFlow.run(StateManagerConstants.INITIAL_STATE_ID, parameters)
                .thenAccept(loadFlowResult -> {
                    if (!loadFlowResult.isOk()) {
                        throw new PowsyblException("Loadflow on network " + network.getId() + " does not converge");
                    }
                })
                .join();
    }
}
