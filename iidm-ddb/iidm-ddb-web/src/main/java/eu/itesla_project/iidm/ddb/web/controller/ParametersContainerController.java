/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.LazyEquipmentDataModel;
import eu.itesla_project.iidm.ddb.web.data.LazyParametersContainerDataModel;
import eu.itesla_project.iidm.ddb.web.data.ParameterWeb;
import eu.itesla_project.iidm.ddb.web.util.Sorter;

// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@ManagedBean
@ViewScoped
public class ParametersContainerController {
	
	@Inject
	private Logger log;
	private Map<SimulatorInst,List<ParameterWeb>> simulatorParams;	
	private List<Parameters> sortedParamList;
	
	@Inject
	private FacesContext facesContext;
	
	@Produces
	@Named
	private ParametersContainer parametersContainer;
	
	@Produces
	@Named
	private Parameters newParameters;
	
	  	@EJB
	private DDBManager pmanager;
	
	private LazyParametersContainerDataModel lazyDataModel;

	private int parametersCount;
	
	private String ddbId;
	
	@PostConstruct
	public void initParametersContainerController() {
		log.info("initParametersContainerController ");
		this.parametersContainer = new ParametersContainer("");
		this.newParameters= new Parameters();
		lazyDataModel=new LazyParametersContainerDataModel(pmanager);
		parametersCount=lazyDataModel.getRowCount();
		
	}

	
	
	
	public LazyParametersContainerDataModel getLazyDataModel() {
		return lazyDataModel;
	}




	public void setLazyDataModel(LazyParametersContainerDataModel lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}




	public int getParametersCount() {
		return parametersCount;
	}




	public void setParametersCount(int parametersCount) {
		this.parametersCount = parametersCount;
	}

	



	public String getDdbId() {
		return ddbId;
	}




	public void setDdbId(String ddbId) {
		this.ddbId = ddbId;
		if(ddbId!= null)
		{
			parametersContainer=pmanager.findParametersContainer(ddbId);
			this.simulatorParams=  new Hashtable<SimulatorInst, List<ParameterWeb>>();
			if(parametersContainer!=null)
				for (Parameters pc :this.parametersContainer.getParameters() ){
						
					SimulatorInst simulInst =pc.getSimulator();	
					List<ParameterWeb> pwList = new ArrayList<ParameterWeb>();
					
					for (Parameter  p: pc.getParameters()){
						log.log(Level.INFO," _params [Name: " +p.getName() + ", Value: "+p.getValue() + "]");
						ParameterWeb pw = new ParameterWeb(p.getName(), p.getValue());
						pwList.add(pw);
					}	
					
					simulatorParams.put(simulInst, pwList);			
				}
			
		}
	}




	public ParametersContainer getParametersContainer() {
		return this.parametersContainer;
	}

	public void setParametersContainer(	ParametersContainer _parametersContainer) {
		this.parametersContainer = _parametersContainer;
	}

	public List<SimulatorInst> getSimulatorParamsKeys() {
        List<SimulatorInst> keys = new ArrayList<SimulatorInst>();
        keys.addAll(simulatorParams.keySet());
        return keys;
    }
		
	
	public List<Parameters> getSortedParamList() {
		return sortedParamList;
	}

	public void setSortedParamList(List<Parameters> sortedParamList) {
		this.sortedParamList = sortedParamList;
	}
	
	public Map<SimulatorInst,List<ParameterWeb>> getSimulatorParams() {
		return simulatorParams;
	}

	public void setSimulatorParams(Map<SimulatorInst,List<ParameterWeb>> simulatorParams) {
		this.simulatorParams = simulatorParams;
	}
	
	public Parameters getNewParameters() {
		return newParameters;
	}

	public void setNewParameters(Parameters parameters) {
		this.newParameters = parameters;
	}
	
	public String create() throws Exception {
		
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		try {
			
			log.log(Level.INFO," Create add ParametersContainer: [ddbId:" +parametersContainer.getDdbId() +"] " +
					"						- [Parameters [defParamSetNum "+newParameters.getDefParamSetNum() + " simulator "+newParameters.getSimulator()+"]");
			
			parametersContainer.getParameters().add(this.newParameters);
			parametersContainer=pmanager.save(parametersContainer);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("create.operation.msg"), bundle.getString("create.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO,"New parametersContainer added: ["+ parametersContainer.getId() +" " +parametersContainer.getDdbId() +"]");
			return "list?faces-redirect=true&includeViewParams=true";
			
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during creation of parametersContainer: ["+ parametersContainer.getId() +" " +parametersContainer.getDdbId() +"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("create.failure.msg"));
			facesContext.addMessage(null, m);
			return "create";
		}
	}
	
	
	public String view(String ddbId) {
		log.log(Level.INFO,"view:: enter  parameter ddbId: " + ddbId);
		this.parametersContainer = pmanager.findParametersContainer(ddbId);
		this.ddbId=ddbId;
		// List<Map> getSimulatorParams() 
		
		this.simulatorParams=  new Hashtable<SimulatorInst, List<ParameterWeb>>();

		for (Parameters pc :this.parametersContainer.getParameters() ){
				
			SimulatorInst simulInst =pc.getSimulator();	
			List<ParameterWeb> pwList = new ArrayList<ParameterWeb>();
			
			for (Parameter  p: pc.getParameters()){
				log.log(Level.INFO," _params [Name: " +p.getName() + ", Value: "+p.getValue() + "]");
				ParameterWeb pw = new ParameterWeb(p.getName(), p.getValue());
				pwList.add(pw);
			}	
			
			simulatorParams.put(simulInst, pwList);			
		}
		
		
		log.log(Level.INFO,"view:: for  parameters container: ["+ parametersContainer.getId() +" " +parametersContainer.getDdbId() + " ]");
		return "/parametersContainer/details?faces-redirect=true&includeViewParams=true";
		
	}


	public String edit(String ddbId) {
		log.log(Level.INFO," edit enter:: parameter ddbId: " + ddbId);
		log.log(Level.INFO," Create add ParametersContainer: [ddbId:" +parametersContainer.getDdbId() +"] " +
				"						- [Parameters [defParamSetNum "+newParameters.getDefParamSetNum() + " simulator "+newParameters.getSimulator()+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		this.ddbId=ddbId;
		this.parametersContainer = pmanager.findParametersContainer(ddbId);	
		try {
			if (parametersContainer != null) {
				this.sortParameters();
				return "edit?faces-redirect=true&includeViewParams=true";
			}
			else 	throw new Exception("Edit:parametersContainer not found!");
			
		} catch (Exception e) {
			log.log(Level.WARNING,"Edit: Edit: parametersContainer not found"); 
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("edit.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";		
		}
	}
	

	public String delete() {
		log.log(Level.INFO," delete enter ::");
		log.log(Level.INFO," ParametersContainer: [ddbId:" +parametersContainer.getDdbId() +"] " +
				"		- [Parameters [defParamSetNum "+newParameters.getDefParamSetNum() + " simulator "+newParameters.getSimulator()+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		try
		{
			this.parametersContainer = pmanager.findParametersContainer(parametersContainer.getDdbId());
			if (parametersContainer != null) 
			{
				log.log(Level.INFO,"Delete parametersContainer :["+ parametersContainer.getId() +" " +parametersContainer.getDdbId() +"]");
				pmanager.delete(this.parametersContainer);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,	parametersContainer.getDdbId()+ " "+bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));
				facesContext.addMessage(null, m);
				return "list?faces-redirect=true";
			} else 
				throw new Exception("Delete: parametersContainer not found");
		} 
		catch (Exception e) 
		{
			log.log(Level.WARNING," delete:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}

	
	public void addParameters()  {
		
		log.log(Level.INFO," addParameters enter ::");
		log.log(Level.INFO," ParametersContainer: [ddbId:" +parametersContainer.getDdbId() +"] - [Parameters [defParamSetNum "+newParameters.getDefParamSetNum() + " simulator "+newParameters.getSimulator()+"]");
		
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
					
		try
		{
			this.parametersContainer = pmanager.findParametersContainer(parametersContainer.getDdbId());
			List<Parameters> parametersList=parametersContainer.getParameters();
			for (Parameters p :parametersList){
				if (p.getSimulator().equals(newParameters.getSimulator() )){
					throw new Exception ("Parameters is already present!");
				}
			}
			
			parametersContainer.getParameters().add(newParameters);
			pmanager.save(parametersContainer);
			newParameters = new Parameters();
			this.parametersContainer = pmanager.findParametersContainer(parametersContainer.getDdbId());
			if (parametersContainer != null) 	this.sortParameters();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("add.operation.msg"), bundle.getString("add.success.msg"));
			facesContext.addMessage(null, m);
		} 
		catch (Exception e)
		{
			log.log(Level.WARNING,"Error :"+ e.getMessage());
			e.printStackTrace();
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
			
		}
	}
	
	private void sortParameters(){
		List<Parameters> paramList=this.parametersContainer.getParameters();
		Collections.sort(paramList,Sorter.compareParamSetNum);  
		this.sortedParamList=paramList;
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
