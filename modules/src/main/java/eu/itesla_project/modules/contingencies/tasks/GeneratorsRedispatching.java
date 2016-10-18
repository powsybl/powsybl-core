/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.contingency.tasks.ModificationTask;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.redispatcher.Redispatcher;
import eu.itesla_project.modules.redispatcher.RedispatcherFactoryImpl;
import eu.itesla_project.modules.redispatcher.RedispatchingParameters;

import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class GeneratorsRedispatching implements ModificationTask {

	private final List<String> generatorIds;
    private final ActionParameters actionsParameters;
    

    public GeneratorsRedispatching(List<String> generatorIds, ActionParameters actionsParameters) {
        this.generatorIds = generatorIds;
        this.actionsParameters = actionsParameters;
    }

    @Override
    public void modify(Network network) {
    	Float deltaP = (Float) actionsParameters.getValue(ActionParameters.REDISPATCHING_DELTAP_PARAMETER);
    	if ( deltaP == null ) {
            throw new ITeslaException("Missing delta P parameter for redispatching of generators "+generatorIds);
        }
    	for(String generatorId : generatorIds) {
    		Generator g = network.getGenerator(generatorId);
            if (g == null) {
                throw new ITeslaException("Generator '" + generatorId + "' not found");
            }
    	}
    	Redispatcher redispatcher = new RedispatcherFactoryImpl().create(network);
		RedispatchingParameters parameters = new RedispatchingParameters(deltaP);
		parameters.setGeneratorsToUse(generatorIds.toArray(new String[generatorIds.size()]));
		redispatcher.redispatch(parameters);
    }

}
