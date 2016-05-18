/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizer;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class CorrectiveControlOptimizerMockFactoryImpl implements CorrectiveControlOptimizerFactory {

	@Override
	public CorrectiveControlOptimizer create(ContingenciesAndActionsDatabaseClient cadbClient, ComputationManager computationManager) {
		return new CorrectiveControlOptimizerMock();
	}

}
