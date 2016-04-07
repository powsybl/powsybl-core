/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.redispatcher;

import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RedispatchingParameters {
	
	private final float deltaP; // P to redispatch
	private String[] generatorsToUse = null; // generators to use while redispatching
	private String[] generatorsToSkip = null; // generators to skip while redispatching
	private Map<String, Float> participationFactor = null; // participation factor to redispatching for each generator
	
	public RedispatchingParameters(float deltaP) {
		this.deltaP = deltaP;
	}
	
	public float getDeltaP() {
		return deltaP;
	}
	
	public String[] getGeneratorsToUse() {
		return generatorsToUse;
	}

	public void setGeneratorsToUse(String[] generatorsToUse) {
		this.generatorsToUse = generatorsToUse;
	}
	
	public String[] getGeneratorsToSkip() {
		return generatorsToSkip;
	}

	public void setGeneratorsToSkip(String[] generatorsToSkip) {
		this.generatorsToSkip = generatorsToSkip;
	}

	public Map<String, Float> getParticipationFactor() {
		return participationFactor;
	}

	public void setParticipationFactor(Map<String, Float> participationFactor) {
		this.participationFactor = participationFactor;
	}

}
