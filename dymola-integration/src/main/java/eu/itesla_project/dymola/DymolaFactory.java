/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.simulation.ImpactAnalysis;
import eu.itesla_project.modules.simulation.SimulatorFactory;
import eu.itesla_project.modules.simulation.Stabilization;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaFactory implements SimulatorFactory {

    @Override
    public Stabilization createStabilization(Network network, ComputationManager computationManager, int priority, DynamicDatabaseClientFactory ddbClientFactory) {
        return new DymolaStabilization(network, computationManager, priority, ddbClientFactory);
    }

    @Override
    public ImpactAnalysis createImpactAnalysis(Network network, ComputationManager computationManager, int priority, ContingenciesAndActionsDatabaseClient cadbClient) {
        return new DymolaImpactAnalysis(network, computationManager, priority, cadbClient);
    }

}
