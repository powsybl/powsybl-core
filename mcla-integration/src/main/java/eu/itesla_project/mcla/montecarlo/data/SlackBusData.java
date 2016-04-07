/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo.data;

import eu.itesla_project.iidm.network.Generator;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SlackBusData {

	int slackBusIndex = -1;
	Generator slackBusGenerator = null;
	
	public int getSlackBusIndex() {
		return slackBusIndex;
	}
	public void setSlackBusIndex(int slackBusIndex) {
		this.slackBusIndex = slackBusIndex;
	}
	public Generator getSlackBusGenerator() {
		return slackBusGenerator;
	}
	public void setSlackBusGenerator(Generator slackBusGenerator) {
		this.slackBusGenerator = slackBusGenerator;
	}
	
}
