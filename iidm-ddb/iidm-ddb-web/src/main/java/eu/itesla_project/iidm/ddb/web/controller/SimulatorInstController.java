/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
@Model
public class SimulatorInstController {
	
	@Inject
	private Logger log;

	@Inject
	private FacesContext facesContext;
	
	@Produces
	@Named
	private SimulatorInst simulatorInst;
	
	@Produces
	@Named
	private Simulator[] simulatorValues; 	
		
	@EJB
	private DDBManager pmanager;

	@PostConstruct
	public void initSimulatorInst() {
		log.log(Level.INFO ,"initSimulatorInst:: enter");
		simulatorInst = new SimulatorInst();
		setSimulatorValues();
	}

	public SimulatorInst getSimulatorInst() {
		return this.simulatorInst;
	}

	public void setSimulatorInst(SimulatorInst _simulatorInst) {
		this.simulatorInst = _simulatorInst;
	}

	public void setSimulatorValues() {		
		this.simulatorValues=Simulator.values();		
	}
	
	public Simulator[] getSimulatorValues() {		
		return this.simulatorValues;		
	}
	
	public String create() throws Exception {
		log.log(Level.INFO," create enter:");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			log.log(Level.INFO," create enter version: "+simulatorInst.getVersion() +" id: " +simulatorInst.getId() + " simulator  "+simulatorInst.getSimulator());
			pmanager.save(simulatorInst);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("create.operation.msg"),bundle.getString("create.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO,"New simulator added: [id:"+ simulatorInst.getId() +" simulator:" +simulatorInst.getSimulator().name() + "Version: "+ simulatorInst.getVersion() +"]");
			// initSimulatorInst();
			return "list";
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during creation of: [id:"+ simulatorInst.getId() +" simulator" +simulatorInst.getSimulator().name() + " "+ simulatorInst.getVersion() +"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("create.failure.msg"));
			facesContext.addMessage(null, m);
			return "create";
		}
	}
	

	public String view(SimulatorInst _simulatorInst) {
		log.log(Level.INFO," view enter:: ["+_simulatorInst.toString()+"]");
		this.simulatorInst=pmanager.findSimulator(_simulatorInst.getSimulator(),_simulatorInst.getVersion());
		log.log(Level.INFO,"view:: for model simulator: ["+ simulatorInst.getId() +" " +simulatorInst.getSimulator().name() + " "+ simulatorInst.getVersion() +"]");
		return "details";
	}
	

	public String edit(SimulatorInst _simulatorInst) {
		log.log(Level.INFO," edit enter:: ["+_simulatorInst.toString()+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		this.simulatorInst=pmanager.findSimulator(_simulatorInst.getSimulator(),_simulatorInst.getVersion());
		try 
		{
			if (simulatorInst != null) {
				log.log(Level.INFO,"Edit SimulatorInst : ["+ simulatorInst.getId() +" " +simulatorInst.getSimulator().name() + " "+ simulatorInst.getVersion() +"]");
				return "edit";
			}	
			else throw new Exception("Edit: SimulatorInst not found!");
		} 
		catch (Exception e) {
			log.log(Level.WARNING,"edit simulator:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("edit.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}
	

	public String delete(SimulatorInst _simulatorInst) {
		log.log(Level.INFO," delete enter:: ["+_simulatorInst.toString()+"]");
		
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		SimulatorInst simInst=pmanager.findSimulator(_simulatorInst.getSimulator(),_simulatorInst.getVersion());
		try 
		{
			if (simInst != null)
			{
				log.log(Level.INFO,"Delete simulator :["+ simInst.getId() +" " +simInst.getSimulator().name() + " "+ simInst.getVersion() +"]");
				pmanager.delete(simInst);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,	simulatorInst.toString()+ " "+bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));  
				facesContext.addMessage(null, m);
				return "list";
			} 
			else throw new Exception("Delete: SimulatorInst not found");
		} 
		catch (Exception e) 
		{
			log.log(Level.WARNING," delete simulator:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}

	
	private String getRootErrorMessage(Exception e) {
		// Default to general error message that registration failed.
		String errorMessage = "Operation failed. See server log for more information";
		if (e == null) {
			// This shouldn't happen, but return the default messages
			return errorMessage;
		}

		// Start with the exception and recurse to find the root cause
		Throwable t = e;
		while (t != null) {

			// Get the message from the Throwable class instance
			errorMessage = t.getLocalizedMessage();
			t = t.getCause();
		}
		// This is the root cause message
		return errorMessage;
	}

	
	

	


}
