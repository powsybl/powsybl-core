/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.mcla;

import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsStatistics {
	
	private String[] injectionsIds;
	private float[] means;
	private float[] standardDeviations;
	
	public ForecastErrorsStatistics(String[] injectionsIds, float[] means, float[] standardDeviations) {
		Objects.requireNonNull(injectionsIds, "injection ids array is null");
		this.injectionsIds = injectionsIds;
		this.means = means;
		this.standardDeviations = standardDeviations;
	}

	public String[] getInjectionsIds() {
		return injectionsIds;
	}

	public float[] getMeans() {
		return means;
	}

	public float[] getStandardDeviations() {
		return standardDeviations;
	}
}
