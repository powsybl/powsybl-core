/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

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
import javax.persistence.EntityManager;
import javax.persistence.Query;

import eu.itesla_project.iidm.ddb.eurostag_modelica.Converter;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.LazyInternalDataModel;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
@ManagedBean
@ViewScoped
public class InternalController {

   @Inject
   private FacesContext facesContext;

   @EJB
   private DDBManager pmanager;
   
   @Inject
   private Logger log;

   private Internal newInternal;
   
   private String nativeId;
   
   private int internalsCount;
   
   private LazyInternalDataModel lazyDataModel; 
   
   private String eurostagVersion;
   private String modelicaVersion;
   
   private List<String> modelTemplateContainersValues;
   private String selectedModelTemplateContainer;
   
   private List<String> parametersContainerValues;
   private String selectedParametersContainer;
   
   
   
   @Inject
   private EntityManager em;
   
   
    
   public EntityManager getEm() {
	return em;
   }




public void setEm(EntityManager em) {
	this.em = em;
}





public String getSelectedParametersContainer() {
	return selectedParametersContainer;
}




public void setSelectedParametersContainer(String selectedParametersContainer) {
	this.selectedParametersContainer = selectedParametersContainer;
}




public List<String> getModelTemplateContainersValues() {
	return modelTemplateContainersValues;
}




public void setModelTemplateContainersValues(
		List<String> modelTemplateContainersValues) {
	this.modelTemplateContainersValues = modelTemplateContainersValues;
}




public String getSelectedModelTemplateContainer() {
	return selectedModelTemplateContainer;
}




public void setSelectedModelTemplateContainer(
		String selectedModelTemplateContainer) {
	this.selectedModelTemplateContainer = selectedModelTemplateContainer;
}




//@Produces
   @Named
   public Internal getNewInternal() {
      return newInternal;
   }




   public LazyInternalDataModel getLazyDataModel() {
	return lazyDataModel;
	}
	
	
	
	
	public void setLazyDataModel(LazyInternalDataModel lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}

	



	public List<String> getParametersContainerValues() {
		return parametersContainerValues;
	}




	public void setParametersContainerValues(List<String> parametersContainerValues) {
		this.parametersContainerValues = parametersContainerValues;
	}




@PostConstruct
   public void initInternal() {
	   log.log(Level.INFO, " initInternal enter:: ");
	   lazyDataModel=new LazyInternalDataModel(pmanager);
	   internalsCount=pmanager.findInternalsAllCount();
	   
	   String paramInternal=null;
	   FacesContext ctx = FacesContext.getCurrentInstance();
	   Map<String, String> parameters = ctx.getExternalContext().getRequestParameterMap();
	   if (parameters.containsKey("nativeId")) {
		   paramInternal = (String)parameters.get("nativeId");
		   log.log(Level.INFO, " param :: "+paramInternal);
	   }
	   
	   if (paramInternal == null) 	   newInternal = new Internal("");
	   else
	   this.newInternal=pmanager.findInternal(paramInternal);
	   if (this.newInternal!= null) log.log(Level.INFO, " newInternal :: "+this.newInternal.getNativeId());
	   log.log(Level.INFO, " query ModelTemplateContainer");
	   Query q = em.createQuery("SELECT m.ddbId FROM ModelTemplateContainer m order by m.ddbId");
	   modelTemplateContainersValues = q.getResultList();
	   log.log(Level.INFO, "DONE query ModelTemplateContainer");
	   log.log(Level.INFO, " query parameterContainer");
	   Query q2 = em.createQuery("SELECT m.ddbId FROM ParametersContainer m order by m.ddbId");
	   parametersContainerValues = q2.getResultList();
	   log.log(Level.INFO, "DONE query parameterContainer");
	}
  
   public String getNativeId() {
		return nativeId;
	}

   
   
  
	public int getInternalsCount() {
	return internalsCount;
	}
	
	public void setInternalsCount(int internalsCount) {
		this.internalsCount = internalsCount;
	}

	public void setNativeId(String currentNativeId) {
		this.nativeId = currentNativeId;
		if(nativeId != null)
			this.newInternal=pmanager.findInternal(nativeId);
	}
  
   public String create() throws Exception {
	    log.log(Level.INFO," Create new Internal: [nativeId: " +newInternal.getNativeId() 
				+"  Model Container DDBID: "+ selectedModelTemplateContainer 
				+"  Parameter Container: "+selectedParametersContainer +"]");
		
	    ModelTemplateContainer mc=pmanager.findModelTemplateContainer(this.selectedModelTemplateContainer);
	    newInternal.setModelContainer(mc);
	    
	    ParametersContainer pc=pmanager.findParametersContainer(this.selectedParametersContainer);
	    newInternal.setParametersContainer(pc);
	    
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		try {
			pmanager.save(this.newInternal);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("create.operation.msg"), bundle.getString("create.success.msg"));
			facesContext.addMessage(null, m);
			return "list?faces-redirect=true";
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during creation of ["+ newInternal.getNativeId()+"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("create.failure.msg"));
			facesContext.addMessage(null, m);
			return "create";
		}
	}
   
  
   public String delete(String nativeId) throws Exception {
	   log.log(Level.INFO," Delete Internal: [nativeId: " +nativeId +"]");
	   
	   FacesContext context = FacesContext.getCurrentInstance();
	   ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
 		try {
 			this.newInternal=pmanager.findInternal(nativeId);													
 			pmanager.delete(this.newInternal);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("delete.operation.msg"), bundle.getString("delete.success.msg"));
 			facesContext.addMessage(null, m);
 			
 			return "list?faces-redirect=true";
 		} catch (Exception e) {
 			log.log(Level.WARNING,"Error during delete of ["+ newInternal.getNativeId()+"]");
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
 			facesContext.addMessage(null, m);
 			return "edit";
 		}
 	}
    
    public String edit(Internal internal) {
    	log.log(Level.INFO," edit enter:: ["+internal.getNativeId()+"]");
 		this.nativeId=internal.getNativeId();
    	FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
 		try {
 			this.newInternal=pmanager.findInternal(internal.getNativeId());
 			if (internal != null) {
 				log.log(Level.INFO,"Edit Internal : ["+ internal.getNativeId()+"]");
 				return "edit?faces-redirect=true&includeViewParams=true";
 			}				
 			else {
 				throw new Exception("Edit: Internal not found!");
 			}

 		} catch (Exception e) {
 			log.log(Level.WARNING,"edit Internal:: catch an Exception" + e.getMessage());
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("edit.failure.msg"));
 			facesContext.addMessage(null, m);
 			return "edit";
 		}
 	}
    
    public String getEurostagVersion() {
		return eurostagVersion;
	}

	public void setEurostagVersion(String eurostagVersion) {
		this.eurostagVersion = eurostagVersion;
	}

	public String getModelicaVersion() {
		return modelicaVersion;
	}

	public void setModelicaVersion(String modelicaVersion) {
		this.modelicaVersion = modelicaVersion;
	}

	public void convertToModelica(Internal internal) {
    	log.log(Level.INFO,"convertToModelica enter:: ["+internal.getNativeId()+"]");
    	log.log(Level.INFO,"convertToModelica versions ["+eurostagVersion+","+modelicaVersion+"]");
 		
    	FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
 		try {
			log.log(Level.INFO,"convertToModelica Internal : ["+ internal.getNativeId()+"]");
			Converter eurostagModelicaConverter = new Converter(pmanager, eurostagVersion, modelicaVersion);
			eurostagModelicaConverter.convertAndSaveInternal(internal.getNativeId(), true);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("convert.operation.msg"), bundle.getString("convert.success.msg"));
 			facesContext.addMessage(null, m);
 			newInternal=pmanager.findInternal(internal.getNativeId());
			//return "/internals/list";
 		} catch (Exception e) {
 			log.log(Level.WARNING,"convertToModelica Internal:: catch an Exception" + e.getMessage());
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("convert.failure.msg"));
 			facesContext.addMessage(null, m);
 			
 			newInternal=pmanager.findInternal(internal.getNativeId());
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
