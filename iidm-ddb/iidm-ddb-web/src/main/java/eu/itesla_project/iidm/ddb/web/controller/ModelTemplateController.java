/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.ModelData;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterBoolean;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.ParameterInteger;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.DefaultParameterWeb;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@ManagedBean
@ViewScoped
public class ModelTemplateController {

	@Inject
	private Logger log;

	@Inject
	private FacesContext facesContext;

	private ArrayList<String> keyList;

	private ModelTemplate modelTemplate;

	private Long currentId;

	private String currentddbid;

	private DefaultParameterWeb defParameterWeb = new DefaultParameterWeb();

	private List<DefaultParameterWeb> addedDefParams = new ArrayList<DefaultParameterWeb>();

	private TreeMap<Integer, List<DefaultParameterWeb>> dbDefParams;

	private List<Integer> keydbDefParams;

	private UploadedFile dataFile;

	private int defaultParamIndex = 0;

	private String dataName;

	@EJB
	private DDBManager pmanager;

	@Inject
	private EntityManager em;

	public int getDefaultParamIndex() {
		return defaultParamIndex;
	}

	public void setDefaultParamIndex(int defaultParamIndex) {
		this.defaultParamIndex = defaultParamIndex;
	}

	public DefaultParameterWeb getDefParameterWeb() {
		return defParameterWeb;
	}

	public void setDefParameterWeb(DefaultParameterWeb defParameterWeb) {
		this.defParameterWeb = defParameterWeb;
	}

	public List<DefaultParameterWeb> getAddedDefParams() {
		return addedDefParams;
	}

	public void setAddedDefParams(List<DefaultParameterWeb> addedDefParams) {
		this.addedDefParams = addedDefParams;
	}

	public TreeMap<Integer, List<DefaultParameterWeb>> getDbDefParams() {
		return dbDefParams;
	}

	public void setDbDefParams(
			TreeMap<Integer, List<DefaultParameterWeb>> dbDefParams) {
		this.dbDefParams = dbDefParams;
	}

	public List<Integer> getKeydbDefParams() {
		return keydbDefParams;
	}

	public void setKeydbDefParams(List<Integer> keydbDefParams) {
		this.keydbDefParams = keydbDefParams;
	}

	public UploadedFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(UploadedFile dataFile) {
		this.dataFile = dataFile;
	}

	public String getCurrentddbid() {
		return currentddbid;
	}

	public void setCurrentddbid(String _containerddbid) {
		this.currentddbid = _containerddbid;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public Long getCurrentId() {
		return currentId;
	}

	public ArrayList<String> getKeyList() {
		return keyList;
	}

	public void setKeyList(ArrayList<String> keyList) {
		this.keyList = keyList;
	}

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public ModelTemplate getModelTemplate() {
		return this.modelTemplate;
	}

	public void setModelTemplate(ModelTemplate _modelTemplate) {
		this.modelTemplate = _modelTemplate;
	}

	@PostConstruct
	public void init() {
		modelTemplate = new ModelTemplate();
	}

	// currentId =PK of ModelTemplate
	public void setCurrentId(Long currentId) {
		log.log(Level.INFO, "setCurrentId:: enter  parameter currentId: "	+ currentId);
		this.currentId = currentId;
		TypedQuery<ModelTemplate> query = em.createQuery(
				"SELECT m FROM ModelTemplate m WHERE m.id = :arg1",
				ModelTemplate.class);
		query.setParameter("arg1", currentId);
		List<ModelTemplate> res = query.getResultList();
		modelTemplate = res.size() > 0 ? res.get(0) : null;
		
		Map<String, ModelData> modelDataMap = modelTemplate.modelDataMap();
		Set<String> mapKey = modelDataMap.keySet();
		this.keyList = new ArrayList<String>();
		keyList.addAll(mapKey);
		buildDefParamsEditTable(modelTemplate.getDefaultParameters());
	}

	private void buildDefParamsEditTable(List<DefaultParameters> _defParams) {
		log.log(Level.INFO, " buildDefParamsEditableTable enter:: ");
		this.dbDefParams = new TreeMap<Integer, List<DefaultParameterWeb>>();

		for (DefaultParameters dp : _defParams) {

			List<DefaultParameterWeb> pwList = new ArrayList<DefaultParameterWeb>();
			int setNum = dp.getSetNum();

			for (Parameter p : dp.getParameters()) {
				log.log(Level.INFO, " _defaultParam [Name: " + p.getName()	+ ", Value: " + p.getValue() + "]");
				DefaultParameterWeb pw = new DefaultParameterWeb();
				pw.setSetNum(setNum);
				pw.setName(p.getName());
				Object pValue = p.getValue();
				pw.setValue(pValue);

				if (pValue instanceof java.lang.String)
					pw.setType("String");
				else if (pValue instanceof java.lang.Float)
					pw.setType("Float");
				else if (pValue instanceof java.lang.Integer)
					pw.setType("Integer");
				else if (pValue instanceof java.lang.Boolean)
					pw.setType("Boolean");
				else
					pw.setType("String"); // default
				pwList.add(pw);
			}

			if (pwList != null && pwList.size() > 0) {
				log.log(Level.INFO, " put in map:  setNum:" + setNum	+ " parameters list of size= " + pwList.size());
				dbDefParams.put(new Integer(setNum), pwList);
				this.keydbDefParams = new ArrayList<Integer>(	dbDefParams.keySet());
				log.log(Level.INFO,	"  keydbDefParams :" + keydbDefParams.toString());
			}
		}

	}

	public String reinit() {
		log.log(Level.INFO, " reint enter::");
		this.defParameterWeb = new DefaultParameterWeb();
		return null;
	}

	public String detailModelTemplate(ModelTemplate modelTemplate) {
		log.log(Level.INFO,	"detailModelTemplate:: enter  ddbId: MT  ID"+ modelTemplate.getId()	+ " Simulator"	+ modelTemplate.getSimulator());
		
		this.modelTemplate = modelTemplate;
		Map<String, ModelData> modelDataMap = modelTemplate.modelDataMap();
		Set<String> mapKey = modelDataMap.keySet();
		this.keyList = new ArrayList<String>();
		keyList.addAll(mapKey);
		return "/modelTemplate/details";
	}

	public StreamedContent getModelTemplateData(String key) {
		log.log(Level.INFO,	"getModelTemplateData:: enter  key: "+key);
		StreamedContent file = null;
		ByteArrayInputStream out = new ByteArrayInputStream(modelTemplate.getData(key));
		file = new DefaultStreamedContent(out, "text/txt", modelTemplate.getSimulator().toString() + "." + key);
		return file;
	}

	public void downloadData(String key) {
		log.log(Level.INFO,	"downloadData:: enter  key: "+key);
		byte[] res = modelTemplate.getData(key);
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		externalContext.setResponseHeader("Content-Length",	String.valueOf(res.length));
		externalContext.setResponseHeader("Content-Disposition","attachment;filename=\"" + key + "\"");
		try {
			externalContext.getResponseOutputStream().write(res);
		} catch (IOException e) {
			log.log(Level.WARNING,	"IOException during downloadData ");
			e.printStackTrace();
		}
		facesContext.responseComplete();
	}

	public void deleteData(String key) {
		log.log(Level.INFO, "enter deleteData    by key: " + key+ " currentid : " + this.currentId + " currentddbid: "+ this.currentddbid);
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try 
		{
			ModelTemplateContainer modelTemplateContainer = pmanager.findModelTemplateContainer(this.currentddbid);
			List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
			for (ModelTemplate mt : modelTemplates) {
				if (mt.getId().compareTo(currentId) == 0) {
					this.modelTemplate = mt;
				}
			}
		
			Map<String, ModelData> modelDataMap = this.modelTemplate.modelDataMap();
			modelDataMap.remove(key);
			List<ModelTemplate> modelTemplateUpdated=updateModelTemplatesData(modelDataMap, modelTemplates);
			modelTemplateContainer.setModelTemplates(modelTemplateUpdated);
			modelTemplateContainer = pmanager.save(modelTemplateContainer);
			Set<String> mapKey = modelDataMap.keySet();
			this.keyList = new ArrayList<String>();
			keyList.addAll(mapKey);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, key + " "+bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));
			FacesContext.getCurrentInstance().addMessage(null, m);
		}
		catch (Exception ex) 
		{
			log.log(Level.WARNING, "Error :" + ex.getMessage());
			String errorMessage = getRootErrorMessage(ex);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			ex.printStackTrace();
		}
	}

	public void updloadData() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		log.log(Level.INFO,	" enter uploadData for  MT_id:" + this.getCurrentId()	+ " MTC_id:" + this.getCurrentddbid());
		try {
			if (this.dataName != null && !this.dataName.equals("")	&& this.dataFile != null) 
			{
				ModelTemplateContainer modelTemplateContainer = pmanager.findModelTemplateContainer(this.currentddbid);
				List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
				for (ModelTemplate mt : modelTemplates) 
				{
					if (mt.getId().compareTo(currentId) == 0)	this.modelTemplate = mt;
					
				}
				
				Map<String, ModelData> mapData = this.modelTemplate.modelDataMap();
				mapData.put(dataName, new ModelData(dataFile.getContents()));
				List<ModelTemplate> modelTemplateUpdated=updateModelTemplatesData(mapData, modelTemplates);
				modelTemplateContainer.setModelTemplates(modelTemplateUpdated);
				modelTemplateContainer = pmanager.save(modelTemplateContainer);
				
				log.log(Level.INFO,	" ModelTemplateContainer save  "+ dataFile.getFileName());
				Set<String> mapKey = mapData.keySet();
				this.keyList = new ArrayList<String>();
				keyList.addAll(mapKey);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,dataFile.getFileName()+ " " +bundle.getString("upload.operation.msg"), bundle.getString("success.msg"));
				FacesContext.getCurrentInstance().addMessage(null, msg);
				this.dataName="";
			} 
			else {
				if (this.dataName.equals("") && this.dataFile != null) {
					throw new Exception("Data can't be empty");
				}
			}
		} catch (Exception ex) {
			log.log(Level.WARNING, "Error :" + ex.getMessage());
			String errorMessage = getRootErrorMessage(ex);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("upload.failure.msg"));
			facesContext.addMessage(null, m);
			ex.printStackTrace();
		}
	}

	public String deleteMT() {
		log.log(Level.INFO, "delete MT");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		
		try {
			ModelTemplateContainer modelTemplateContainer = pmanager.findModelTemplateContainer(this.currentddbid);
			List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
			for (ModelTemplate mt : modelTemplates) {
				if (mt.getId().compareTo(currentId) == 0) {
					this.modelTemplate = mt;
				}
			}

			modelTemplates.remove(this.modelTemplate);
			log.log(Level.INFO, "deleteMT:  ModelTemplatesList  after remove  "	+ modelTemplates.contains(this.modelTemplate));
			modelTemplateContainer.setModelTemplates(modelTemplates);
			modelTemplateContainer = pmanager.save(modelTemplateContainer);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, this.modelTemplate.getComment() + " "+bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "../modelTemplateContainer/list.jsf";

		} catch (Exception ex) {
			String errorMessage = getRootErrorMessage(ex);
			log.log(Level.WARNING, "Error  " + errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";

		}
	}

	public void updateMT() {
		log.log(Level.INFO, "update: [ id: " + this.modelTemplate.getId() + " "	+ " comment: " + this.modelTemplate.getComment() + "]");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try {
			updateModelTemplates( );
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, this.modelTemplate.getComment() + " "+bundle.getString("update.operation.msg"), bundle.getString("update.success.msg"));
			FacesContext.getCurrentInstance().addMessage(null, msg);
		
		} catch (Exception ex) {
			String errorMessage = getRootErrorMessage(ex);
			log.log(Level.WARNING, "Error  " + errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,bundle.getString("update.failure.msg"));
			facesContext.addMessage(null, m);
		}

	}
	
	public void saveDefParameters() {
		log.log(Level.INFO, "  saveDefParameters enter::");
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try 
		{
			ModelTemplateContainer modelTemplateContainer 	= pmanager.findModelTemplateContainer(this.currentddbid);
			List<ModelTemplate> modelTemplates 				= modelTemplateContainer.getModelTemplates();
			for (ModelTemplate mt : modelTemplates) {
				if (mt.getId().compareTo(currentId) == 0) {
					this.modelTemplate = mt;
				}
			}
			
			for (DefaultParameterWeb pw : addedDefParams) {
				boolean isNewDefParameters = false;
				DefaultParameters dp = null;
				List<DefaultParameters> dparamsList = this.modelTemplate.getDefaultParameters();
				if (dparamsList == null	|| (dparamsList != null && dparamsList.size() == 0)) {
					log.log(Level.INFO,"  there aren't any DefaultParameters defined for modelTemplate: "+ this.modelTemplate.getId());
					dparamsList = new ArrayList<DefaultParameters>();
					isNewDefParameters = true;
				} else {
					log.log(Level.INFO, "  there are: " + dparamsList.size()+ " DefaultParameters defined for modelTemplate: "	+ this.modelTemplate.getId());
					dp = defaultParameterBySetNum(dparamsList, pw.getSetNum());
				}

				if (dp == null) {
					log.log(Level.INFO,"  make a new instance of default parameters");
					dp = new DefaultParameters(pw.getSetNum());
					isNewDefParameters = true;
				}
				try {
					if (pw.getValue() != null) {
						switch (pw.getType()) {
							case "Integer": 	dp.addParameter(new ParameterInteger(pw.getName(),Integer.valueOf(pw.getValue().toString()))); 
												break;
							case "Float":		dp.addParameter(new ParameterFloat(pw.getName(),Float.valueOf(pw.getValue().toString())));
												break;
							case "Boolean":		dp.addParameter(new ParameterBoolean(pw.getName(),Boolean.valueOf(pw.getValue().toString())));
												break;
							case "String":		dp.addParameter(new ParameterString(pw.getName(),pw.getValue().toString()));
												break;
						    default: throw new RuntimeException(pw.getType() + " not supported.");
						}
						dp = pmanager.save(dp);
						if (isNewDefParameters) {
							log.log(Level.INFO, " add also to model template ");
							dparamsList.add(dp);
							ArrayList<ModelTemplate> modelTemplateUpdated = new ArrayList<ModelTemplate>();
							ListIterator<ModelTemplate> modelTemplateListIter = modelTemplates.listIterator();
							while (modelTemplateListIter.hasNext()) {
								ModelTemplate mt = modelTemplateListIter.next();
								if (mt.equals(this.modelTemplate))
									mt.setDefaultParameters(dparamsList);
								modelTemplateUpdated.add(mt);
							}
							modelTemplateContainer.setModelTemplates(modelTemplateUpdated);
							pmanager.save(modelTemplateContainer);
						}
					} else	log.log(Level.WARNING," parameterAdded has a NULL value will be rejected!");
				} catch (  RuntimeException ex) {
					log.log(Level.WARNING,	"Throw Exception :"+ ex.getMessage());
					throw new Exception(ex);
				}			
			}
			resetForms();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Parameters " +bundle.getString("add.operation.msg"), bundle.getString("success.msg"));
			facesContext.addMessage(null, m);

		} catch (Exception e) {
			log.log(Level.WARNING, "Error :" + e.getMessage());
			e.printStackTrace();
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,errorMessage, bundle.getString("add.failure.msg"));
			facesContext.addMessage(null, m);
		}
	}

	/**
	 * Delete user's parameter selected from defaultParameter, parameter table
	 * 
	 * @defParam user's defaultParameter selected
	 */
	public void deleteDefaultParamerter(DefaultParameterWeb defParam) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle= context.getApplication().getResourceBundle(context, "msg");	
		try 
		{
			log.log(Level.INFO,	" DefaultParamerter to delete: " 
					+ "[Name: "+ defParam.getName() + " setNum: "+ defParam.getSetNum() + " Type: "+ defParam.getType() + "  Value: "+ defParam.getValue() + "]");
			ModelTemplateContainer modelTemplateContainer = pmanager.findModelTemplateContainer(this.currentddbid);
			List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
			// get current Model Template
			for (ModelTemplate mt : modelTemplates) {
				if (mt.getId().compareTo(currentId) == 0) {
					this.modelTemplate = mt;
				}
			}
			// get a list of model template's defaultParameters
			List<DefaultParameters> dparamsList = this.modelTemplate.getDefaultParameters();
			if (dparamsList == null		|| (dparamsList != null && dparamsList.size() == 0)) {
				log.log(Level.INFO,	" no default parameters list for Model Template selected:: ");
			} else {
				// find parameters for setNum defParam.getSetNum
				DefaultParameters dp = defaultParameterBySetNum(dparamsList,defParam.getSetNum());
				Parameter paramToRemove = removeParameter(dp, defParam);
				if (paramToRemove != null) {
					dp.getParameters().remove(paramToRemove);
					dp = pmanager.save(dp);
				}

				if (dp.getParameters().size() == 0) {
					List<DefaultParameters> dparamsListUpdated = new ArrayList<DefaultParameters>();
					for (DefaultParameters elem : dparamsList) {
						if (elem.getSetNum() != defParam.getSetNum()
								&& elem.getParameters().size() > 0)
							dparamsListUpdated.add(elem);
					}
					ArrayList<ModelTemplate> modelTemplateUpdated = new ArrayList<ModelTemplate>();
					ListIterator<ModelTemplate> modelTemplateListIter = modelTemplates.listIterator();
					while (modelTemplateListIter.hasNext()) {
						ModelTemplate mt = modelTemplateListIter.next();
						if (mt.equals(this.modelTemplate)) {
							if (dparamsListUpdated.size() > 0)  mt.setDefaultParameters(dparamsListUpdated);
							else mt.setDefaultParameters(null);
						}
						modelTemplateUpdated.add(mt);
					}
					log.log(Level.INFO,	" modelTemplateList size con modelTemplate aggiornato: "+modelTemplateUpdated.size());
					modelTemplateContainer.setModelTemplates(modelTemplateUpdated);
					modelTemplateContainer = pmanager.save(modelTemplateContainer);
				}
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "DefaultParameters " +bundle.getString("delete.operation.msg"), bundle.getString("success.msg"));
				resetForms();
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}

		} 
		catch (Exception ex) {
			log.log(Level.WARNING, "Error :" + ex.getMessage());
			String errorMessage = getRootErrorMessage(ex);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			ex.printStackTrace();
		}

	}

	private DefaultParameters defaultParameterBySetNum(
		List<DefaultParameters> dparamsList, int setNum) throws Exception {
		for (DefaultParameters dp : dparamsList) {
			if (dp.getSetNum() == setNum)	return dp;
		}
		return null;
	}

	private Parameter removeParameter(DefaultParameters df,	DefaultParameterWeb dfw) throws Exception {
		log.log(Level.INFO,	"removeParameter:: enter parameter's name " + dfw.getName());

		List<Parameter> parameterList = null;
		if (df.containsParameterWithName(dfw.getName())) {
			log.log(Level.INFO, "DefaultParameter contains:: " + dfw.getName());
			parameterList = df.getParameters();
			Parameter paramToDelete = null;
			for (Parameter p : parameterList) {
				if (p.getName().equals(dfw.getName())) {
					paramToDelete = p;
					log.log(Level.INFO,	"paramToDelete:: " + paramToDelete.getName());
					break;
				}
			}
			return paramToDelete;
		} else	throw new Exception("DefaultParameter doesn't contain Parameter with name: "+ dfw.getName());
	}

	private String getRootErrorMessage(Exception e) {
		// Default to general error message that registration failed.
		String errorMessage = "See server log for more information";
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

	//this method avoid the errors duplicate key
	private void updateModelTemplates() throws Exception {

		ModelTemplateContainer modelTemplateContainer = pmanager.findModelTemplateContainer(this.currentddbid);

		List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
		ArrayList<ModelTemplate> modelTemplatesUpdated = new ArrayList<ModelTemplate>();
		ListIterator<ModelTemplate> modelTemplateListIter = modelTemplates.listIterator();
		while (modelTemplateListIter.hasNext()) {
			ModelTemplate mt = modelTemplateListIter.next();
			if (mt.getId().compareTo(this.currentId) != 0)
				modelTemplatesUpdated.add(mt);
			else
				modelTemplatesUpdated.add(this.modelTemplate);
		}
		modelTemplateContainer.setModelTemplates(modelTemplatesUpdated);
		modelTemplateContainer = pmanager.save(modelTemplateContainer);

	}

	//this method avoid the errors duplicate key
	private List<ModelTemplate> updateModelTemplatesData(Map<String, ModelData> modelDataMap, List<ModelTemplate> modelTemplates) throws Exception {
		this.modelTemplate.setMdata(modelDataMap);
		ArrayList<ModelTemplate> modelTemplatesUpdated = new ArrayList<ModelTemplate>();
		ListIterator<ModelTemplate> modelTemplateListIter = modelTemplates.listIterator();
		while (modelTemplateListIter.hasNext()) {
			ModelTemplate mt = modelTemplateListIter.next();
			if (mt.getId().compareTo(this.currentId) == 0)
				mt.setMdata(modelDataMap);

			modelTemplatesUpdated.add(mt);
		}
		return modelTemplatesUpdated;
	}
	
	private void resetForms() {
		log.log(Level.INFO, " resetForms enter");
		if (this.modelTemplate.getDefaultParameters() != null)
			buildDefParamsEditTable(this.modelTemplate.getDefaultParameters());
		else
			this.keydbDefParams = new ArrayList<Integer>();
		this.addedDefParams = new ArrayList<DefaultParameterWeb>();
		this.defParameterWeb = new DefaultParameterWeb();

	}

	
	
}
