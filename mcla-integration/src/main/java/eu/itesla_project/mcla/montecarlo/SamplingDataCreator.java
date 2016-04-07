/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.montecarlo.data.BusData;
import eu.itesla_project.mcla.montecarlo.data.LoadData;
import eu.itesla_project.mcla.montecarlo.data.SamplingNetworkData;
import eu.itesla_project.mcla.montecarlo.data.SlackBusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.montecarlo.data.GeneratorData;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SamplingDataCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingDataCreator.class);

	Network network;
	private ArrayList<String> generatorsIds = new ArrayList<String>();
	private ArrayList<String> loadsIds = new ArrayList<String>();
	SamplingNetworkData samplingData = null;

	public SamplingDataCreator(Network network, ArrayList<String> generatorsIds, ArrayList<String> loadsIds) {
		Objects.requireNonNull(network, "network is null");
		Objects.requireNonNull(generatorsIds, "generatorsIds is null");
		Objects.requireNonNull(loadsIds, "loadsIds is null");
		this.network = network;
		this.generatorsIds = generatorsIds;
		this.loadsIds = loadsIds;
	}

	public SamplingDataCreator(Network network) {
		this(network, NetworkUtils.getRenewableGeneratorsIds(network), NetworkUtils.getLoadsIds(network));
	}

	public SamplingNetworkData createSamplingNetworkData() {
		HashMap<String, Integer> busMapping = new HashMap<String, Integer>();
		LOGGER.debug("Getting buses data from {} network", network.getId());
		ArrayList<BusData> busesData = getBusData(busMapping);
		LOGGER.debug("Getting generators data from {} network", network.getId());
		ArrayList<GeneratorData> generatorsData = getGeneratorData(busMapping);
		LOGGER.debug("Getting loads data from {} network", network.getId());
		ArrayList<LoadData> loadsData = getLoadData(busMapping);
		samplingData = new SamplingNetworkData(busesData, generatorsData, loadsData);
		return samplingData;
	}

	public SamplingNetworkData getSamplingNetworkData() {
		return samplingData;
	}


	protected ArrayList<BusData> getBusData(HashMap<String, Integer> busMapping) {
		ArrayList<BusData> busesData = new ArrayList<BusData>();
		int busIndex = 0;
		SlackBusData slackBusData = new SlackBusData();
        for (Bus bus : network.getBusBreakerView().getBuses()) {
        	busIndex++;
//        	printBusData(bus, busIndex);
        	busMapping.put(bus.getId(), busIndex);
        	BusData busData = new BusData(bus.getId());
        	busData.setBusName(bus.getName());
        	busData.setBusIndex(busIndex);
        	busData.setBusType(MCSNetworkUtils.getBusType(bus));
        	busData.setNominalVoltage(bus.getVoltageLevel().getNominalV());
        	busData.setVoltage(bus.getV());
        	busData.setAngle(bus.getAngle());
        	busData.setMinVoltage(bus.getVoltageLevel().getLowVoltageLimit());
        	busData.setMaxVoltage(bus.getVoltageLevel().getHighVoltageLimit());
        	busData.setActivePower(bus.getP());
        	busData.setReactivePower(bus.getQ());
        	busesData.add(busData);
        	updateSlackBusData(bus, busIndex, slackBusData);
        	LOGGER.debug(busData.toString());
        }
        if ( slackBusData.getSlackBusIndex() != -1) {
        	BusData busData = busesData.get(slackBusData.getSlackBusIndex()-1);
        	busData.setBusType(BusData.BUS_TYPE_SLACK); // slack bus
        	LOGGER.debug(busData.toString());
        }
        return busesData;
	}

	protected void updateSlackBusData(Bus bus, Integer busIndex, SlackBusData slackBusData) {
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

	protected ArrayList<GeneratorData> getGeneratorData(HashMap<String, Integer> busMapping) {
		ArrayList<GeneratorData> generatorsData = new ArrayList<GeneratorData>();
		// I put the generators in a specific order, the same used when producing the matrix of historical data, for the forecast errors analysis
		for( String generatorId : generatorsIds ) {
			Generator generator = network.getGenerator(generatorId);
			if ( generator != null ) {
	    		GeneratorData generatorData = new GeneratorData(generator.getId());
	    		Bus generatorBus = generator.getTerminal().getBusBreakerView().getBus();
	    		if ( generatorBus == null )
	    			generatorBus = generator.getTerminal().getBusBreakerView().getConnectableBus();
	    		if ( generatorBus == null ) { // it should not happen
	    			LOGGER.warn("Skipping generator " + generator.getId() + ": not connected/connectable to a bus");
	    			continue;
	    		}
	    		generatorData.setBusId(generatorBus.getId());
	    		generatorData.setBusIndex(busMapping.get(generatorBus.getId()));
	    		generatorData.setConnected(NetworkUtils.isConnected(generator));
	    		//generatorData.setActvePower(generator.getTargetP());
	    		generatorData.setActvePower(generator.getTerminal().getP());
	    		//generatorData.setReactvePower(generator.getTargetQ());
	    		generatorData.setReactvePower(generator.getTerminal().getQ());
	    		generatorData.setMinActivePower(generator.getMinP());
	    		generatorData.setMaxActivePower(generator.getMaxP());
	    		generatorData.setMinReactivePower(generator.getReactiveLimits().getMinQ(generator.getTargetP()));
	    		generatorData.setMaxReactivePower(generator.getReactiveLimits().getMaxQ(generator.getTargetP()));
	    		generatorData.setNominalPower(MCSNetworkUtils.getNominalPower(generator));
	    		generatorData.setRenewableEnergySource(MCSNetworkUtils.getRenewableEnergySource(generator));
	    		generatorData.setFuelType(MCSNetworkUtils.getFuelType(generator));
	    		generatorData.setDispatchable(MCSNetworkUtils.isDispatchable(generator));
	    		generatorsData.add(generatorData);
	    		LOGGER.debug(generatorData.toString());
			} else {
				LOGGER.warn("Skipping missing generator in the network: {}", generatorId); // it should not happen
			}
    	}
        return generatorsData;
	}

	protected ArrayList<LoadData> getLoadData(HashMap<String, Integer> busMapping) {
		ArrayList<LoadData> loadsData = new ArrayList<LoadData>();
		// I put the loads in a specific order, the same used when producing the matrix of historical data, for the forecast errors analysis
		for( String loadId : loadsIds ) {
			Load load = network.getLoad(loadId);
			if ( load != null ) {
	    		LoadData loadData = new LoadData(load.getId());
	    		Bus loadBus = load.getTerminal().getBusBreakerView().getBus();
	    		if ( loadBus == null )
	    			loadBus = load.getTerminal().getBusBreakerView().getConnectableBus();
	    		if ( loadBus == null ) { // it should not happen
	    			LOGGER.warn("Skipping load " + load.getId() + ": not connected/connectable to a bus");
	    			continue;
	    		}
	    		loadData.setBusId(loadBus.getId());
	    		loadData.setBusIndex(busMapping.get(loadBus.getId()));
	    		loadData.setConnected(NetworkUtils.isConnected(load));
	    		//loadData.setActvePower(load.getP0());
	    		loadData.setActvePower(load.getTerminal().getP());
	    		//loadData.setReactvePower(load.getQ0());
	    		loadData.setReactvePower(load.getTerminal().getQ());
                if (load.getTerminal().getBusView().getBus() != null) {
                    loadData.setVoltage(load.getTerminal().getBusView().getBus().getV());
                }
	    		loadsData.add(loadData);
	    		LOGGER.debug(loadData.toString());
			} else {
				LOGGER.warn("Skipping missing load in the network: {}", loadId); // it should not happen
			}
		}
		return loadsData;
	}

//	private void printBusData(Bus bus, int busIndex) {
//    	System.out.println("*****************************************");
//    	String value = "nome=" + bus.getName()
//    				+ "; codice=" + bus.getId()
//    				+ "; id=" + busIndex
//    				+ "; Vnom=" + bus.getVoltageLevel().getNominalV()
//    				+ "; V=" + bus.getV() //(bus.getV()/bus.getVoltageLevel().getNominalV())
//    				+ "; angle=" + bus.getAngle()
//    				+ "; Vmin=" + bus.getVoltageLevel().getLowVoltageLimit() //(bus.getVoltageLevel().getLowVoltageLimit()/bus.getVoltageLevel().getNominalV())
//    				+ "; Vmax=" + bus.getVoltageLevel().getHighVoltageLimit() //(bus.getVoltageLevel().getHighVoltageLimit()/bus.getVoltageLevel().getNominalV())
//    				+ "; P=" + bus.getP()
//    				+ "; Q=" + bus.getQ()
//    				;
//    	System.out.println(value);
//    	int both = 0;
//    	String genIds = "gens:";
//    	for(Generator generator : bus.getGenerators() ) {
//    		if ( both == 0 )
//    			both = 1;
//    		genIds += generator.getId() + "-type=" + generator.getEnergySource() + "-P=" + generator.getTargetP() + "-Q=" + generator.getTargetQ() + "-" + generator.getTerminal().getBusBreakerView().getBus().getId() + "=" + generator.getTerminal().getBusBreakerView().getBus().getId().equals(bus.getId()) + ";";
//    	}
//    	System.out.println(genIds);
//    	String loadIds = "loads:";
//    	for(Load load : bus.getLoads() ) {
//    		if ( both == 1 )
//    			both = 2;
//    		loadIds += load.getId() + "-P=" + load.getP0() + "-Q=" + load.getQ0() + "-" + load.getTerminal().getBusBreakerView().getBus().getId() + "=" + load.getTerminal().getBusBreakerView().getBus().getId().equals(bus.getId()) + ";";
//    	}
//    	System.out.println(loadIds);
//    	if ( both == 2 )
//    		System.out.println("both");
//	}


}
