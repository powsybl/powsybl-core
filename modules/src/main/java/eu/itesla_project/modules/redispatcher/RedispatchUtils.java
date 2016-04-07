/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.redispatcher;

import eu.itesla_project.iidm.network.EnergySource;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;

import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RedispatchUtils {
	
	public static Map<String, Float> getParticipationFactor(Network network) {
		Map<String, Float> partecipationFactor = new HashMap<String, Float>();
		for(Generator generator : network.getGenerators()) {
			partecipationFactor.put(generator.getId(), generator.getMaxP());
		}
		return partecipationFactor;
	}
		
	public static List<Generator> getRedispatchableGenerators(Network network, String[] generatorsToSkip, float redispatchLimitsPercentage) {
		List<Generator> redispatchableGenerators = new ArrayList<Generator>();
		for(Generator generator : network.getGenerators()) {
			if ( isRedispatchable(generator, redispatchLimitsPercentage) ) {
				if ( generatorsToSkip != null ) { // check if there are generators to skip
					if ( !Arrays.asList(generatorsToSkip).contains(generator.getId()) ) // check if this generator have to be skipped
						redispatchableGenerators.add(generator);
				} else
					redispatchableGenerators.add(generator);
			}
		}
		return redispatchableGenerators;
	}
	
	public static List<Generator> filterRedispatchableGenerators(Network network, String[] generatorsToUse, float redispatchLimitsPercentage) {
		List<Generator> redispatchableGenerators = new ArrayList<Generator>();
		for(Generator generator : network.getGenerators()) {
			if ( isRedispatchable(generator, redispatchLimitsPercentage) ) {
				if ( generatorsToUse != null ) { // check if there are generators to use
					if ( Arrays.asList(generatorsToUse).contains(generator.getId()) ) // check if this generator can be used
						redispatchableGenerators.add(generator);
				} else
					redispatchableGenerators.add(generator);
			}
		}
		return redispatchableGenerators;
	}
	
	public static boolean isRedispatchable(Generator generator, float redispatchLimitsPercentage) {
		return (generator.getTerminal().getBusBreakerView().getBus() != null)  // is connected
				&& (generator.getEnergySource() == EnergySource.HYDRO || generator.getEnergySource() == EnergySource.THERMAL) // is hydro or thermal
				&& (generator.getTerminal().getP() < 0) // inject power
				&& (generator.isVoltageRegulatorOn()) // has voltage regulator on
				&& (generator.getTargetP() <= getRedispatchPMax(generator, redispatchLimitsPercentage) 
					&& generator.getTargetP() >= getRedispatchPMin(generator, redispatchLimitsPercentage)) // target P is within redispatch limits
				//&& (generator.getTargetP() <= generator.getMaxP() && generator.getTargetP() >= generator.getMinP()) // target P is within limits
				;
	}
	
	public static float getRedispatchPMax(Generator generator, float redispatchLimitsPercentage) {
		float redispatchPMax = generator.getMaxP();
		if ( generator.getTargetP() < generator.getMinP() )
			redispatchPMax = generator.getMinP() + redispatchLimitsPercentage * 0.01f * generator.getMaxP();
		else
			redispatchPMax = generator.getTargetP() + redispatchLimitsPercentage * 0.01f * generator.getMaxP();
		return generator.getMaxP() > redispatchPMax ? redispatchPMax : generator.getMaxP();
	}
	
	public static float getRedispatchPMin(Generator generator, float redispatchLimitsPercentage) {
		float redispatchPMin = generator.getMinP();
		if ( generator.getTargetP() > generator.getMaxP() )
			redispatchPMin = generator.getMaxP() - redispatchLimitsPercentage * 0.01f * generator.getMaxP();
		else
			redispatchPMin = generator.getTargetP() - redispatchLimitsPercentage * 0.01f * generator.getMaxP();
		return generator.getMinP() < redispatchPMin ? redispatchPMin : generator.getMinP();
	}
	

}
