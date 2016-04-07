/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.redispatcher;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RedispatcherConfig {
	
	/*
	 * example of redispatcher.properties file.
	 *
	# percentage used to compute redispatching limits (pmin and pmax of generators)
	redispatchLimitsPercentage=10

	*/
	
	private final float redispatchLimitsPercentage;
	
	public RedispatcherConfig(float redispatchLimitsPercentage) {
		this.redispatchLimitsPercentage = redispatchLimitsPercentage;
	}
	
	public static RedispatcherConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("redispatcher");
        
        float redispatchLimitsPercentage = config.getFloatProperty("redispatchLimitsPercentage");

        return new RedispatcherConfig(redispatchLimitsPercentage);
	}
	

	public float getRedispatchLimitsPercentage() {
		return redispatchLimitsPercentage;
	}
	
	@Override
	public String toString() {
		return "RedispatcherConfig ["+ "redispatchLimitsPercentage=" + redispatchLimitsPercentage + "]";
	}


}
