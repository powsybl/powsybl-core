/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.modules.online.OnlineWorkflowParameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.JMRuntimeException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RemoteOnlineApplication implements OnlineApplication, NotificationListener {

	

    private final List<OnlineApplicationListener> listeners = new CopyOnWriteArrayList<>();


    private JMXConnector connector;

    private MBeanServerConnection mbsc;
    
    private JMXServiceURL serviceURL;
    Map<String, String> jmxEnv;

    private LocalOnlineApplicationMBean application;
    
    private boolean connected;

    public RemoteOnlineApplication( OnlineWorkflowStartParameters conf) throws IOException, MalformedObjectNameException, InstanceNotFoundException {

    	
		
		
		String host = conf.getJmxHost();
		String port = conf.getJmxPort();
		
		String urlString = "service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/jmxrmi";
		serviceURL = new JMXServiceURL(urlString);
		jmxEnv = new HashMap<>();
    	
    	
    	ScheduledExecutorService scheduledExecutorService =
    	        Executors.newScheduledThreadPool(5);

    	ScheduledFuture scheduledFuture =
    	    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
    	        public void run() {
    	        	try{
    	        		application.ping();
    	        	}
    	        	catch(Exception ex){
    	        		try{
    	        			
    	        			notifyDisconnection();
    	        			connect();}
    	        		catch(Throwable t){}
    	        	}
    	           
    	            
    	        }
    	    },
    	    2,
    	    10,
    	    TimeUnit.SECONDS);

    }
    
    
    private void connect() throws IOException, MalformedObjectNameException, InstanceNotFoundException{
    	try{
	    	this.connector = JMXConnectorFactory.connect(serviceURL, jmxEnv) ;
	        mbsc = connector.getMBeanServerConnection();
	        
	        ObjectName name = new ObjectName(LocalOnlineApplicationMBean.BEAN_NAME);
	        application = MBeanServerInvocationHandler.newProxyInstance(mbsc, name, LocalOnlineApplicationMBean.class, false);
	        mbsc.addNotificationListener(name, this, null, null);
	        connected=true;
	        for (OnlineApplicationListener l : listeners) {
	            l.onConnection();
	        }
    	}
    	catch(Exception ex)
    	{
    		//ex.printStackTrace();
    		System.out.println("Exception connecting JMX "+ex);
    	}
    }
    
    
    
    private void notifyDisconnection()
    {
    	if(connected)
    	{
	    	connected=false;
	    	for (OnlineApplicationListener l : listeners) {
	            l.onDisconnection();
	        }
    	}
    }


    @Override
    @SuppressWarnings("unchecked")
    public void handleNotification(Notification notification, Object handback) {
    	
        AttributeChangeNotification notification1 = (AttributeChangeNotification) notification;
        switch (notification1.getAttributeName()) {
            case LocalOnlineApplicationMBean.BUSY_CORES_ATTRIBUTE:
                for (OnlineApplicationListener l : listeners) {
                    l.onBusyCoresUpdate((int[]) notification1.getNewValue());
                }
                break;
            case LocalOnlineApplicationMBean.RUNNING_ATTRIBUTE:
                for (OnlineApplicationListener l : listeners) {
                    l.onWorkflowUpdate( (StatusSynthesis) notification1.getNewValue());
                }
                break;
            case LocalOnlineApplicationMBean.WCA_RUNNING_ATTRIBUTE:
                for (OnlineApplicationListener l : listeners) {
                    l.onWcaUpdate( (RunningSynthesis) notification1.getNewValue());
                }
                break;
            case LocalOnlineApplicationMBean.STATES_ACTIONS_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onStatesWithActionsUpdate(  (ContingencyStatesActionsSynthesis) notification1.getNewValue());
                }
                
                break;     
            case LocalOnlineApplicationMBean.STATES_INDEXES_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onStatesWithIndexesUpdate( (ContingencyStatesIndexesSynthesis) notification1.getNewValue());
                }
                
                break;                  
            case LocalOnlineApplicationMBean.WORK_STATES_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onWorkflowStateUpdate(  (WorkSynthesis) notification1.getNewValue());
                }
                
                break;              
         /* case LocalOnlineApplicationMBean.STABLE_CONTINGENCIES_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onStableContingencies(  (StableContingenciesSynthesis) notification1.getNewValue());
                }
                
                break;*/  
                
            case LocalOnlineApplicationMBean.INDEXES_SECURITY_RULES_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onStatesWithSecurityRulesResultsUpdate( (IndexSecurityRulesResultsSynthesis) notification1.getNewValue());
                }                
            break; 
            
           /* case LocalOnlineApplicationMBean.UNSTABLE_CONTINGENCIES_ATTRIBUTE:
            	
                for (OnlineApplicationListener l : listeners) {
                    l.onUnstableContingencies(  (UnstableContingenciesSynthesis) notification1.getNewValue());
                }
                
                break;  
             */   
            
            
            case LocalOnlineApplicationMBean.WCA_CONTINGENCIES_ATTRIBUTE:
            	for (OnlineApplicationListener l : listeners) {
                    l.onWcaContingencies(  (WcaContingenciesSynthesis) notification1.getNewValue());
                }
                
                break;
            	
           default:
                throw new AssertionError();
        }
    }

    @Override
    public int getAvailableCores() {
    	int cores=0;
    	try{
    		cores= application.getAvailableCores();
    	}
    	catch( JMRuntimeException e){
    		e.printStackTrace();
    		notifyDisconnection();
    	}
    	return cores;
    }

   

    @Override
    public void startWorkflow(OnlineWorkflowStartParameters start, OnlineWorkflowParameters params) {
        try {
        	application.startWorkflow(start,  params);
            
        } catch (Exception  e) {
          e.printStackTrace();
          notifyDisconnection();
        }
    }

    @Override
    public void stopWorkflow() {
    	
        try {
        	application.stopWorkflow();
        } catch (Exception e) {
        	e.printStackTrace();
        	notifyDisconnection();
        }
        
    }

   

    @Override
    public void notifyListeners() {
        application.notifyListeners();
    }

    @Override
    public void close() throws Exception {
        mbsc.removeNotificationListener(new ObjectName(LocalOnlineApplicationMBean.BEAN_NAME), this);
       
        connector.close();
    }

	@Override
	public void addListener(OnlineApplicationListener l) {
		listeners.add(l);
		
	}

	@Override
	public void removeListener(OnlineApplicationListener l) {
		listeners.remove(l);
		
	}

}
