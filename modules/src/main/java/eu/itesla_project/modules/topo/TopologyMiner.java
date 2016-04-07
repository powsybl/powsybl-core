/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.Module;
import eu.itesla_project.modules.histo.HistoDbClient;
import org.joda.time.Interval;

import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TopologyMiner extends Module, AutoCloseable {

    TopologyContext initContext(Network network, Interval histoInterval, double correlationThreshold, double probabilityThreshold, HistoDbClient histoDbClient, ComputationManager computationManager);

    TopologyContext loadContext(Path dir, Interval histoInterval, double correlationThreshold, double probabilityThreshold);

    void predictTopology(Network network, TopologyContext context);

}
