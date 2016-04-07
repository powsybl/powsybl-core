/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Model
public class EditController {
	
	@Inject
	private Logger log;

	@Inject
	private FacesContext facesContext;
	
	@Produces
	@Named 
	private ModelTemplateContainer modelTemplCont;
		
	@Produces
	@Named
	private ModelTemplate modelTempl;
	
	@EJB
	private DDBManager pmanager;
	
	private boolean showNewMt;

	
	public void initEdit() {
		log.info("initEdit ....");
		this.modelTemplCont = new ModelTemplateContainer("", "");
		this.modelTempl = new ModelTemplate();
		this.setShowNewMt(false);	
	}
	
	public ModelTemplateContainer getModelTemplCont() {
		return this.modelTemplCont;
	}

	public void setModelTemplCont(	ModelTemplateContainer _mtc) {
		this.modelTemplCont = _mtc;
	}

	public ModelTemplate getModelTempl() {
		return this.modelTempl;
	}

	public void setModelTempl(	ModelTemplate _mt) {
		this.modelTempl = _mt;
	}
	
	public boolean getShowNewMt() {
		return this.showNewMt;
	}

	public void setShowNewMt(boolean _show) {
		this.showNewMt = _show;
	}
	
	
	public String edit(ModelTemplateContainer mtc) {

		log.info(" edit enter::  mtc.ddbId: "+mtc.getDdbId() + " "+mtc.getComment());
		
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");
		
		this.modelTemplCont = mtc;	
		try {
			if (modelTemplCont != null) return "edit";
			else throw new Exception("Edit:modelTemplCont IS NULL");
			} 
		catch (Exception e) 
		{
			log.log(Level.WARNING,"Edit: Edit:modelTemplCont IS NULL"); 
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("edit.success.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}
	

	public String delete() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try 
		{
			if (modelTemplCont != null) 
			{
				log.log(Level.INFO,"Delete model template container :["+ modelTemplCont.getId() +" " +modelTemplCont.getDdbId() + " "+ modelTemplCont.getComment()+"]");
				this.modelTemplCont = pmanager.findModelTemplateContainer(modelTemplCont.getDdbId() );
				pmanager.delete(this.modelTemplCont);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, modelTemplCont.getDdbId()+ " "+bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));
				facesContext.addMessage(null, m);
				return "list";
			} else	throw new Exception("Delete: modelTemplCont IS NULL");

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
	
	
	public void deleteModelTemplateT(ModelTemplate modelTemplate) {
		//System.out.println("deleteMT enter:: ");
	}

	
	public String update() {	
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try
		{
			if (modelTemplCont != null) 
			{
				log.log(Level.INFO,"update: Model Template Controller: ["+ modelTemplCont.getId() +" " +modelTemplCont.getDdbId() + " "+ modelTemplCont.getComment()+"]");
				pmanager.save(modelTemplCont);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("update.operation.msg"), bundle.getString("update.success.msg"));
				facesContext.addMessage(null, m);
				return "edit";
			} 
			else	throw new Exception("update: newModelTemplateContainer IS NULL");

		} 
		catch (Exception e) {
			log.log(Level.WARNING,"update:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,  bundle.getString("update.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}

	
	public void displayNewMT(){
		this.showNewMt=true;		
	}
	
	
	public void addModelTemplate() throws Exception {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");		
		try {
			log.log(Level.INFO," addModelTemplate enter:: "+modelTempl.getComment() );
			modelTemplCont.getModelTemplates().add(modelTempl);
			pmanager.save(modelTemplCont);
			modelTempl = new ModelTemplate();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("add.operation.msg"), bundle.getString("add.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO," model template added: ;");
		} catch (Exception e) {
			log.log(Level.WARNING,"Error ---");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
			
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
