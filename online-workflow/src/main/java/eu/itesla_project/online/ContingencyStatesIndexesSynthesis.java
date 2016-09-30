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
import java.util.List;
import java.util.Map;

import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ContingencyStatesIndexesSynthesis implements Serializable {


   private static final long serialVersionUID = 1L;

   private final  Map<String,Map> contingencyMap=new HashMap<String,Map>();

   private String workflowId;
   
   public ContingencyStatesIndexesSynthesis() {

    }
   public ContingencyStatesIndexesSynthesis(String workflowId) {
	   this.workflowId=workflowId;
   }

   public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

    public void addStateIndexes(String contingencyId, Integer stateId, List<SecurityIndexInfo> indexes){
    	
		//Map<Integer,List<SecurityIndexInfo>> statesIndexes = contingencyMap.get(contingencyId);
    	Map<Integer,Map<SecurityIndexType,SecurityIndexInfo>> statesIndexes = contingencyMap.get(contingencyId);
    	
		if(statesIndexes==null)
			//statesIndexes = new HashMap<Integer,List<SecurityIndexInfo>>();
			statesIndexes = new HashMap<Integer,Map<SecurityIndexType,SecurityIndexInfo>>();
		
		Map<SecurityIndexType,SecurityIndexInfo> mapIndexType = new  EnumMap<SecurityIndexType,SecurityIndexInfo>(SecurityIndexType.class);
		
		
		//for (Map.Entry entry : mapIndexType.entrySet()) {
		  //  System.out.println("mapIndexType:   "+ entry.getKey() + ", " + entry.getValue());
		//}
		
		for (int i=0; i<SecurityIndexType.values().length; i++){
			mapIndexType.put(SecurityIndexType.values()[i], new SecurityIndexInfo());
			
		}
		
		for (SecurityIndexInfo index :indexes){
			mapIndexType.put(SecurityIndexType.fromLabel(index.type), index);
		}
		
		statesIndexes.put(stateId, mapIndexType);
		System.out.println(" state index stateid: "+ stateId + ", " + mapIndexType);

		contingencyMap.put(contingencyId, statesIndexes);
		
		
		
    }
    
    public class SecurityIndexInfo implements Serializable{
    	
    	String description;
    	boolean ok;
    	String type;
    	String value;
    	
    	public SecurityIndexInfo(SecurityIndex idx)
    	{
    		
    		this.type		  = idx.getId().getSecurityIndexType().getLabel();
    		this.description  = idx.toString();    		
    		this.ok=idx.isOk();
    	}
    	
    	public SecurityIndexInfo()
    	{
    		
    	
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
		
		public String getValue(){
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public boolean isOk() {
			return ok;
		}
		public void setOk(boolean ok) {
			this.ok = ok;
		}
    	
		
		
    }

}
