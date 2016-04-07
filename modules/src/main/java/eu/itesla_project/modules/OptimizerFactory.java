/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.topo.TopologyMiner;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OptimizerFactory {

    Optimizer create(Network network, ComputationManager computationManager, int priority, HistoDbClient histoDbClient, TopologyMiner topologyMiner);

}
