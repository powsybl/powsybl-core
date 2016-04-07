/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.optimizer;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public enum CCOFinalStatus {
	MANUAL_CORRECTIVE_ACTION_FOUND,
	AUTOMATIC_CORRECTIVE_ACTION_FOUND,
	NO_CONSTRAINT_VIOLATED,
	NO_CORRECTIVE_ACTION_FOUND,
	NO_SUPPORTED_CORRECTIVE_ACTION_AVAILABLE_IN_THE_DATABASE,
	OPTIMIZER_EXECUTION_ERROR,
	OPTIMIZER_INTERNAL_ERROR;
}
