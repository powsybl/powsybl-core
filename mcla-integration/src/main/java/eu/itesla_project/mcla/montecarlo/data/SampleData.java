/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo.data;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SampleData {
	
	private float[] generatorsActivePower;
	private float[] loadsActivePower;
	private float[] loadsReactivePower;
	
	public SampleData(float[] generatorsActivePower, float[] loadsActivePower, float[] loadsReactivePower) {
		this.generatorsActivePower = generatorsActivePower;
		this.loadsActivePower = loadsActivePower;
		this.loadsReactivePower = loadsReactivePower;
	}
	
	public float[] getGeneratorsActivePower() {
		return generatorsActivePower;
	}

	public float[] getLoadsActivePower() {
		return loadsActivePower;
	}

	public float[] getLoadsReactivePower() {
		return loadsReactivePower;
	}

}
