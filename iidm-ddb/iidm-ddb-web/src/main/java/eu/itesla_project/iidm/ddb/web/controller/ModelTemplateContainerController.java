/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
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

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.ModelData;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.LazyModelTemplateContainerDataModel;
import eu.itesla_project.iidm.ddb.web.data.LazyParametersContainerDataModel;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation

//@Model
@ManagedBean
@ViewScoped
public class ModelTemplateContainerController {
	
	@Inject
	private Logger log;

	@Inject
	private FacesContext facesContext;
	
	//@Produces
	@Named
	private ModelTemplateContainer modelTemplateContainer;
	
	//@Produces
	@Named
	private ModelTemplate modelTemplate;
	
	@Produces
	@Named
	private ArrayList<String> mtMapKeyList;
	
	//key=modelTemplate.id, List setNum
	private Map<ModelTemplate, List<Integer>> modelTemplateList;
	

	@Produces
	@Named
	private StreamedContent fileData;  		
	
	private UploadedFile uploadData;
	
	private String data;	
	
	@Produces
	private Map<String,ModelData> mtData;
	
	private Integer numDefaultParameters;

	private String currentddbid;
	
	@EJB
	private DDBManager pmanager;
	
	private LazyModelTemplateContainerDataModel lazyDataModel;

	private int modelTemplatesCount;
	
	private String ddbId;
	
	
	@PostConstruct
	public void initModelTemplateContainer() {
		log.info("initModelTemplateContainer enter "+this.currentddbid);
		this.modelTemplateContainer = new ModelTemplateContainer("", "");
		this.modelTemplate = new ModelTemplate();
		lazyDataModel=new LazyModelTemplateContainerDataModel(pmanager);
		modelTemplatesCount=lazyDataModel.getRowCount();
		
	/*	
		Map<String,String> params =   FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String action = params.get("action");
		this.currentddbid = params.get("currentddbid");
		log.info("action "+action + "currentddbid "+currentddbid);
		if (action != null ) 
		switch(action) {
	    	case "edit":
	    		this.edit(currentddbid);
	    		break;
	    	case "view":
	    		this.view(currentddbid);
	    		break;
		}*/
		log.info("initModelTemplateContainer out");
		
	}

	
	
	
	
	public LazyModelTemplateContainerDataModel getLazyDataModel() {
		return lazyDataModel;
	}





	public void setLazyDataModel(LazyModelTemplateContainerDataModel lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}





	public int getModelTemplatesCount() {
		return this.modelTemplatesCount;
	}





	public void setModelTemplatesCount(int modelTemplatesCount) {
		this.modelTemplatesCount = modelTemplatesCount;
	}





	public String getDdbId() {
		return ddbId;
	}





	public void setDdbId(String ddbId) {
		this.ddbId = ddbId;
		if(ddbId!= null)
		{
			this.modelTemplateContainer=pmanager.findModelTemplateContainer(ddbId);
		}
	}





	public Map<ModelTemplate, List<Integer>> getModelTemplateList() {
		return modelTemplateList;
	}

	public void setModelTemplateList(
			Map<ModelTemplate, List<Integer>> modelTemplateList) {
		this.modelTemplateList = modelTemplateList;
	}


	public Map<String,ModelData> getMtData() {
		return mtData;
	}

	public void setMtData(Map<String,ModelData> mtData) {
		this.mtData = mtData;
	}

	public ArrayList<String> getMtMapKeyList() {
		return mtMapKeyList;
	}

	public void setMtMapKeyList(ArrayList<String> mtMapKeyList) {
		this.mtMapKeyList = mtMapKeyList;
	}
	
	public Integer getNumDefaultParameters() {
		return numDefaultParameters;
	}

	public void setNumDefaultParameters(Integer numDefaultParameters) {
		this.numDefaultParameters = numDefaultParameters;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public UploadedFile getUploadData() {
		return uploadData;
	}

	public void setUploadData(UploadedFile uploadData) {
		this.uploadData = uploadData;
	}

	public ModelTemplateContainer getModelTemplateContainer() {
		return this.modelTemplateContainer;
	}

	public void setModelTemplateContainer(	ModelTemplateContainer _modelTemplateContainer) {
		this.modelTemplateContainer = _modelTemplateContainer;
	}

	public ModelTemplate getModelTemplate() {
		return this.modelTemplate;
	}

	public void setModelTemplate(	ModelTemplate _modelTemplate) {
		this.modelTemplate = _modelTemplate;
	}
	
	public StreamedContent getFileData() {
		return fileData;
	}

	public void setFileData(StreamedContent fileData) {
		this.fileData = fileData;
	}
	

	public String getCurrentddbid() {
		return currentddbid;
	}

	public void setCurrentddbid(String currentddbid) {
		log.log(Level.INFO, "setcurrentddbid:: enter  parameter currentddbid: "	+ currentddbid);
			this.currentddbid = currentddbid;
						
		}
		
	
	public void downLoadFile(ModelTemplate mt, String mapKey){
	    	if (mt!= null) {
	    		System.out.println("mt id  "+mt.getId() + " mt comment "+mt.getComment() + "mapkey "+mapKey );
	    		byte[]  fileMap=mt.getData(mapKey);
	    		;
	    		ByteArrayInputStream bis = new ByteArrayInputStream(fileMap);
	    		fileData = new  DefaultStreamedContent(bis, "text/plain", mapKey+".txt");
	    		
	    		}
	    	else {
	    		System.out.println("mt is null ");
	    		
	    	}
	 }
	    	
	public String create() throws Exception {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			log.log(Level.INFO," Create add  [ddbId:" +modelTemplateContainer.getDdbId() + " MTC Comment:"+ modelTemplateContainer.getComment() +" MT Comment "+modelTemplate.getComment() +" "+modelTemplate.getSimulator().getId()+ " "+modelTemplate.getSimulator().getSimulator()+"]");
			
			modelTemplateContainer.getModelTemplates().add(modelTemplate);
			
			pmanager.save(modelTemplateContainer);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,  bundle.getString("create.operation.msg"), bundle.getString("create.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO,"New model template container added: ["+ modelTemplateContainer.getId() +" " +modelTemplateContainer.getDdbId() + " "+ modelTemplateContainer.getComment() +"]");
			// initModelTemplateContainer();
			return "list?faces-redirect=true";
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during creation of ["+ modelTemplateContainer.getId() +" " +modelTemplateContainer.getDdbId() + " "+ modelTemplateContainer.getComment()+"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("create.failure.msg"));
			facesContext.addMessage(null, m);
			return "create";
		}
	}
	
	
	public String view(String ddbId) {
		log.log(Level.INFO,"view:: enter  parameter ddbId: " + ddbId);
		this.ddbId=ddbId;
		this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
		this.buildDefParamTable();
		log.log(Level.INFO,"view:: for  model template container: ["+ modelTemplateContainer.getId() +" " +modelTemplateContainer.getDdbId() + " "+ modelTemplateContainer.getComment()+"]");
		return "/modelTemplateContainer/details?faces-redirect=true&includeViewParams=true";
	}


	public String edit(String ddbId) {
		log.info(" edit enter:: parameter ddbId: " + ddbId);
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);	
		this.currentddbid=ddbId;
		this.ddbId=ddbId;
		try
		{
			if (modelTemplateContainer != null) 
			{
				buildDefParamTable();
				return "/modelTemplateContainer/edit?faces-redirect=true&includeViewParams=true";			
			}				
			else throw new Exception("Edit:modelTemplateContainer IS NULL");
		} 
		catch (Exception e) 
		{
			log.log(Level.WARNING,"Edit: Edit: modelTemplateContainer IS NULL"); 
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("edit.failure.msg"));
			facesContext.addMessage(null, m);
			return "list?faces-redirect=true";			
		}
	}
	
	
	

	public String delete(String ddbId) {
		log.log(Level.INFO,"delete:: enter  parameter ddbId: " + ddbId);
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
		
		try {
		
			if (modelTemplateContainer != null) {
				log.log(Level.INFO,"Delete model template container :["+ modelTemplateContainer.getId() +" " +modelTemplateContainer.getDdbId() + " "+ modelTemplateContainer.getComment()+"]");
				this.modelTemplateContainer = pmanager.findModelTemplateContainer(modelTemplateContainer.getDdbId() );
				pmanager.delete(this.modelTemplateContainer);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, modelTemplateContainer.getDdbId()+" "+bundle.getString("delete.operation.msg"),  bundle.getString("success.msg"));
				facesContext.addMessage(null, m);
				return "list?faces-redirect=true";
			} else 
				throw new Exception("Delete: modelTemplateContainer IS NULL");

		} catch (Exception e) {
			log.log(Level.WARNING," delete:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}

	public String update(String ddbId) {
		log.log(Level.INFO,"update:: enter parameter ddbId: " + ddbId);
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try {
			ModelTemplateContainer mtcFromDB = pmanager.findModelTemplateContainer(ddbId);
			if (mtcFromDB != null) {
				log.log(Level.INFO,"update: Model Template Controller: ["+ mtcFromDB.getId() +"] "); // +modelTemplateContainer.getDdbId() + " "+ modelTemplateContainer.getComment()+"]");
				
				mtcFromDB.setComment(modelTemplateContainer.getComment());
				pmanager.save(mtcFromDB);
				this.modelTemplateContainer = mtcFromDB;
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("update.operation.msg"), bundle.getString("update.success.msg"));
				facesContext.addMessage(null, m);
				return "edit";
			} else
				throw new Exception("update: newModelTemplateContainer IS NULL");

		} catch (Exception e) {			
			log.log(Level.WARNING,"update:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,  bundle.getString("update.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}	
	
	public void addModelTemplate(String ddbId)  {
		
		log.log(Level.INFO,"addModelTemplate:: enter  parameter ddbId: " + ddbId +" MT DafaultParameters.setNum: " +this.numDefaultParameters);
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		try {
			
			this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
			log.log(Level.INFO,"  addModelTemplate  [ddbId:" +modelTemplateContainer.getDdbId() + " MTC Comment:"+ modelTemplateContainer.getComment() +" MT Comment "+modelTemplate.getComment() +" "+modelTemplate.getSimulator().getId()+ " "+modelTemplate.getSimulator().getSimulator()+"]");
			List<ModelTemplate> modelTemplates=modelTemplateContainer.getModelTemplates();
			
			for (ModelTemplate mt :modelTemplates){
				if (mt.getSimulator().equals(modelTemplate.getSimulator() )){
					throw new Exception ("Model Template is already present!");
				}
			}
									
			modelTemplateContainer.getModelTemplates().add(modelTemplate);
			pmanager.save(modelTemplateContainer);
			this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
			modelTemplate = new ModelTemplate();
			this.buildDefParamTable();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("add.operation.msg"), bundle.getString("add.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO," model template added: ;");			
			
		} catch (Exception e) {
			log.log(Level.WARNING,"Errors :"+ e.getMessage());
			e.printStackTrace();
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
			
			
		}
	}
	
	
	
	public void removeModelTemplate(String ddbId,ModelTemplate modelTemplate)  {
		log.log(Level.INFO,"removeModelTemplate:: enter  parameter ddbId: " + ddbId +" MT  ID"+modelTemplate.getId()+ " Simulator" +modelTemplate.getSimulator());
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		try 
		{
			this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
			log.log(Level.INFO,"  removeModelTemplate  [ddbId:" +modelTemplateContainer.getDdbId() + " MTC Comment:"+ modelTemplateContainer.getComment() +" MT Comment "+modelTemplate.getComment() +" "+modelTemplate.getId()+ " "+modelTemplate.getSimulator()+"]");
			modelTemplateContainer.getModelTemplates().remove(modelTemplate);
			pmanager.save(modelTemplateContainer);
			modelTemplate = new ModelTemplate();
			this.modelTemplateContainer = pmanager.findModelTemplateContainer(ddbId);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("delete.operation.msg"), bundle.getString("delete.success.msg"));
			facesContext.addMessage(null, m);
			log.log(Level.INFO," model template removed: ;");			
			
		} 
		catch (Exception e) 
		{
			log.log(Level.WARNING,"Error ---");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,errorMessage,bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
		}
	}

	
	public String detailModelTemplate(ModelTemplate modelTemplate)  {
		log.log(Level.INFO,"detailModelTemplate:: enter  parameter ddbId:  MT  ID"+modelTemplate.getId()+ " Simulator" +modelTemplate.getSimulator());
		this.modelTemplate=modelTemplate;
		Map<String, ModelData> modelDataMap=modelTemplate.modelDataMap();
		Set<String> mapKey=modelDataMap.keySet();
		this.mtMapKeyList = new ArrayList<String>();
		mtMapKeyList.addAll(mapKey);
		log.log(Level.INFO, " mapKey  "+mapKey.toString());
		
		/**for (String p: mtMapKeyList){
			
			log.log(Level.INFO, " p "+p);
		}**/
		log.log(Level.INFO,"detailModelTemplate:: enter  MT  ID"+modelTemplate.getId()+ " Simulator:" +modelTemplate.getSimulator() +" comment: "+modelTemplate.getComment());
		return "/modelTemplate/details";
	}

	
	public void onRowToggle(ToggleEvent event) {  
		log.log(Level.INFO,":: onRowToggle enter");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,     "Row State " + event.getVisibility(), "Model Template :" + ((ModelTemplate) event.getData()).getId() ); 
        FacesContext.getCurrentInstance().addMessage(null, msg);  
    }  
	
	
	private void buildDefParamTable(){
		log.log(Level.INFO, "buildDefParamTable enter");
		Map <ModelTemplate,List<Integer>> mtList	= new HashMap<ModelTemplate,List<Integer>> ();
		List<ModelTemplate> modelTemplates=this.modelTemplateContainer.getModelTemplates();
		
		for (ModelTemplate elem: modelTemplates ) {
			if (elem.getDefaultParameters()!= null) {
				
				List<Integer> setNumList= new ArrayList<Integer>();
				for (DefaultParameters df: elem.getDefaultParameters()){
					log.log(Level.INFO, " key: setNum  :"+df.getSetNum() +" defParamListSize: "+df.getParameters().size());
					setNumList.add(df.getSetNum());

				}
				//sort by defSetNum
				Collections.sort(setNumList);
				mtList.put(elem,setNumList) ;
			}
		}
		this.modelTemplateList= mtList;

		
	}	
	

	public List<Parameter> getMtDefParams(ModelTemplate mt, Integer setNum){	
		List<ModelTemplate> modelTemplates=this.modelTemplateContainer.getModelTemplates();
		for (ModelTemplate elem: modelTemplates ) {
			if (elem.getDefaultParameters()!= null )
				if (elem.equals(mt)) 		
					for (DefaultParameters df: elem.getDefaultParameters()){
						if (setNum.equals(df.getSetNum()))	return df.getParameters();
					}
		}
		return null;
		
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
