/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.merge;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MergeOptimizerFactoryMockImpl implements MergeOptimizerFactory {
    public MergeOptimizer newMergeOptimizer(final Network network, final ComputationManager computationManager) {
        return new MergeOptimizerMockImpl(network, computationManager);
    }
}
