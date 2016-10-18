/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.simulation.ImpactAnalysis;
import eu.itesla_project.simulation.SimulatorFactory;
import eu.itesla_project.simulation.Stabilization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PostContLoadFlowSimFactory implements SimulatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostContLoadFlowSimFactory.class);

    private final PostContLoadFlowSimConfig config = PostContLoadFlowSimConfig.load();

    private final LoadFlowFactory loadFlowFactory;

    public PostContLoadFlowSimFactory() {
        LOGGER.info(config.toString());
        try {
            loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stabilization createStabilization(Network network, ComputationManager computationManager, int priority) {
        return new PostContLoadFlowSimStabilization(network, config);
    }

    @Override
    public ImpactAnalysis createImpactAnalysis(Network network, ComputationManager computationManager, int priority, ContingenciesProvider contingenciesProvider) {
        return new PostContLoadFlowSimImpactAnalysis(network, computationManager, priority, contingenciesProvider, config, loadFlowFactory);
    }

}
