/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors.data;

import java.util.Objects;

import eu.itesla_project.iidm.network.Country;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class StochasticVariable {

	public static final String TYPE_SOLAR_GENERATOR = "solar";
	public static final String TYPE_WIND_GENERATOR = "wind";
	public static final String TYPE_LOAD = "load";
	
	final String id;
	final String type;
	final Country country;
	
	public StochasticVariable(String id, String type, Country country) {
		Objects.requireNonNull(id, "id is null");
		Objects.requireNonNull(type, "type is null");
		this.id = id;
		this.type = type;
		this.country = country;
	}
	
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	
	public Country getCountry() {
		return country;
	}
	
	@Override
	public String toString() {
		if ( getId() == null || getType() == null )
			return null;
		return "[" + getId() + "," + getType() + "]";
	}
}
