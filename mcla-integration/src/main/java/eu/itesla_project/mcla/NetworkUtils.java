/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Terminal;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class NetworkUtils {

	public static ArrayList<String> getRenewableGeneratorsIds(Network network) {
		Objects.requireNonNull(network, "network is null");
		ArrayList<String> generatorsIds = new ArrayList<String>();
		for ( Generator generator : network.getGenerators() ) {
			if ( generator.getEnergySource().isIntermittent() ) {
				generatorsIds.add(generator.getId());
			}
		}
		Collections.sort(generatorsIds);
		return generatorsIds;
	}

	public static ArrayList<String> getGeneratorsIds(Network network) {
		Objects.requireNonNull(network, "network is null");
		ArrayList<String> generatorsIds = new ArrayList<String>();
		for ( Generator generator : network.getGenerators() ) {
			generatorsIds.add(generator.getId());
		}
		Collections.sort(generatorsIds);
		return generatorsIds;
	}

	public static ArrayList<String> getConnectedGeneratorsIds(Network network) {
		Objects.requireNonNull(network, "network is null");
		ArrayList<String> generatorsIds = new ArrayList<String>();
		for ( Generator generator : network.getGenerators() ) {
			if ( isConnected(generator) )
    			generatorsIds.add(generator.getId());
		}
		Collections.sort(generatorsIds);
		return generatorsIds;
	}

	public static boolean isConnected(Generator generator) {
		Bus generatorBus = generator.getTerminal().getBusBreakerView().getBus();
		float voltage = getV(generator.getTerminal());
		if ( generatorBus != null && !Float.isNaN(voltage) )  // generator is connected
			return true;
		return false;
	}

	public static ArrayList<String> getLoadsIds(Network network) {
		Objects.requireNonNull(network, "network is null");
		ArrayList<String> loadsIds = new ArrayList<String>();
		for ( Load load : network.getLoads() ) {
			loadsIds.add(load.getId());
		}
	    Collections.sort(loadsIds);
	    return loadsIds;
	}

	public static ArrayList<String> getConnectedLoadsIds(Network network) {
		Objects.requireNonNull(network, "network is null");
		ArrayList<String> loadsIds = new ArrayList<String>();
		for ( Load load : network.getLoads() ) {
			if ( isConnected(load) )
    			loadsIds.add(load.getId());
		}
	    Collections.sort(loadsIds);
	    return loadsIds;
	}

	public static boolean isConnected(Load load) {
		Bus generatorBus = load.getTerminal().getBusBreakerView().getBus();
		float voltage = getV(load.getTerminal());
		if ( generatorBus != null && !Float.isNaN(voltage) )  // load is connected
			return true;
		return false;
	}

	private static float getV(Terminal t) {
		Bus b = t.getBusView().getBus();
		return b != null ? b.getV() : Float.NaN;
	}



}
