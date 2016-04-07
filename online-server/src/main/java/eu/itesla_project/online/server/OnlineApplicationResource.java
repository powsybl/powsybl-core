/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server;



import java.util.Collection;
import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.annotations.cache.NoCache;

import eu.itesla_project.online.ContingencyStatesActionsSynthesis;
import eu.itesla_project.online.ContingencyStatesIndexesSynthesis;
import eu.itesla_project.online.IndexSecurityRulesResultsSynthesis;
import eu.itesla_project.online.WcaContingenciesSynthesis;
import eu.itesla_project.online.WorkStatus;
import eu.itesla_project.online.WorkSynthesis;
import eu.itesla_project.online.server.message.SelectedWorkFlowInfoMessage;
import eu.itesla_project.online.server.message.StatesWithActionsSynthesisMessage;
import eu.itesla_project.online.server.message.StatesWithIndexesSynthesisMessage;
import eu.itesla_project.online.server.message.StatesWithSecurityRulesResultSynthesisMessage;
import eu.itesla_project.online.server.message.WcaContingenciesMessage;
import eu.itesla_project.online.server.message.WorkFlowIdsMessage;
import eu.itesla_project.online.server.message.WorkStatusMessage;
import eu.itesla_project.online.server.message.WorkflowListMessage;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Path("/online/workflow")
@NoCache
public class OnlineApplicationResource {
		
    @Inject
    private OnlineApplicationBean bean;
       
    @Context
    SecurityContext securityContext;
    
    public OnlineApplicationResource() {
    	
    }
   

    @POST
    @Path("start")
    public void start() {
        bean.start();
    }

    @POST
    @Path("stop")
    public void stop() {
        bean.stop();
    }

    @POST
    @Path("notifyListeners")
    public void notifyListeners() {
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("availableCores")
    public int getAvailableCores() {
    	int cores=0;
    	try{
    		cores=bean.getAvailableCores();
    	}
    	catch(Exception ex){ex.printStackTrace();}
       return cores;
    }
    

    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("currentUser")
    public String getCurrentUser() {
    	String user="";
    	if(securityContext!=null && securityContext.getUserPrincipal()!=null)
    		user= securityContext.getUserPrincipal().getName();
    
    	return user;
    }
    
    @POST
    @Path("logout")
    public void logout(@Context HttpServletRequest req) {
    	try {
			req.logout();
		} catch (ServletException e) {
			e.printStackTrace();
		}
    	
    	HttpSession session= req.getSession();   
    	if(session != null)
    		try{
    			
    			session.invalidate();
    			
    			
    		}
    		catch(Exception ex){}
    	
    	
    }
    
    
    
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("isConnected")
    public boolean isConnected() {
    	boolean res=false;
    	try{
    		res =bean.isJmxConnected();
    	}
    	catch(Exception ex){}
       return res;
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("wcaRunning/{workflowId}")
    public boolean iswcaRunning(@PathParam("workflowId") String workflowId) {
    	boolean res=false;
    	try{
    		res =bean.isWcaRunning(workflowId);
    	}
    	catch(Exception ex){}
       return res;
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("running/{workflowId}")
    public boolean isRunning(@PathParam("workflowId") String workflowId) {
    	boolean res=false;
    	try{
    		
    		res =bean.isWorkflowRunning(workflowId);
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("status/{workflowId}")
    public int getStatus(@PathParam("workflowId") String workflowId) {
    	int res=-1;
    	try{
    		
    		res =bean.getWorkflowStatus(workflowId);
    	}
    	catch(Exception ex){ex.printStackTrace();}
       return res;
    }
    /*
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("stables/{workflowId}")
    public String  getStableContingencies(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		String[] ws=bean.getStableContingencies(workflowId);
    		if(ws!=null)
    		{
    			StableContingenciesMessage msg =new StableContingenciesMessage(new StableContingenciesSynthesis(workflowId,ws));
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    */
   
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("workStatus/{workflowId}")
    public String  getWorkStatus(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		HashMap<Integer,WorkStatus> ws=bean.getWorkStatus(workflowId);
    		if(ws!=null)
    		{
	    		WorkStatusMessage msg =new WorkStatusMessage(new WorkSynthesis(workflowId,ws));
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statesActions/{workflowId}")
    public String  getStatesWithActions(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		ContingencyStatesActionsSynthesis ws=bean.getStatesActions(workflowId);
    		if(ws!=null)
    		{
    			StatesWithActionsSynthesisMessage msg =new StatesWithActionsSynthesisMessage(ws);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statesIndexes/{workflowId}")
    public String  getStatesWithIndexes(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		ContingencyStatesIndexesSynthesis ws=bean.getUnsafeContingencies(workflowId);
    		if(ws!=null)
    		{
    			StatesWithIndexesSynthesisMessage msg =new StatesWithIndexesSynthesisMessage(ws);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statesIndexesSecurityRules/{workflowId}")
    public String  getStatesWithSecurityRulesSyntesis(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		IndexSecurityRulesResultsSynthesis sria =bean.getSecurityRulesIndexesApplication(workflowId);
    		if(sria!=null)
    		{
    			StatesWithSecurityRulesResultSynthesisMessage msg =new StatesWithSecurityRulesResultSynthesisMessage(sria);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("workflowids")
    public String  getWorkflowids() {
    	String  res=null;
    	try{
    		Collection<String> ids=bean.getWorkFlowIds();
    		if(ids!=null)
    		{
    			WorkFlowIdsMessage msg =new WorkFlowIdsMessage(ids);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("workflows")
    public String  getWorkflows() 
    {
    	String  res=null;
    	try
    	{
    		HashMap<String, OnlineWorkflowInfo> workflows=bean.getWorkFlows();
    		if(workflows!=null)
    		{
    			WorkflowListMessage msg =new WorkflowListMessage(workflows);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex)
    	{
    	}
    	
       return res;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("selectedWorkFlow/{workflowId}")
    public String  getSelectedWorkflow(@PathParam("workflowId") String workFlowId) 
    {
    	String  res=null;
    	try
    	{
    		HashMap<String, OnlineWorkflowInfo> workflows=bean.getWorkFlows();
    		
    		if(workflows!=null)
    		{   OnlineWorkflowInfo onlineWorkflowInfo = workflows.get(workFlowId);
    			SelectedWorkFlowInfoMessage msg =new SelectedWorkFlowInfoMessage(onlineWorkflowInfo);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex)
    	{
    	}
    	
       return res;
    }
    
  
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("isWorkflowRunning/{workflowId}")
    public boolean isWorkflowRunning(@PathParam("workflowId") String workflowId) {
    	boolean res=false;
    	try{
    		
    		res =bean.isWorkflowRunning(workflowId);
    	}
    	catch(Exception ex){
    	}
       return res;
    }
    
    
   /* @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("unstableContingencies/{workflowId}")
    public String  getUnstableContingenciesSynthesis(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		UnstableContingenciesSynthesis ustCont =bean.getUnstableContingencies(workflowId);
    		if(ustCont!=null)
    		{
    			UnstableContingenciesMessage msg =new UnstableContingenciesMessage(ustCont);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }*/
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("wcaContingencies/{workflowId}")
    public String  getWcaContingenciesSynthesis(@PathParam("workflowId") String workflowId) {
    	String res=null;
    	try{
    		WcaContingenciesSynthesis wcaCtgs =bean.getWcaContingencies(workflowId);
    		if(wcaCtgs!=null)
    		{
    			WcaContingenciesMessage msg =new WcaContingenciesMessage(wcaCtgs);
	    		res=msg.toJson();
    		}
    	}
    	catch(Exception ex){}
    	
       return res;
    }
   
}
