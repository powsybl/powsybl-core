/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.sampling.SamplerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SamplerWp41Factory implements SamplerFactory {

    @Override
    public SamplerWp41 create(Network network, ComputationManager computationManager, int priority, HistoDbClient histoDbClient) {
        return new SamplerWp41(network, computationManager, priority, histoDbClient);
    }

}
