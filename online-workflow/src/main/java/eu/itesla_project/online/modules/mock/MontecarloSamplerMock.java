/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.MontecarloSampler;
import eu.itesla_project.modules.mcla.MontecarloSamplerParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MontecarloSamplerMock implements MontecarloSampler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MontecarloSamplerMock.class);

    public MontecarloSamplerMock(Network network, ComputationManager computationManager, ForecastErrorsDataStorage feDataStorage) {
    }

    @Override
    public void init(MontecarloSamplerParameters parameters) throws Exception {
        LOGGER.info("init-ing ...");
    }

    @Override
    public void sample() throws Exception {
        LOGGER.warn("running sampler mock");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
    }

    @Override
    public String getName() {
        return "MockMontecarloSampling";
    }

    @Override
    public String getVersion() {
        return "V1.0";
    }
}
