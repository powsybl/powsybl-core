/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import eu.itesla_project.iidm.ddb.model.Connection;
import eu.itesla_project.iidm.ddb.model.ConnectionSchema;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.web.data.LazyEquipmentDataModel;
import eu.itesla_project.iidm.ddb.web.data.LazyInternalDataModel;


// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
//@Model
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@ManagedBean
@ViewScoped
public class EquipmentController {

   @Inject
   private FacesContext facesContext;

   @Inject
   private Logger log;
      
   @EJB
   private DDBManager pmanager;

   private Equipment newEquipment;
   
   private String cimId;
   
   private List<String> modelTemplateContainerDdbids;

  // private TreeMap<SimulatorInst,List<Connection>> treeMapConnection ;
   
   private List<Connection> schemaConnections ;
   
   private List<SimulatorInst> keySimulatorInst;
   
   private DualListModel<String> internals;
   
   private LazyEquipmentDataModel lazyDataModel;

   private int equipmentsCount;
   
   
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


   //@Produces
   @Named
   public Equipment getNewEquipment() {
      return newEquipment;
   }

   @PostConstruct
   public void initNewEquipment() {
	   log.log(Level.INFO, " initNewEquipment enter:: ");
	   
	   lazyDataModel=new LazyEquipmentDataModel(pmanager);
	   equipmentsCount=lazyDataModel.getRowCount();
	   
	   String paramCimId=null;
	   FacesContext ctx = FacesContext.getCurrentInstance();
	   Map<String, String> parameters = ctx.getExternalContext().getRequestParameterMap();
	   if (parameters.containsKey("cimId")) {
		   paramCimId = (String)parameters.get("cimId");
		   log.log(Level.INFO, " param :: "+paramCimId);
	   }
	   if (paramCimId == null) 
		   this.newEquipment = new Equipment("");
	   else
		   this.newEquipment=pmanager.findEquipment(paramCimId);
	   
	   if (this.newEquipment!= null) 
		   log.log(Level.INFO, " newEquipment :: "+this.newEquipment.getCimId());
	   
       List<String> internalsSource = new ArrayList<String>();  
       List<String> internalsTarget = new ArrayList<String>();  
       
       Query q1 = em.createQuery("SELECT m.nativeId FROM Internal m order by m.nativeId");
       
       internalsSource=q1.getResultList();

       /*
       List<Internal> internalToConnect=pmanager.findInternalsAll();
       for (Internal internalElem : internalToConnect){
    	   internalsSource.add(internalElem.getNativeId());  
       }
       */
         
       internals = new DualListModel<String>(internalsSource, internalsTarget);  
       
	   log.log(Level.INFO, " query ModelTemplateContainer");
	   Query q = em.createQuery("SELECT m.ddbId FROM ModelTemplateContainer m order by m.ddbId");
	   modelTemplateContainersValues = q.getResultList();
	   log.log(Level.INFO, "DONE query ModelTemplateContainer");
	   log.log(Level.INFO, " query parameterContainer");
	   Query q2 = em.createQuery("SELECT m.ddbId FROM ParametersContainer m order by m.ddbId");
	   parametersContainerValues = q2.getResultList();
	   log.log(Level.INFO, "DONE query parameterContainer");
   }  
   
   
   
   
   public LazyEquipmentDataModel getLazyDataModel() {
	return lazyDataModel;
}

public void setLazyDataModel(LazyEquipmentDataModel lazyDataModel) {
	this.lazyDataModel = lazyDataModel;
}

public int getEquipmentsCount() {
	return equipmentsCount;
}

public void setEquipmentsCount(int equipmentsCount) {
	this.equipmentsCount = equipmentsCount;
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




public List<String> getParametersContainerValues() {
	return parametersContainerValues;
}




public void setParametersContainerValues(List<String> parametersContainerValues) {
	this.parametersContainerValues = parametersContainerValues;
}




public String getSelectedParametersContainer() {
	return selectedParametersContainer;
}




public void setSelectedParametersContainer(String selectedParametersContainer) {
	this.selectedParametersContainer = selectedParametersContainer;
}




public String getCimId() {
		return cimId;
   }

   public void setCimId(String cimId) {
		this.cimId = cimId;
		
		if(cimId != null)
		{
			this.newEquipment=pmanager.findEquipment(cimId);
			buildConnectionTable();
		}
   }
	
   public List<String> getModelTemplateContainerDdbids() {
		return modelTemplateContainerDdbids;
   }

   public void setModelTemplateContainerDdbids(
			List<String> modelTemplateContainerDdbids) {
		this.modelTemplateContainerDdbids = modelTemplateContainerDdbids;
   }

   public  List<Connection> getSchemaConnections() {
		return schemaConnections;
	}

	public void setSchemaConnections( List<Connection> schemaConnections) {
		this.schemaConnections = schemaConnections;
	}
	
   /*
   public TreeMap<SimulatorInst, List<Connection>> getTreeMapConnection() {
		return treeMapConnection;
   }

   public void setTreeMapConnection(TreeMap<SimulatorInst, List<Connection>> connections) {
		this.treeMapConnection = connections;
   }
   **/


   public List<SimulatorInst> getKeySimulatorInst() {
		return keySimulatorInst;
   }

   public void setKeySimulatorInst(List<SimulatorInst> keySimulatorInst) {
		this.keySimulatorInst = keySimulatorInst;
   }

   public DualListModel<String> getInternals() {
		return internals;
   }

   public void setInternals(DualListModel<String> internals) {
		this.internals = internals;
   }
			
   public String create() throws Exception {
	   FacesContext context = FacesContext.getCurrentInstance();
	   ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
	   try {
		   log.log(Level.INFO," Create new Equipment: [cimIdId: " +newEquipment.getCimId() 
					+"  Model Container DDBID: "+ selectedModelTemplateContainer
					+"  Parameter Container: "+selectedParametersContainer +"]");
		   
		    ModelTemplateContainer mc=pmanager.findModelTemplateContainer(this.selectedModelTemplateContainer);
		    newEquipment.setModelContainer(mc);
		    
		    ParametersContainer pc=pmanager.findParametersContainer(this.selectedParametersContainer);
		    newEquipment.setParametersContainer(pc);
			
			pmanager.save(this.newEquipment);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("create.operation.msg"), bundle.getString("create.success.msg"));
			facesContext.addMessage(null, m);
			return "list?faces-redirect=true";
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during creation of ["+ newEquipment.getCimId()+"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("create.failure.msg"));
			facesContext.addMessage(null, m);
			return "create";
		}
	}
   
   
   public String delete(String cimId) throws Exception {
	   	FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		
		try {
			log.log(Level.INFO," Delete Equipment: [cimIdId: " +cimId +"]");
			this.newEquipment=pmanager.findEquipment(cimId);													
			pmanager.delete(this.newEquipment);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("delete.operation.msg"), bundle.getString("delete.success.msg"));
			facesContext.addMessage(null, m);
			return "list?faces-redirect=true";
		} catch (Exception e) {
			log.log(Level.WARNING,"Error during delete of ["+ newEquipment.getCimId()+"]");
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage, bundle.getString("delete.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}
   
   public String edit(Equipment eq) {
		log.log(Level.INFO," edit enter:: ["+eq.getCimId()+"]");
		this.cimId=eq.getCimId();
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
		this.newEquipment=pmanager.findEquipment(eq.getCimId());
		try 
		{
			if (newEquipment != null) {
				log.log(Level.INFO,"Edit Equipment : ["+ newEquipment.getCimId()+"]");
				this.buildConnectionTable();
				return "edit?faces-redirect=true&includeViewParams=true";
			}
			else throw new Exception("Edit: Equipment not found!");
		} 
		catch (Exception e) 
		{
			log.log(Level.WARNING,"edit equipment:: catch an Exception" + e.getMessage());
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,	errorMessage,  bundle.getString("edit.failure.msg"));
			facesContext.addMessage(null, m);
			return "edit";
		}
	}
   

   public void onTransfer(TransferEvent event) {  
	   log.log(Level.INFO,"onTransfer:: enter");
	   FacesContext context = FacesContext.getCurrentInstance();
	   ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
	   StringBuilder builder = new StringBuilder();  
       for(Object item : event.getItems()) {  
           builder.append((String) item).append("<br />");
       }  
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("connection.operation.transf.msg"),bundle.getString("connection.success.msg"));  
       FacesContext.getCurrentInstance().addMessage(null, msg);  
   }  
  
   
   public void addConnectionWithInternal(String paramCimId ){
	   log.log(Level.INFO,"addConnectionWithInternal::  paramCimId: "+paramCimId ); 
	   
	   FacesContext context = FacesContext.getCurrentInstance();
	   ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
	   
	   if (paramCimId  != null && this.internals != null && this.internals.getTarget().size()> 0) {	
		   this.newEquipment=pmanager.findEquipment(paramCimId);		   
	   	   ConnectionSchema cs=pmanager.findConnectionSchema(paramCimId, null);
	   	   if (cs==null)   cs= new ConnectionSchema(paramCimId);
	   	       	   	   	   
	   	   List<Connection> connections = new ArrayList<Connection>();
	   	   if (cs.getConnections() !=null && cs.getConnections().size()>0)
	   		   connections= cs.getConnections();
	   	   
           for(Object item : this.internals.getTarget())   {
        	   Connection connToAdd=new Connection (paramCimId, 0, (String)item, 1, null,null, 0);
        	   //to avoid connection duplicated 
        	   if (!connections.contains(connToAdd))
        	   	connections.add(new Connection (paramCimId, 0, (String)item, 1, null,null, 0));
           }
           
           if (connections!= null  && connections.size()>0) { 
        	   cs.setConnections(connections);
        	   pmanager.save(cs);
        	   log.log(Level.INFO,"addConnectionWithInternal:: connectionsaved");
        	   buildConnectionTable();
        	   FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("connection.operation.add.msg"),bundle.getString("connection.add.success.msg"));  
        	   FacesContext.getCurrentInstance().addMessage(null, msg);
           }else log.log(Level.INFO,"addConnectionWithInternal:: no connection added");
	   }
	   else {
		   log.log(Level.INFO,"addConnectionWithInternal:: No connection added");
		   FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,bundle.getString("connection.add.failure.msg"),null);  
           FacesContext.getCurrentInstance().addMessage(null, msg);
	   }
   }
  
  /* This methods has been replaced by new methods that does not contains simulator's references
   * 
   private void buildConnectionTable() {
	   log.log(Level.INFO," buildConnectionTable enter:: ") ;	  		
	   this.treeMapConnection=  new TreeMap<SimulatorInst, List<Connection>>(new Comparator<SimulatorInst>()
 			    			{ @Override
 								public int compare(SimulatorInst o1, SimulatorInst o2)
 			    				{
 			    				return o1.getId().compareTo(o2.getId());
 			    				} 
 			    			});
 		this.keySimulatorInst = new ArrayList<SimulatorInst>();
 		try 
 		{
 			List<ModelTemplate>  mtList  =this.newEquipment.getModelContainer().getModelTemplates();
 			String cimId=this.newEquipment.getCimId();
 			
 			//exist one to one relationschip between MT and Simulator for each one search existing connection
 			for (ModelTemplate mt :mtList ){
 				SimulatorInst simulator	=mt.getSimulator();
 				log.log(Level.INFO," get ConnectionSchema for cimId:  "+cimId  +" Model Template: [id:"+  mt.getId() + " Simulator id: "+simulator.getId() +"]") ;
 				//ConnectionSchema connSchema =pmanager.findConnectionSchema(cimId, simulator);
 				ConnectionSchema connSchema =pmanager.findConnectionSchema(cimId, null);
 				if (connSchema!=null) 
 				{
 					List<Connection> connectionList= connSchema.getConnections();
 					List<Connection> equipmentConnections= new ArrayList<Connection>();
 					if (connectionList != null && connectionList.size()> 0) 
 					{
 						Iterator<Connection> connectionIterator=connectionList.listIterator();
 						//get only connection of selectecdEquipment
 						while (connectionIterator.hasNext())
 						{
 							Connection conn=connectionIterator.next();
 							if (conn.getId1().equals(cimId) ) equipmentConnections.add(conn);
 						}
 						if (equipmentConnections!= null && equipmentConnections.size()>0) 
 						{
 							this.keySimulatorInst.add(simulator);
 							this.treeMapConnection.put(simulator, equipmentConnections);  		
 						}
 					}
 					else log.log(Level.INFO," there aren't  connections for connectionSchema "+connSchema.getId()) ;
 				}
 				else log.log(Level.INFO," there aren't  connectionSchema ") ;
 		}
 		  	
 		}
 		catch (Exception exc)
 		{
 			exc.printStackTrace();
 			log.log(Level.INFO,"  exception "+exc.getMessage()) ;
 		}
 	}
*/
   
   private void buildConnectionTable() {
	   log.log(Level.INFO," buildConnectionTable enter:: ") ;	  		
	   try 
 		{
 			this.schemaConnections=  new ArrayList<Connection>();
 			String cimId=this.newEquipment.getCimId();
 			ConnectionSchema connSchema =pmanager.findConnectionSchema(cimId, null);
 			if (connSchema!=null) 
 				this.schemaConnections= connSchema.getConnections();
 			else 
 				log.log(Level.INFO," there aren't  connectionSchema ") ;
 		}
 		catch (Exception exc)
 		{
 			exc.printStackTrace();
 			log.log(Level.INFO,"  exception "+exc.getMessage()) ;
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
