/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.online.ContingencyStatesActionsSynthesis;
import eu.itesla_project.online.ContingencyStatesIndexesSynthesis;
import eu.itesla_project.online.IndexSecurityRulesResultsSynthesis;
import eu.itesla_project.online.OnlineApplication;
import eu.itesla_project.online.OnlineApplicationListener;
import eu.itesla_project.online.OnlineWorkflowContext;
import eu.itesla_project.online.OnlineWorkflowStartParameters;
import eu.itesla_project.online.RemoteOnlineApplication;
import eu.itesla_project.online.RunningSynthesis;
import eu.itesla_project.online.StatusSynthesis;
import eu.itesla_project.online.WcaContingenciesSynthesis;
import eu.itesla_project.online.WorkStatus;
import eu.itesla_project.online.WorkSynthesis;
import eu.itesla_project.online.server.message.BusyCoresMessage;
import eu.itesla_project.online.server.message.ConnectionMessage;
import eu.itesla_project.online.server.message.StatesWithActionsSynthesisMessage;
import eu.itesla_project.online.server.message.StatesWithIndexesSynthesisMessage;
import eu.itesla_project.online.server.message.StatesWithSecurityRulesResultSynthesisMessage;
import eu.itesla_project.online.server.message.StatusMessage;
import eu.itesla_project.online.server.message.WcaContingenciesMessage;
import eu.itesla_project.online.server.message.WcaRunningMessage;
import eu.itesla_project.online.server.message.WorkStatusMessage;
import eu.itesla_project.online.server.message.WorkflowListMessage;
import eu.itesla_project.online.server.util.WebSocketSessions;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@ApplicationScoped
public class OnlineApplicationBean {

    private static final Logger 			LOGGER = LoggerFactory.getLogger(OnlineApplicationBean.class);

    private OnlineApplication 				application;

    private final WebSocketSessions 		sessions = new WebSocketSessions();

    @Resource
    private ManagedExecutorService 			executorService;
    
    private boolean 						remoteConnected=false;
    
    private HashMap<String, OnlineWorkflowInfo>  workflows = new HashMap<String, OnlineWorkflowInfo> ();
    
    private int idGenerator=0;
    
    private final OnlineApplicationListener computationResourcesListener = new OnlineApplicationListener() {

        @Override
        public void onBusyCoresUpdate(int[] busyCores) {
        	StringBuffer sb=new StringBuffer();
        	sb.append("[");
        	for(int i : busyCores)
        		sb.append(" "+i);
        	
        	sb.append(" ]");
        		
        	LOGGER.info("onBusyCoresUpdate "+sb.toString());
            sessions.send(new BusyCoresMessage(busyCores));
        }

		@Override
		public void onWorkflowStateUpdate(WorkSynthesis status) {
			OnlineWorkflowInfo wi = workflows.get(status.getWorkflowId());
			wi.setWorkStatus(status.getStatus());
			
			//currentWorkflow.setWorkStatus(status.getStatus());
			sessions.send(new WorkStatusMessage(status));
		}
		
		@Override
	    public void onWorkflowUpdate(StatusSynthesis status) {
			OnlineWorkflowInfo wi = workflows.get(status.getWorkflowId());
			//System.out.println("Received StatusSynthesis "+status.getWorkflowId() +" , "+status.getStatus());
			if(wi==null)
			{
				wi=new OnlineWorkflowInfo(status.getWorkflowId());
				workflows.put(status.getWorkflowId(), wi);
				sessions.send( new WorkflowListMessage(workflows));
			}
			wi.setStatus(status.getStatus());
			sessions.send(new StatusMessage(status));
			
	        }

		@Override
	    public void onWcaUpdate(RunningSynthesis wcaRunning) {	
			OnlineWorkflowInfo wi = workflows.get(wcaRunning.getWorkflowId());
			wi.setWcaRunning(wcaRunning.isRunning());
	        sessions.send(new WcaRunningMessage(wcaRunning));
		}

	        
	    @Override
	    public void onStatesWithActionsUpdate(ContingencyStatesActionsSynthesis statesActions) {
	    	OnlineWorkflowInfo wi = workflows.get(statesActions.getWorkflowId());
	    	wi.setStatesActions(statesActions);
	    	sessions.send(new StatesWithActionsSynthesisMessage(statesActions));
	    }

	    @Override
	    public void onStatesWithIndexesUpdate(ContingencyStatesIndexesSynthesis unsafeContingencies) {
	    	OnlineWorkflowInfo wi = workflows.get(unsafeContingencies.getWorkflowId());
	    	wi.setUnsafeContingencies(unsafeContingencies);
	    	sessions.send(new StatesWithIndexesSynthesisMessage(unsafeContingencies));
	    }

		/*@Override
		public void onStableContingencies(StableContingenciesSynthesis stableContingencies) {
			OnlineWorkflowInfo wi = workflows.get(stableContingencies.getWorkflowId());
			wi.setStableContingencies(stableContingencies.getContingencies());
			sessions.send(new StableContingenciesMessage(stableContingencies));
			
		}
		
		
				
		@Override
		public void onUnstableContingencies(UnstableContingenciesSynthesis unstableContingencies) {
			OnlineWorkflowInfo wi = workflows.get(unstableContingencies.getWorkflowId());
			wi.setUnstableContingencies(unstableContingencies);
			sessions.send(new UnstableContingenciesMessage(unstableContingencies));
			
		}
		*/
	    
	    @Override
	    public void onWcaContingencies(WcaContingenciesSynthesis wcaContingencies) {
			OnlineWorkflowInfo wi = workflows.get(wcaContingencies.getWorkflowId());
			wi.setWcaContingencies(wcaContingencies);
			sessions.send(new WcaContingenciesMessage(wcaContingencies));
			
		}
	    
		
		@Override
		public void onStatesWithSecurityRulesResultsUpdate(IndexSecurityRulesResultsSynthesis indexResults){
			OnlineWorkflowInfo wfi = workflows.get(indexResults.getWorkflowId());
			wfi.setSecurityRulesIndexesApplication(indexResults);
			sessions.send(new StatesWithSecurityRulesResultSynthesisMessage(indexResults));
		}

		@Override
		public void onDisconnection() {
			remoteConnected=false;
			LOGGER.warn("JMX Disconnected");
			sessions.send(new ConnectionMessage(false));
		}
		
		@Override
		public void onConnection() {
			remoteConnected=true;
			LOGGER.info("JMX Connected");
			sessions.send(new ConnectionMessage(true));
		}

		
		@Override
		public void onWorkflowEnd(OnlineWorkflowContext oCtx, OnlineDb onlineDB,  ContingenciesAndActionsDatabaseClient cadbClient, OnlineWorkflowParameters parameters) {
			// TODO Auto-generated method stub
			
		}

		
	
		
    };

    WebSocketSessions getSessions() {
        return sessions;
    }


    @PostConstruct
    void init() {
        LOGGER.info("Initializing online workflow");
        
        try {
            OnlineWorkflowStartParameters conf=OnlineWorkflowStartParameters.loadDefault();
            application = new RemoteOnlineApplication(conf);
            application.addListener(computationResourcesListener);

        } catch (IOException | MalformedObjectNameException | InstanceNotFoundException e) {
        	remoteConnected=false;
        	 LOGGER.error(" OnlineApplicationBean error th runtime Exception "+e.getMessage());
        	 e.printStackTrace();
        }
        
    }

    @PreDestroy
    void terminate() {
        LOGGER.info("Terminating online workflow");
        if(application!=null)
        	application.removeListener(computationResourcesListener);
        sessions.close();
    }

    void start() {
    	if(!remoteConnected)
    		return;
    	
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                	LOGGER.info("START WORKFLOW");
                	
                    
                    application.startWorkflow(null,null);

                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                    
                }
            }
        });
    }

    void stop() {
    	LOGGER.info("STOP WORKFLOW");
    	if(!remoteConnected)
    		return;
    	if(application!=null)
    		application.stopWorkflow();

    }
    
    boolean isWorkflowRunning(String workflowId)
    {
    	LOGGER.info("IS WORKFLOW ID ::"+workflowId + "RUNNING" );
    	OnlineWorkflowInfo wfi = workflows.get(workflowId);
    	return wfi.getStatus( ) == StatusSynthesis.STATUS_RUNNING;
    	
    }

   

	public boolean isWcaRunning(String workflowId) {
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.isWcaRunning();
	}
	
	public HashMap<Integer, WorkStatus> getWorkStatus(String workflowId) {
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getWorkStatus();
	}
	
	public ContingencyStatesActionsSynthesis getStatesActions(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getStatesActions();
	}
	
	public ContingencyStatesIndexesSynthesis getUnsafeContingencies(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getUnsafeContingencies();
	}
	

	public IndexSecurityRulesResultsSynthesis getSecurityRulesIndexesApplication(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getSecurityRulesIndexesApplication();
	}
	
	/*
	
	public String[] getStableContingencies(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getStableContingencies();
	}

	public UnstableContingenciesSynthesis getUnstableContingencies(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getUnstableContingencies();
	}
	*/
	
	public WcaContingenciesSynthesis getWcaContingencies(String workflowId){
		OnlineWorkflowInfo wfi = workflows.get(workflowId);
		return wfi.getWcaContingencies();
	}

	public int getAvailableCores() {
		int cores=0;
		if(remoteConnected && application !=null)
			cores=application.getAvailableCores();
		return cores;
	}


	public int getWorkflowStatus(String workflowId) {
		return workflows.get(workflowId).getStatus();
		
	}
	
	
	public Collection<String> getWorkFlowIds(){
		return workflows.keySet();	
	}
	
	
	public HashMap<String, OnlineWorkflowInfo> getWorkFlows()
	{
		return workflows;		
		
	}
	
	public boolean isJmxConnected(){
		return (remoteConnected && application !=null );
	}
	
	
}
