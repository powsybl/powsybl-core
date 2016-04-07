/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ActionParameterBooleanValue;
import eu.itesla_project.modules.contingencies.ActionParameterFloatValue;
import eu.itesla_project.modules.contingencies.ActionParameterStringValue;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.ContingencyElement;
import eu.itesla_project.modules.contingencies.ContingencyElementType;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizer;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerParameters;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerResult;
import eu.itesla_project.modules.optimizer.PostContingencyState;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class CorrectiveControlOptimizerMock implements CorrectiveControlOptimizer {

	Logger logger = LoggerFactory.getLogger(CorrectiveControlOptimizerMock.class);
	
	ContingenciesAndActionsDatabaseClient cadbClient;
	
	public CorrectiveControlOptimizerMock(ContingenciesAndActionsDatabaseClient cadbClient) {
		this.cadbClient = cadbClient;
	}
	
	@Override
	public void close() throws Exception {
	}
	
	@Override
	public void init(CorrectiveControlOptimizerParameters parameters) {
	}

	@Override
	public CorrectiveControlOptimizerResult run(PostContingencyState postContingencyState) throws Exception {
		CorrectiveControlOptimizerResult result = null; 
		if ( postContingencyState!= null ) {
			logger.info("Working on contingency {} and state {}", postContingencyState.getContingency().getId(), postContingencyState.getStateId());
			result = new CorrectiveControlOptimizerResult(postContingencyState.getContingency().getId(), true);
			// get network in the post contingency state
			Network network = postContingencyState.getNetwork();
			logger.debug("Working on network state {}", network.getStateManager().getWorkingStateId());
			result.setActionPlan("mockActionPlan");
			result.setFinalStatus(CCOFinalStatus.MANUAL_CORRECTIVE_ACTION_FOUND);
			// search actions for the contingency
			for (ContingencyElement contingencyElement : postContingencyState.getContingency().getElements()) {
				if ( contingencyElement.getType().equals(ContingencyElementType.LINE) ) {
					String actionId = contingencyElement.getId() + "_opening";
					ActionParameters parameters = new ActionParameters();
					parameters.addParameter("string_action_param", new ActionParameterStringValue("value"));
					parameters.addParameter("float_action_param", new ActionParameterFloatValue(0f));
					parameters.addParameter("boolean_action_param", new ActionParameterBooleanValue(true));
					logger.debug("Adding action {} to solution for contingency {}", actionId, postContingencyState.getContingency().getId());
//					Map<String,ActionParameters> equipmentWithParameters = new HashMap<String, ActionParameters>();
//					equipmentWithParameters.put(contingencyElement.getId(), parameters);
//					result.addAction(actionId, equipmentWithParameters);
					result.addEquipment(actionId, contingencyElement.getId(), parameters);
				}
			}
		}
		return result;
	}



	

}
