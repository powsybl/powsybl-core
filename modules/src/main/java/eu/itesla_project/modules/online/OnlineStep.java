/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public enum OnlineStep {
	FORECAST_ERRORS_ANALYSIS,
	MERGING,
	WORST_CASE_APPROACH,
	MONTE_CARLO_SAMPLING,
	LOAD_FLOW,
	SECURITY_RULES_ASSESSMENT,
	CONTROL_ACTION_OPTIMIZATION,
	TIME_DOMAIN_SIMULATION,
	STABILIZATION,
	IMPACT_ANALYSIS,
	MONTE_CARLO_LIKE_APPROACH,
	POSTCONTINGENCY_LOAD_FLOW
}
