/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@RequestScoped
public class SimulatorInstListProducer {

    @Inject
	private Logger log;
    
	@EJB
	private DDBManager pmanager;

    private List<SimulatorInst> simulatorsAll;

    @Produces
    @Named
    public List<SimulatorInst> getSimulatorsAll() {
        return simulatorsAll;
    }    

    @PostConstruct
    public void retrieveAllSimulators() {
    	
    	log.log(Level.INFO," find All simulators ");
    	this.simulatorsAll= pmanager.findSimulatorsAll();
        
    }
    
    @Produces
    @Named
    public List<String> getEurostagVersions() {
    	List<String> eurostagVersions = new ArrayList<String>();
    	for(SimulatorInst sim : simulatorsAll) {
    		if ( sim.getSimulator() == Simulator.EUROSTAG )
    			eurostagVersions.add(sim.getVersion());
    	}
    	return eurostagVersions;
    }
    
    @Produces
    @Named
    public List<String> getModelicaVersions() {
    	List<String> modelicaVersions = new ArrayList<String>();
    	for(SimulatorInst sim : simulatorsAll) {
    		if ( sim.getSimulator() == Simulator.MODELICA )
    			modelicaVersions.add(sim.getVersion());
    	}
    	return modelicaVersions;
    }
}
