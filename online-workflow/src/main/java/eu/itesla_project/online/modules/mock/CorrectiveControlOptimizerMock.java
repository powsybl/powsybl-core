/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			result = new CorrectiveControlOptimizerResult(postContingencyState.getContingency().getId(), false);
			result.setFinalStatus(CCOFinalStatus.NO_CORRECTIVE_ACTION_FOUND);
		}
		return result;
	}



	

}
