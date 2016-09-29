/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.optimizer;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.contingency.Contingency;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class PostContingencyState {
	
	private Network network;
	private final String stateId;
	private final Contingency contingency;
	
	public PostContingencyState(Network network, String stateId, Contingency contingency) {
		this.network = network;
		this.stateId = stateId;
		this.contingency = contingency;
	}

	
	public Network getNetwork() {
		network.getStateManager().setWorkingState(stateId);
		return network;
	}
	
	public String getStateId() {
		return stateId;
	}

	public Contingency getContingency() {
		return contingency;
	}

	@Override
	public String toString() {
		return "postCtgState[newtwork= "+ network.getId() + ", id=" + stateId + ", cid=" + contingency.getId() + "]";
	}
	
	
	

}
