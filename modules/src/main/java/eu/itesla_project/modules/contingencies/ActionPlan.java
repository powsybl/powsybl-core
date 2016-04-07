/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface ActionPlan {

	String getName();
	
	String getDescription();
	
	//Collection of zone's Id
	//Collection<BigInteger> getZones();
	Collection<String> getZones();
	
	//<key,value> : <priority ,option>
	Map<BigInteger, eu.itesla_project.modules.contingencies.ActionPlanOption> getPriorityOption();
	
}
