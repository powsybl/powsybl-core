/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.RowEditEvent;

import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterBoolean;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.ParameterInteger;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.ParameterWeb;

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
public class ParametersController {
	
	@Inject
	private Logger log;

	@Inject
	private FacesContext facesContext;		
	
	@Produces
	@Named
	private Parameters parameters;
	
	private Long currentId;			 	// id Parameter
	
	private String currentddbid; 		// ddbid ParametersContainer
	
    private ParameterWeb parameterWeb =new ParameterWeb();  

	private List<ParameterWeb> addedParams =new ArrayList<ParameterWeb>(); 
	
	private List<ParameterWeb> dbParams = new ArrayList<ParameterWeb>();

	private String paramType;
	
	private String paramName;
	
	private String paramValue;
	
	private ParameterWeb selectedParam;
	
	@Inject
	private EntityManager em;
		
	@EJB
	private DDBManager pmanager;
	
	public void initParametersContainer() {
		log.info("initParametersContainer ");
		this.parameters= new Parameters();
	}
		
	public Long getCurrentId() {
		return currentId;
	}

	//currentID pk of parametersTable
	public void setCurrentId(Long currentId) {
		log.log(Level.INFO,"setCurrentId:: enter  parameter currentId: "+currentId);

		this.currentId = currentId;

		TypedQuery<Parameters> query = em.createQuery(
				"SELECT p FROM Parameters p WHERE p.id = :arg1",
				Parameters.class);
		query.setParameter("arg1", currentId);
		List<Parameters> res = query.getResultList();
		this.parameters= res.size() > 0 ? res.get(0) : null;
		this.dbParams=buildParamsEditableTable(parameters);
	}
	
	
	public String getCurrentddbid() {
		return currentddbid;
	}


	public void setCurrentddbid(String _containerddbid) {
		this.currentddbid = _containerddbid;
	}
	
	
	public EntityManager getEm() {
		return em;
	}

	
	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	
	public ParameterWeb getSelectedParam() {
		return selectedParam;
	}


	public void setSelectedParam(ParameterWeb selectedParam) {
		this.selectedParam = selectedParam;
	}

	
	public List<ParameterWeb> getDbParams() {
		return dbParams;
	}


	public void setDbParams(List<ParameterWeb> dbParams) {
		this.dbParams = dbParams;
	}


	public ParameterWeb getParameterWeb() {
		return parameterWeb;
	}

	
	public void setParameterWeb(ParameterWeb parameterWeb) {
		this.parameterWeb = parameterWeb;
	}
	
	
	public List<ParameterWeb> getAddedParams() {
		return addedParams;
	}
	

	public void setAddedParams(List<ParameterWeb> addedParams) {
		this.addedParams = addedParams;
	}
		
		
	public String getParamType() {
		return paramType;
	}

	
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	
	public String getParamName() {
		return paramName;
	}

	
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	
	public String getParamValue() {
		return paramValue;
	}

	
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

		
	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	
	public String detailParameters(Parameters parameters)  {
		log.log(Level.INFO,"detailParameters:: enter  parameter ddbId:  MT  ID"+parameters.getId()+ " Simulator" +parameters.getSimulator());
		this.parameters=parameters;
		log.log(Level.INFO,"detailParameters:: enter  MT  ID"+parameters.getId()+ " Simulator:" +parameters.getSimulator() +" derfParamSetNum: "+parameters.getDefParamSetNum());
		return "/parameters/details";
		}

	
	public void save(){
		log.log(Level.INFO," Save enter ::");
		log.log(Level.INFO," ParametersContainer: [ddbId:" +this.currentddbid +"] " +
				"		- [Parameters [defParamSetNum "+parameters.getDefParamSetNum() + " simulator "+parameters.getSimulator()+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			
			ParametersContainer paramitersContainer = pmanager.findParametersContainer(this.currentddbid); 
			List<Parameters> parametersList = paramitersContainer.getParameters();
			for (Parameters pt : parametersList) {
				if (pt.getId().compareTo(currentId) == 0) {
					this.parameters = pt;	
					
				}
			}
			
			for (ParameterWeb pw : addedParams) {
				log.log(Level.INFO,"parameterAdded [Name: "+pw.getName()+ ", Value: "+ pw.getValue() + ", Type: "+pw.getType() + "]");
				if (pw.getValue()!= null) {
					
					switch (pw.getType()) {
						case "Integer": 
							this.parameters.addParameter(new ParameterInteger(pw.getName(),Integer.valueOf(pw.getValue().toString()))); 
							break;
						case "Float":
							this.parameters.addParameter(new ParameterFloat(pw.getName(),Float.valueOf(pw.getValue().toString())));
							break;
						case "Boolean":	
							this.parameters.addParameter(new ParameterBoolean(pw.getName(),Boolean.valueOf(pw.getValue().toString())));
							break;
						case "String":	
							this.parameters.addParameter(new ParameterString(pw.getName(),pw.getValue().toString()));
							break;
				    default: throw new RuntimeException(pw.getType() + " not supported.");
					}
					pmanager.save(this.parameters);
				}
				else{
					log.log(Level.INFO,"parameterAdded has a NULL value will be rejected!");
				}
			}
			
			this.dbParams=this.buildParamsEditableTable(this.parameters);
			this.addedParams=new ArrayList<ParameterWeb>();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,  "Parameters  "+bundle.getString("add.operation.msg"), bundle.getString("add.success.msg"));
			facesContext.addMessage(null, m);			
			
		} catch (Exception e) {
			log.log(Level.WARNING,"Error :"+ e.getMessage());
			e.printStackTrace();
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
		}
		
	}
	
	public String delete(){
		log.log(Level.INFO, "Delete: [ parametersId: " +this.parameters.getId() + " "+"defParamSetNum: "+this.parameters.getDefParamSetNum() +"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			ParametersContainer paramitersContainer = pmanager.findParametersContainer(this.currentddbid); 
			List<Parameters> parametersList = paramitersContainer.getParameters();
			ArrayList<Parameters> parametersUpdated = new ArrayList<Parameters>();
			for (Parameters ps : parametersList) {
				if (ps.getId().compareTo(this.parameters.getId()) == 0) {
					this.parameters = ps;
				}else 
					parametersUpdated.add(ps);
			}

			paramitersContainer.setParameters(parametersUpdated);		
			paramitersContainer = pmanager.save(paramitersContainer);
				
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,  "Parameters  "+bundle.getString("delete.operation.msg"), bundle.getString("delete.success.msg"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "../parametersContainer/list.jsf";
		
		} catch (Exception ex) {
			String errorMessage = getRootErrorMessage(ex);
			log.log(Level.WARNING,"Error  "+errorMessage); 
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
			
		}
	}

	public void update(){
		log.log(Level.INFO, "update: [ id: " +this.parameters.getId() + " "+"defParamSetNum: "+this.parameters.getDefParamSetNum() +"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try
		{
			this.parameters=pmanager.save(parameters);
			FacesMessage msg = new FacesMessage("Parameters succesfully");
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("update.operation.msg")	,bundle.getString("update.success.msg"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (Exception ex) 
		{
			String errorMessage = getRootErrorMessage(ex);
			log.log(Level.WARNING,"Error  "+errorMessage); 
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,bundle.getString("update.failure.msg"));
			facesContext.addMessage(null, m);		
		}		
	}
	
	
	public void addSimulatorParameters()  {
		
		log.log(Level.INFO," addParameters enter ::");
		log.log(Level.INFO," ParametersContainer: [ddbId:" +this.currentddbid +"] " +
				"		- [Parameters [defParamSetNum "+parameters.getDefParamSetNum() + " simulator "+parameters.getSimulator()+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			
			ParametersContainer parametersContainer = pmanager.findParametersContainer(this.currentddbid);			
			List<Parameters> parametersList=parametersContainer.getParameters();			
			for (Parameters p :parametersList)
			{
				if (p.getSimulator().equals(parameters.getSimulator() ))
					throw new Exception ("Parameters for "+parameters.getSimulator() +" is already present!");
			}
			parametersContainer.getParameters().add(parameters);
			pmanager.save(parametersContainer);
			// form clean
			reinit();			
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
	

	public void onRowEdit(RowEditEvent event) {
		log.log(Level.INFO, "  onRowEdit::  ParameteContainer: [ ddbid= "+this.currentddbid +"]  Parameters per simulatore [id: "+ this.currentId+"]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try 
		{
			//retrieve container Instance
			ParametersContainer paramitersContainer = pmanager.findParametersContainer(this.currentddbid);
			// one list for each simulator
			List<Parameters> parametersContainerList = paramitersContainer.getParameters();
			boolean findParameters=false;
			int index=0;
			// retrieve simulator parameter to update
			for (Parameters pt : parametersContainerList) 
			{
				
				log.log(Level.INFO, " parameter id "+ pt.getId() +" simulator "+ pt.getSimulator());
				if (pt.getId().compareTo(currentId) == 0) 
				{
					this.parameters = pt;
					findParameters=true;
					log.log(Level.INFO, " parameters found id: "+this.parameters.getId() + " simulator "+this.parameters.getSimulator().toString() +" index "+ index);
					break;
				}
				index++;
			}
			
			if (findParameters)
			{
				ParameterWeb paramSelected = (ParameterWeb) event.getObject();
				Parameter paramToUpdate = null;
				log.log(Level.INFO, " rowSeleted [Name: "+paramSelected.getName() +",  Value:"+ paramSelected.getValue() + ", Type: "+paramSelected.getType() +"]");
				
				if (this.parameters.containsParameterWithName(paramSelected.getName())) 
				{
					List<Parameter> paramNameValueList = this.parameters.getParameters();	
					for (Parameter pvl : paramNameValueList) 
					{
						log.log(Level.INFO, " pvl [name: "+pvl.getName() + ", value:  "+ pvl.getValue() +", id: "+pvl.getId()+"]");
						if (pvl.getName().equals(paramSelected.getName())) 
						{						
							paramToUpdate = pvl;
							log.log(Level.INFO, " Trovato ");
						}
					}
					
					if (paramToUpdate!= null)  
					{
						switch (paramSelected.getType()) {
						case "Float":
							((ParameterFloat) paramToUpdate).setValue(Float.valueOf(paramSelected.getValue().toString()));
							break;
						case "Integer":
							((ParameterInteger) paramToUpdate).setValue(Integer.valueOf(paramSelected.getValue().toString()));
							break;
						case "Boolean":
							((ParameterBoolean) paramToUpdate).setValue(Boolean.valueOf(paramSelected.getValue().toString()));
							break;
						case "String":
							((ParameterString) paramToUpdate).setValue(String.valueOf(paramSelected.getValue().toString()));
							break;
						default:
							throw new RuntimeException(paramSelected.getType() + " not supported.");
					}

					paramToUpdate=pmanager.save(paramToUpdate);
					dbParams=buildParamsEditableTable(this.parameters);
	  				reinit();
	  				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Parameter "+bundle.getString("update.operation.msg"), bundle.getString("update.success.msg"));
					facesContext.addMessage(null, m);
				}
			}
			else log.log(Level.WARNING, "There aren't any paramater to update:" );
		}
	} 
	catch (Exception e) {
		log.log(Level.WARNING, "Error :" + e.getMessage());
		e.printStackTrace();
		String errorMessage = getRootErrorMessage(e);
		FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, 	errorMessage, bundle.getString("update.failure.msg"));
		facesContext.addMessage(null, m);
		}
	}
	
	//triggered when user select icon x on parameterEditTable     
    public String onRowCancel(RowEditEvent event) {
    	log.log(Level.INFO, "  onRowCancel enter: "); 
    	FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    	try 
    	{
    		// get Parameters Container
  			ParametersContainer paramitersContainer = pmanager.findParametersContainer(this.currentddbid); 
  			List<Parameters> parametersContainerList = paramitersContainer.getParameters();
  			for (Parameters pt : parametersContainerList) 
  			{	
  				if (pt.getId().compareTo(currentId) == 0) this.parameters = pt;
  			}  					
  			
  			ParameterWeb paramSelected=(ParameterWeb) event.getObject();
  			//Parameter paramToCancel= null;
  			log.log(Level.INFO, "  onRowCancel Parameter [Name: "+paramSelected.getName() + ", Value: "+paramSelected.getValue() + ",Type:  "+ paramSelected.getType());
  			if (this.parameters.containsParameterWithName(paramSelected.getName())) 
  			{
  				ArrayList<Parameter> parametersToUpdate = new ArrayList<Parameter>();
  				List<Parameter> paramNameValueList=this.parameters.getParameters();
  				for (Parameter pvl : paramNameValueList) 
  				{
  					if (!pvl.getName().equals(paramSelected.getName()))
  						parametersToUpdate.add(pvl);
  	  			}  				
  				this.parameters.setParameters(parametersToUpdate);
  				this.parameters=pmanager.save(this.parameters);
  				log.log(Level.FINEST, "  parameters saved! ");
  				
  				//for (Parameter parameter : this.parameters.getParameters()) {
					//log.log(Level.INFO, "  param name "+ parameter.getName() +" = " + parameter.getValue() );
				//}
  				/**log.log(Level.INFO, "  after removing par end");
  				//if (this.parameters.getParameters().isEmpty()) {
  					log.log(Level.INFO,"-- lista vuota --");
  					 UIComponent lComp = event.getComponent();
  				     if ( lComp instanceof DataTable )  {
  				    	  DataTable lDataTable = (DataTable)lComp;
  				    	log.log(Level.INFO,"- data table  --"+lDataTable.getRowsToRender());
  				    	  lDataTable.setsetRows(0);
  				     }
  					
  				}*/
  				this.dbParams=buildParamsEditableTable(this.parameters);
  				log.log(Level.INFO, "  after removing par end -  dbParams " + this.dbParams.size());
  				reinit();  				
  				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, ((ParameterWeb) event.getObject()).getName() +" "+bundle.getString("delete.operation.msg"), bundle.getString("delete.success.msg"));
  				facesContext.addMessage(null, m);
  			}
  		} 
    	catch (Exception e) 
    	{
  			log.log(Level.WARNING,"Error :"+ e.getMessage());
  			e.printStackTrace();
  			String errorMessage = getRootErrorMessage(e);
  			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
  			facesContext.addMessage(null, m);
  		}
		return "edit";
      }  
	
    
    public String reinit() {  
		log.log(Level.INFO," reint enter::") ;
		parameterWeb = new ParameterWeb("","","");
		return null;  
	}
    
	private List<ParameterWeb> buildParamsEditableTable(Parameters _params){
		log.log(Level.INFO," buildParamsEditableTable enter::") ;
		List<ParameterWeb> pwList = new ArrayList<ParameterWeb>();
		for (Parameter  p:_params.getParameters())
		{
			log.log(Level.INFO," _params [Name: " +p.getName() + ", Value: "+p.getValue() + "]");
			ParameterWeb pw = new ParameterWeb();
			pw.setName(p.getName());
			Object pValue=p.getValue();
			pw.setValue(pValue);
			
			if (pValue instanceof java.lang.String) 	
				pw.setType("String");
			else if (pValue instanceof java.lang.Float) 
				pw.setType("Float");
			else if (pValue instanceof java.lang.Integer)
				pw.setType("Integer");
			else if (pValue instanceof java.lang.Boolean)
				pw.setType("Boolean");
			else pw.setType("Object");
			
			pwList.add(pw);
		}
		return pwList;
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
