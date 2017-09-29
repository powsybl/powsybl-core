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
public class MergeOptimizerMockImpl implements MergeOptimizer {
    public MergeOptimizerMockImpl(Network network, ComputationManager computationManager) {
    }

    @Override
    public String getName() {
        return "mock merge-optimizer";
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean optimize() throws Exception {
        return true;
    }
}
