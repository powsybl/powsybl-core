/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.redispatcher;

import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RedispatcherImpl implements Redispatcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedispatcherImpl.class);
	
	private Network network;
	private Map<String, RedispatcherImpl.RedispatchLimits> redispatchLimits = new HashMap<String, RedispatcherImpl.RedispatchLimits>();
	private RedispatcherConfig config;
	
	public RedispatcherImpl(Network network) {
		this(network, RedispatcherConfig.load());
	}
	
	public RedispatcherImpl(Network network, RedispatcherConfig config) {
		this.network = network;
		this.config = config;
		LOGGER.info(config.toString());
		computeRedispatchLimits(network);
	}
	
	private void computeRedispatchLimits(Network network) {
		for(Generator generator : network.getGenerators()) {
			redispatchLimits.put(generator.getId(), new RedispatchLimits(RedispatchUtils.getRedispatchPMin(generator, config.getRedispatchLimitsPercentage()), 
					 													 RedispatchUtils.getRedispatchPMax(generator, config.getRedispatchLimitsPercentage())));
		}
	}
	
	@Override
	public RedispatchingResults redispatch(RedispatchingParameters parameters) {
		Objects.requireNonNull(parameters,"redispatching parameters are null");
		float deltaP = parameters.getDeltaP();
		if ( deltaP == 0 ) {
			LOGGER.info("No p to redispacthing in network {}", network.getId());
			return new RedispatchingResults(0f, 0f);
		}
		if ( parameters.getParticipationFactor() == null ) // if no participation factor as been provided as input
			parameters.setParticipationFactor(RedispatchUtils.getParticipationFactor(network)); // compute the participation factor of the network 
		LOGGER.info("Redispatching {} MW in network {}", deltaP, network.getId());
		List<Generator> redispatchableGenerators = null;
		if ( parameters.getGeneratorsToUse() != null )
			redispatchableGenerators = RedispatchUtils.filterRedispatchableGenerators(network, parameters.getGeneratorsToUse(), config.getRedispatchLimitsPercentage());
		else
			redispatchableGenerators = RedispatchUtils.getRedispatchableGenerators(network, parameters.getGeneratorsToSkip(), config.getRedispatchLimitsPercentage());
		float totalRedispatchedP = 0;
		// run until all the delta P has been redispatched and there are generators that can be redispatched
		while( deltaP != 0 && redispatchableGenerators.size() > 0 ) { 
			float totalPartecipationFactor = getTotalPartecipationFactor(parameters.getParticipationFactor(), redispatchableGenerators);
			LOGGER.debug("totalPartecipationFactor = {}", totalPartecipationFactor);
			float remainingDeltaP = 0;
			List<Generator> remainingRedispatchableGenerators = new ArrayList<Generator>();
			float redispactchedP = 0;
			// distribute the P in the available redispatchable generators for this run
			for(Generator generator : redispatchableGenerators) {
				float redispatchPMin = redispatchLimits.containsKey(generator.getId()) ? redispatchLimits.get(generator.getId()).getPMin() : generator.getMinP();
				float redispatchPMax = redispatchLimits.containsKey(generator.getId()) ? redispatchLimits.get(generator.getId()).getPMax() : generator.getMaxP();
				// calculate new P according to delta P to redispatch and participation factor
				float newP = newP(generator, deltaP, parameters.getParticipationFactor().get(generator.getId()), totalPartecipationFactor);
//				LOGGER.debug("{}: generator {} - new computed P:{}", network.getStateManager().getWorkingStateId(), generator.getId(), newP);
				// keep P within redispatch limits
				if ( -newP <= redispatchPMin ) {
					remainingDeltaP -= (redispatchPMin + newP); // the P outside the limits will be redispacthed at the following run
					newP = -redispatchPMin;
				} else if ( -newP >= redispatchPMax ) {
					remainingDeltaP += (-newP - redispatchPMax);  // the P outside the limits will be redispacthed at the following run
					newP = -redispatchPMax;
				} else
					remainingRedispatchableGenerators.add(generator); // not outsied or at the limits -> I can use it for the next run
				// redispatch on this generator
				LOGGER.debug("{}: generator {} - P:{} -> P:{} - limits[{},{}], redispatch limits[{},{}]", 
						network.getStateManager().getWorkingStateId(), generator.getId(), generator.getTerminal().getP(), newP,
						generator.getMinP(), generator.getMaxP(), redispatchPMin, redispatchPMax);
				redispactchedP += (-newP + generator.getTerminal().getP());
				generator.getTerminal().setP(newP);
				generator.setTargetP(-newP);
//				LOGGER.debug("Redispatched {} MW", redispactchedP);
//				LOGGER.debug("Remaining {} MW", remainingDeltaP);
			}
			LOGGER.debug("Redispatched {} MW", redispactchedP); // P redispatched at this run
			totalRedispatchedP += redispactchedP;
			if ( remainingDeltaP != 0 )
				LOGGER.debug("Still to redispatch {} MW in network {}", remainingDeltaP, network.getId()); // remaining delta P of this run
			deltaP = remainingDeltaP;
			redispatchableGenerators = remainingRedispatchableGenerators;
		}
		LOGGER.info("Redispatched {} MW in network {}", totalRedispatchedP, network.getId());
		if ( deltaP != 0 )
			LOGGER.warn("Cannot redispatch {} MW in network {}", deltaP, network.getId());
		return new RedispatchingResults(totalRedispatchedP, deltaP);
	}
	
	private float getTotalPartecipationFactor(Map<String, Float> partecipationFactor, List<Generator> redispatchableGenerators) {
		List<String> generatorsIds = redispatchableGenerators.stream().map(Generator::getId).collect(Collectors.toList());
		return (float) partecipationFactor.keySet().stream().filter(x -> generatorsIds.contains(x)).mapToDouble((x) -> partecipationFactor.get(x)).sum();
	}

	private float newP(Generator generator, float deltaP, float participationFactor, float totalPartecipationFactor) {
		return generator.getTerminal().getP() - deltaP * participationFactor / totalPartecipationFactor;
	}
	
	class RedispatchLimits {
		
		float pMin;
		float pMax;
		
		RedispatchLimits(float pMin, float pMax) {
			this.pMin = pMin;
			this.pMax = pMax;
		}
		
		public float getPMin() {
			return pMin;
		}

		public float getPMax() {
			return pMax;
		}
		
	}

}
