/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.simulation.ImpactAnalysis;
import eu.itesla_project.simulation.SimulatorFactory;
import eu.itesla_project.simulation.Stabilization;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaFactory implements SimulatorFactory {

    @Override
    public Stabilization createStabilization(Network network, ComputationManager computationManager, int priority) {
        return new DymolaStabilization(network, computationManager, priority);
    }

    @Override
    public ImpactAnalysis createImpactAnalysis(Network network, ComputationManager computationManager, int priority, ContingenciesProvider contingenciesProvider) {
        return new DymolaImpactAnalysis(network, computationManager, priority, contingenciesProvider);
    }

}
