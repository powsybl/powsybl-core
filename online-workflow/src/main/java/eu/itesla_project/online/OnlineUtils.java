/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.securityindexes.SecurityIndex;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineUtils {

	public static Collection<Contingency> filterContingencies(List<Contingency> contingencies, List<String> contingenciesIds) {
		Objects.requireNonNull(contingencies, "contingencies list is null");
		Objects.requireNonNull(contingenciesIds, "contingenciesIds list is null");
		List<Contingency> filteredContingencies = new ArrayList<Contingency>();
		for (Contingency contingency : contingencies) {
			if ( contingenciesIds.contains(contingency.getId()) )
					filteredContingencies.add(contingency);
		}
		return filteredContingencies;
	}
	
	public static Set<String> getContingencyIds(List<Contingency> contingencies) {
		Objects.requireNonNull(contingencies, "contingencies list is null");
		Set<String> contingencyIds = new HashSet<>();
		for (Contingency contingency : contingencies) {
			contingencyIds.add(contingency.getId());
		}
		return contingencyIds;
	}
	
	public static boolean isSafe(Collection<SecurityIndex> securityIndexes) {
		Objects.requireNonNull(securityIndexes, "security indexes collection is null");
		for ( SecurityIndex index : securityIndexes ) {
            if ( !index.isOk() )
            	return false;
        }
		return true;
	}
}
