/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import java.util.ArrayList;
import java.util.Objects;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.forecast_errors.data.StochasticVariable;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class FEANetworkUtils {
	
	public static ArrayList<StochasticVariable> getStochasticVariables(Network network) {
		return getStochasticVariables(network, NetworkUtils.getGeneratorsIds(network), NetworkUtils.getLoadsIds(network));
	}
	
	public static ArrayList<StochasticVariable> getStochasticVariables(Network network, ArrayList<String> generatorsIds, 
																  	   ArrayList<String> loadsIds) {
		Objects.requireNonNull(network, "network is null");
		Objects.requireNonNull(generatorsIds, "generatorsIds is null");
		Objects.requireNonNull(loadsIds, "loadsIds is null");
		ArrayList<StochasticVariable> stochasticVariables = new ArrayList<StochasticVariable>(); 
		for( String generatorId : generatorsIds ) {
			Generator generator = network.getGenerator(generatorId);
			if ( generator != null ) {
				String stochasticVariablesType = null;
				switch (generator.getEnergySource()) {
				case WIND:
					stochasticVariablesType = StochasticVariable.TYPE_WIND_GENERATOR;
					break;
				case SOLAR:
					stochasticVariablesType = StochasticVariable.TYPE_SOLAR_GENERATOR;
					break;
				default:
					stochasticVariablesType = ""; // it should not happen
					break;
				}
				Country country = generator.getTerminal().getVoltageLevel().getSubstation().getCountry();
				StochasticVariable stochasticVariable = new StochasticVariable(generatorId, stochasticVariablesType, country);
				stochasticVariables.add(stochasticVariable);
			}
		}
		for( String loadId : loadsIds ) {
			Load load = network.getLoad(loadId);
			if ( load != null ) {
				Country country = load.getTerminal().getVoltageLevel().getSubstation().getCountry();
				StochasticVariable stochasticVariable = new StochasticVariable(loadId, StochasticVariable.TYPE_LOAD, country);
				stochasticVariables.add(stochasticVariable);
			}
		}
		return stochasticVariables;
	}
}
