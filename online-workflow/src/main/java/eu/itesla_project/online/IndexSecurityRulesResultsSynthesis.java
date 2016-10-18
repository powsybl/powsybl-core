/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class IndexSecurityRulesResultsSynthesis implements Serializable {


   private static final long serialVersionUID = 1L;
   
   private String workflowId;

   
   private final  Map<String,Map<Integer,StateInfo>> contingencySecurityRulesMap=new HashMap<String,Map<Integer,StateInfo>>();

  
   public IndexSecurityRulesResultsSynthesis(String workflowId ) {
	   this.workflowId=workflowId;
   }
   
   
   
  /* public IndexSecurityRulesResultsSynthesis(String workflowId, SecurityRulesApplicationResults results)
   {
	   this.workflowId=workflowId;
	   //set of contingencies
	   Set<String> contingenciesId=results.getContingenciesWithSecurityRulesResults();
	   
	   for (String contingencyId: contingenciesId) 
	   {
		
		   Map<Integer,StateInfo> stateIndexStatus = new Hashtable<Integer,StateInfo>();
		   
		   for (Integer stateId : results.getStatesWithSecurityRulesResults(contingencyId)) 
		   {
			   Map<SecurityIndexType, StateStatus> mapIndexTypeSecurityRulesResults =results.getSecurityRulesResults(contingencyId, stateId);
			   StateStatus stateStatus=results.getStateStatus(contingencyId, stateId);
			   StateInfo stateInfo = new StateInfo (stateId, stateStatus, mapIndexTypeSecurityRulesResults);
			   stateIndexStatus.put(stateId,stateInfo);
		   }
		   contingencySecurityRulesMap.put(contingencyId, stateIndexStatus);
		   
	   }
	   
	   
   }*/

   public String getWorkflowId() 
   {
		return workflowId;
   }
   
   public void setWorkflowId(String workflowId) 
   {
		this.workflowId = workflowId;
   }

   
   
   
   public void addStateSecurityRuleIndexes(String contingencyId, Integer stateId, SecurityRulesApplicationResults rulesApplicationResults)
   {
	   Map<SecurityIndexType, StateStatus> indexStatus = rulesApplicationResults.getSecurityRulesResults(contingencyId,stateId);
	   StateStatus stateStatus	= rulesApplicationResults.getStateStatus(contingencyId, stateId);	
	   StateInfo stateInfo 		= new StateInfo (stateId, stateStatus, indexStatus);
	  
	   if (contingencySecurityRulesMap.containsKey(contingencyId)) {
		   Map<Integer,StateInfo> stateIndexInfo = contingencySecurityRulesMap.get(contingencyId);
		   stateIndexInfo.put(stateId,stateInfo);
    	   this.contingencySecurityRulesMap.put(contingencyId, stateIndexInfo);
    	}
    	else
    	{	
    		Map<Integer,StateInfo> stateIndexStatus = new Hashtable<Integer,StateInfo>();
    		stateIndexStatus.put(stateId,stateInfo);
    		this.contingencySecurityRulesMap.put(contingencyId, stateIndexStatus);
    	}
	   
    }
    
	/*
   public void addStateSecurityRuleIndexes(String contingencyId, StateInfo stateInfo, List<SecurityRulesResults> results)
   {
    	
    	Map<StateInfo,Map<SecurityIndexType,SecurityRulesResults>> statesIndexesSecurityRules = contingencySecurityRulesMap.get(contingencyId);
    	
		if(statesIndexesSecurityRules==null)
			statesIndexesSecurityRules = new HashMap<StateInfo,Map<SecurityIndexType,SecurityRulesResults>>();
		
		Map<SecurityIndexType,SecurityRulesResults> mapIndexTypeSR = new  EnumMap<SecurityIndexType,SecurityRulesResults>(SecurityIndexType.class);
		
		for (int i=0; i<SecurityIndexType.values().length; i++)
			mapIndexTypeSR.put(SecurityIndexType.values()[i], new SecurityRulesResults());
			
		
		
		for (SecurityRulesResults index :results)
			mapIndexTypeSR.put(SecurityIndexType.fromLabel(index.type), index);
		
		
		//System.out.println("addStateSecurityRuleIndexes stateInfo:   "+ stateInfo.getStateId() + " "+stateInfo.getStatus());
	
		statesIndexesSecurityRules.put(stateInfo, mapIndexTypeSR);
		
		
		contingencySecurityRulesMap.put(contingencyId, statesIndexesSecurityRules);
		
		
		//Map<String, StateInfo> statesIndexesSecurityRules = contingencySecurityRulesMap.get(contingencyId)
		
    }*/
    
  

   
   
    
    public class SecurityRulesResults implements Serializable
    {
    	
    	String type;
    	String description;
    	StateStatus status;
    	
    	public SecurityRulesResults(SecurityIndexType idx, StateStatus status)
    	{
    		
    		this.type		  = idx.getLabel();
    		this.description  = idx.toString();    	
    		this.status		  = status;
    		
    	}
    	
    	public SecurityRulesResults() {
    			this.description  = "";    
    	}
    	
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public StateStatus getStatus(){
			return status;
		}
		
		public void setValue(StateStatus status) {
			this.status = status;
		}
		
    }
    
    
    public class StateInfo implements Serializable  {
    	
    	Integer stateId;
    	StateStatus status;
    	String statusCode;
    	
    

		Map<SecurityIndexType,SecurityRulesResults> indexTypeSecurityRulesResults;
    	
    	
    	
    	public Map<SecurityIndexType, SecurityRulesResults> getIndexTypeSecurityRulesResults() {
			return indexTypeSecurityRulesResults;
		}

		public void setIndexTypeSecurityRulesResults(
				Map<SecurityIndexType, SecurityRulesResults> indexTypeSecurityRulesResults) {
			this.indexTypeSecurityRulesResults = indexTypeSecurityRulesResults;
		}

		public StateInfo(Integer stateId, StateStatus stateStatus,	Map<SecurityIndexType, StateStatus> results) 
		{
			this.status = stateStatus;
			this.stateId=stateId;
		
			switch(stateStatus){
			case SAFE:
				this.statusCode="S";
				break;
				
			case UNSAFE:
				this.statusCode="U";
				break;	
			case SAFE_WITH_CORRECTIVE_ACTIONS:
				this.statusCode="SWCA";
				break;
			default:		
				this.statusCode="";
				break;
			}
			
			
			Map<SecurityIndexType,SecurityRulesResults> mapIndexTypeSR = new  EnumMap<SecurityIndexType,SecurityRulesResults>(SecurityIndexType.class);
			
			for (int i=0; i<SecurityIndexType.values().length; i++)	{
				if (results.containsKey(SecurityIndexType.values()[i])){
					StateStatus status=results.get(SecurityIndexType.values()[i]);
					mapIndexTypeSR.put(SecurityIndexType.values()[i],new SecurityRulesResults(SecurityIndexType.values()[i], status));
				}
				else
					mapIndexTypeSR.put(SecurityIndexType.values()[i], new SecurityRulesResults());
				
			}
			
			this.indexTypeSecurityRulesResults=mapIndexTypeSR;
			
		}

		public Integer getStateId() 
    	{
			return stateId;
		}
		
		public void setStateId(Integer _stateId) 
		{
			this.stateId = _stateId;
		}
		
		
		public StateStatus getStatus()
		{
			return this.status;
		}
		
		
		public void setStatus(StateStatus _status)
		{
			this.status= _status;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}

	}  

}
