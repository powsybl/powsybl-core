/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.redispatcher;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RedispatchingResults {
	
	private final float redispatchedP; // redispatched P
	private final float remainingDeltaP; // remaining delta P, that the redisparcher was not able to redispatch
	
	public RedispatchingResults(float redispatchedP, float remainingDeltaP) {
		this.redispatchedP = redispatchedP;
		this.remainingDeltaP = remainingDeltaP;
	}
	
	public float getRedispatchedP() {
		return redispatchedP;
	}

	public float getRemainingDeltaP() {
		return remainingDeltaP;
	}

}
