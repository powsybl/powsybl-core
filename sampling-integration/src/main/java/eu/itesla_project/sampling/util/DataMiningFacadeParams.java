/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.util;

import java.util.*;

import eu.itesla_project.iidm.network.*;
import org.joda.time.Interval;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DataMiningFacadeParams {

	private final List<String> gensIds;
	private final List<String> loadsIds;
	private final List<String> danglingLinesIds;
	private final Interval interval;
	private final Set<Country> countries;

	public DataMiningFacadeParams(Network network, boolean generationSampled, boolean boundariesSampled, Interval interval) {

		gensIds = new ArrayList<>();
		if (generationSampled) {
			for (Generator gen : network.getGenerators()) {
				if (gen.getEnergySource().isIntermittent()) {
					gensIds.add(gen.getId());
				}
			}
		}
		// it seems that elements order in iidm model is not the same
		// after two subsequent network initialization from file
		Collections.sort(gensIds);

		loadsIds = new ArrayList<>();
	    for (Load load : network.getLoads()) {
			loadsIds.add(load.getId());
		}
	    Collections.sort(loadsIds);

		danglingLinesIds = new ArrayList<>();
		if (boundariesSampled) {
			for (DanglingLine dl : network.getDanglingLines()) {
				danglingLinesIds.add(dl.getId());
			}
		}
		Collections.sort(danglingLinesIds);

		countries = EnumSet.copyOf(network.getCountries());

	    this.interval = interval;
	}

	public List<String> getGensIds() {
		return gensIds;
	}

	public List<String> getLoadsIds() {
		return loadsIds;
	}

	public List<String> getDanglingLinesIds() {
		return danglingLinesIds;
	}

	public Set<Country> getCountries() {
		return countries;
	}

	public Interval getInterval() {
        return interval;
    }

}
