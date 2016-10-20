/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.itesla_project.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class NetworkDataExtractor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDataExtractor.class);
	
	public static NetworkData extract(Network network) {
		Objects.requireNonNull(network, "network is null");
		LOGGER.info("Extracting data of network {}", network.getId());
		NetworkData networkData = new NetworkData(network.getId());
		extractBusesData(network, networkData);
		extractLinesData(network, networkData);
		extractTfo2WData(network, networkData);
		extractTfo3WData(network, networkData);
		extractGeneratorsData(network, networkData);
		extractLoadsData(network, networkData);
		return networkData;
	}
	
	private static void extractBusesData(Network network, NetworkData networkData) {
		SlackBusData slackBusData = new NetworkDataExtractor().new SlackBusData();
		int busIndex = 0;
		for (Bus bus : network.getBusBreakerView().getBuses()) {
			
			List<String> generators = new ArrayList<String>();
			List<Float> generatorsActivePower = new ArrayList<Float>();
			List<Float> generatorsReactivePower = new ArrayList<Float>();
			//bus.getGenerators().forEach( (generator) -> generators.add(generator.getId()) );
			bus.getGenerators().forEach( (generator) -> { 
				generators.add(generator.getId());
				generatorsActivePower.add(generator.getTerminal().getP());
				generatorsReactivePower.add(generator.getTerminal().getQ());
			});
			float activeInjection = generatorsActivePower.isEmpty() ? Float.NaN : generatorsActivePower.stream().reduce(0f, (a, b) -> a + b);
			float reactiveInjection = generatorsReactivePower.isEmpty() ? Float.NaN : generatorsReactivePower.stream().reduce(0f, (a, b) -> a + b);
			
			List<String> loads = new ArrayList<String>();
			List<Float> loadsActivePower = new ArrayList<Float>();
			List<Float> loadsReactivePower = new ArrayList<Float>();
			//bus.getLoads().forEach( (load) -> loads.add(load.getId()) );
			bus.getLoads().forEach( (load) -> {
				loads.add(load.getId());
				loadsActivePower.add(load.getTerminal().getP());
				loadsReactivePower.add(load.getTerminal().getQ());
			});
			float activeAbsorption = loadsActivePower.isEmpty() ? Float.NaN : loadsActivePower.stream().reduce(0f, (a, b) -> a + b);
			float reactiveAbsorption = loadsReactivePower.isEmpty() ? Float.NaN : loadsReactivePower.stream().reduce(0f, (a, b) -> a + b);
			
			networkData.addBusData(new BusData(bus.getId(), 
											   bus.getVoltageLevel().getNominalV(), 
											   bus.getV(), 
											   bus.getAngle(), 
											   bus.getGenerators().iterator().hasNext(),
											   generators,
											   activeInjection,
											   reactiveInjection,
											   bus.getLoads().iterator().hasNext(),
											   loads,
											   activeAbsorption,
											   reactiveAbsorption,
											   bus.getP(),
											   bus.getQ(),
											   false)
			);
			updateSlackBusData(bus, busIndex, slackBusData);
			
			busIndex++;
		}
		if ( slackBusData.getSlackBusIndex() != -1) {
        	BusData busData = networkData.getBusesData().get(slackBusData.getSlackBusIndex());
        	busData.setSlack(true); // slack bus
		}
	}
	
	private static void extractLinesData(Network network, NetworkData networkData) {
		for(Line line : network.getLines()) {
			if ( line.getTerminal1().getVoltageLevel().getNominalV() >= 110) {
				networkData.addLineData(new LineData(line.getId(), 
													 (line.getTerminal1().getBusBreakerView().getBus() != null) 
													 	? line.getTerminal1().getBusBreakerView().getBus().getId()
													 	: line.getTerminal1().getBusBreakerView().getConnectableBus().getId(),
													 (line.getTerminal2().getBusBreakerView().getBus() != null) 
													 	? line.getTerminal2().getBusBreakerView().getBus().getId()
													 	: line.getTerminal2().getBusBreakerView().getConnectableBus().getId(),
													 line.getTerminal1().getI(),
													 line.getTerminal2().getI(),
													 (line.getCurrentLimits1() != null) ? line.getCurrentLimits1().getPermanentLimit() : Float.NaN,
													 (line.getCurrentLimits2() != null) ? line.getCurrentLimits2().getPermanentLimit() : Float.NaN)
				);
			}
		}
	}
	
	private static void extractTfo2WData(Network network, NetworkData networkData) {
		for(TwoWindingsTransformer tfo : network.getTwoWindingsTransformers()) {
			networkData.addTfo2WData(new Tfo2WData(tfo.getId(), 
												   (tfo.getTerminal1().getBusBreakerView().getBus() != null)
												   		? tfo.getTerminal1().getBusBreakerView().getBus().getId()
												   		: tfo.getTerminal1().getBusBreakerView().getConnectableBus().getId(), 
												   	(tfo.getTerminal2().getBusBreakerView().getBus() != null)
												   		? tfo.getTerminal2().getBusBreakerView().getBus().getId()
												   		: tfo.getTerminal2().getBusBreakerView().getConnectableBus().getId(), 
												   apparentPower(tfo.getTerminal1()),
												   apparentPower(tfo.getTerminal2()),
												   tfo.getTerminal1().getVoltageLevel().getNominalV(),
												   tfo.getTerminal2().getVoltageLevel().getNominalV(),
												   (tfo.getCurrentLimits1() != null) ? tfo.getCurrentLimits1().getPermanentLimit() : Float.NaN,
												   (tfo.getCurrentLimits2() != null) ? tfo.getCurrentLimits2().getPermanentLimit() : Float.NaN,
												   isRegulating(tfo), 
												   correntStepPosition(tfo))
			);
		}
	}
	
	private static void extractTfo3WData(Network network, NetworkData networkData) {
		for(ThreeWindingsTransformer tfo : network.getThreeWindingsTransformers()) {
			networkData.addTfo3WData(new Tfo3WData(tfo.getId(), 
												   (tfo.getLeg1().getTerminal().getBusBreakerView().getBus() != null) 
												   		? tfo.getLeg1().getTerminal().getBusBreakerView().getBus().getId()
												   		: tfo.getLeg1().getTerminal().getBusBreakerView().getConnectableBus().getId(), 
												   (tfo.getLeg2().getTerminal().getBusBreakerView().getBus() != null) 
												   		? tfo.getLeg2().getTerminal().getBusBreakerView().getBus().getId()
												   		: tfo.getLeg2().getTerminal().getBusBreakerView().getConnectableBus().getId(), 
												   (tfo.getLeg3().getTerminal().getBusBreakerView().getBus() != null) 
												   		? tfo.getLeg3().getTerminal().getBusBreakerView().getBus().getId()
												   		: tfo.getLeg3().getTerminal().getBusBreakerView().getConnectableBus().getId(),
												   apparentPower(tfo.getLeg1().getTerminal()), 
												   apparentPower(tfo.getLeg2().getTerminal()),
												   apparentPower(tfo.getLeg3().getTerminal()),
												   tfo.getLeg1().getTerminal().getVoltageLevel().getNominalV(),
												   tfo.getLeg1().getTerminal().getVoltageLevel().getNominalV(),
												   tfo.getLeg3().getTerminal().getVoltageLevel().getNominalV(),
												   (tfo.getLeg1().getCurrentLimits() != null) ? tfo.getLeg1().getCurrentLimits().getPermanentLimit() : Float.NaN,
												   (tfo.getLeg2().getCurrentLimits() != null) ? tfo.getLeg2().getCurrentLimits().getPermanentLimit() : Float.NaN,
												   (tfo.getLeg3().getCurrentLimits() != null) ? tfo.getLeg3().getCurrentLimits().getPermanentLimit() : Float.NaN)
			);
		}
	}
	
	private static void extractGeneratorsData(Network network, NetworkData networkData) {
		for(Generator generator : network.getGenerators()) {
			networkData.addGeneratorData(new GeneratorData(generator.getId(), 
														   (generator.getTerminal().getBusBreakerView().getBus() != null) 
														   			? generator.getTerminal().getBusBreakerView().getBus().getId() 
														   			: generator.getTerminal().getBusBreakerView().getConnectableBus().getId(), 
														   (generator.getTerminal().getBusBreakerView().getBus() != null), 
														   apparentPower(generator.getTerminal()),
														   generator.getTerminal().getP(),
														   generator.getTerminal().getQ(),
														   generator.getRatedS(),
														   generator.getReactiveLimits().getMaxQ(generator.getTargetP()),
														   generator.getReactiveLimits().getMinQ(generator.getTargetP()))
//														   generator.getReactiveLimits().getMaxQ(generator.getTerminal().getP()),
//														   generator.getReactiveLimits().getMinQ(generator.getTerminal().getP()))
			);
		}
	}
	
	private static void extractLoadsData(Network network, NetworkData networkData) {
		for(Load load : network.getLoads()) {
			networkData.addLoadData(new LoadData(load.getId(),
												 (load.getTerminal().getBusBreakerView().getBus() != null) 
												 		? load.getTerminal().getBusBreakerView().getBus().getId() 
												 		: load.getTerminal().getBusBreakerView().getConnectableBus().getId(),
												 (load.getTerminal().getBusBreakerView().getBus() != null),
												 load.getTerminal().getVoltageLevel().getNominalV(),
												 load.getTerminal().getP(),
												 load.getTerminal().getQ())
			);
		}
	}
	
	private static float apparentPower(Terminal terminal) {
		float apparentPower = Float.NaN;
		if ( !Float.isNaN(terminal.getP()) && !Float.isNaN(terminal.getQ()) )
			apparentPower = (float) Math.sqrt(Math.pow(terminal.getP(), 2) + Math.pow(terminal.getQ(), 2));
		return apparentPower;
	}

	private static boolean isRegulating(TwoWindingsTransformer tfo) {
		if ( tfo.getPhaseTapChanger() != null )
			return tfo.getPhaseTapChanger().getRegulationMode() != PhaseTapChanger.RegulationMode.OFF;
		if ( tfo.getRatioTapChanger() != null )
			return tfo.getRatioTapChanger().isRegulating();
		return false;
	}
	
	private static int correntStepPosition(TwoWindingsTransformer tfo) {
		if ( tfo.getPhaseTapChanger() != null )
			return tfo.getPhaseTapChanger().getTapPosition();
		if ( tfo.getRatioTapChanger() != null )
			return tfo.getRatioTapChanger().getTapPosition();
		return 0;
	}
		
	private static void updateSlackBusData(Bus bus, Integer busIndex, SlackBusData slackBusData) {
		if ( bus.getGenerators() == null ) return;
        //...slackbus has at least one generator connected
        for ( Generator generator :  bus.getGenerators() )
        {
              //...which has a generator with voltage regulator on
              if ( !generator.isVoltageRegulatorOn()) continue;
              //...assure the generator is the one connected to the bus (and not on the aggregated buses)
              if ( !generator.getTerminal().getBusBreakerView().getBus().getId().equals(bus.getId()) ) return;
              //...candidate slackbus
              if ( slackBusData.getSlackBusIndex() == -1 ) {
            	  slackBusData.setSlackBusIndex(busIndex);
                  slackBusData.setSlackBusGenerator(generator);
                  return;
              }
              //...choice the generator with the largest TargetP
              if ( generator.getTargetP() > slackBusData.getSlackBusGenerator().getTargetP() ) {
            	  slackBusData.setSlackBusIndex(busIndex);
                  slackBusData.setSlackBusGenerator(generator);
              }
        }
	}
	
	class SlackBusData {

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
}
