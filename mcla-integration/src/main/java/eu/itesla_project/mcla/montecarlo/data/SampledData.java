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
public class SampledData {
	
	private double[][] generatorsActivePower;
	private double[][] loadsActivePower;
	private double[][] loadsReactivePower;
	
	public SampledData(double[][] generatorsActivePower, double[][] loadsActivePower, double[][] loadsReactivePower) {
		this.generatorsActivePower = generatorsActivePower;
		this.loadsActivePower = loadsActivePower;
		this.loadsReactivePower = loadsReactivePower;
	}
	
	public double[][] getGeneratorsActivePower() {
		return generatorsActivePower;
	}

	public double[][] getLoadsActivePower() {
		return loadsActivePower;
	}

	public double[][] getLoadsReactivePower() {
		return loadsReactivePower;
	}

}
