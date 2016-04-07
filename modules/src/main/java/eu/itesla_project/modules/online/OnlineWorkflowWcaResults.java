/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.util.Collection;
import java.util.List;

/**
 * The results of Worst Case Approach step during a run of the online workflow
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineWorkflowWcaResults {
	
	/**
	 * Get the id of the workflow where the WCA has been appplied
	 * @return the id of the workflow
	 */
	String getWorkflowId();
	
	/**
	 * Get the time horizon used for the workflow where the WCA has been applied
	 * @return the time horizon of the workflow
	 */
	TimeHorizon getTimeHorizon();
	
	/**
	 * Get the contingencies analyzed by the workflow with the WCA
	 * @return the collection of ids of the contingencies analyzed with the WCA
	 */
	Collection<String> getContingencies();
	
	/**
	 * Get the cluster index the WCA assigned to a contingency
	 * @param contingencyId the id of the contingency
	 * @return the cluster number assigned by the WCA
	 */
	int getClusterIndex(String contingencyId);
	
	
	/**
	 * Get the causes of the assignment of the contingency in the cluster
	 * @param contingencyId the id of the contingency
	 * @return the causes of the assignment of the contingency in the cluster
	 */
	List<String> getCauses(String contingencyId);

}
