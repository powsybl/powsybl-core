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

import eu.itesla_project.modules.online.StateProcessingStatus;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WorkStatus implements StateProcessingStatus,Serializable {

	private Integer stateId;
	
	private String timeHorizon;
	
	private HashMap<String,String> status;
	
	private String detail;
	
	

	public WorkStatus(Integer stateId, EnumMap<OnlineTaskType, OnlineTaskStatus> st, String time)
	{
		this.stateId=stateId;
		this.timeHorizon=time;
		this.status=new HashMap<String, String>();
				for(OnlineTaskType k :st.keySet())
					status.put(k.toString(), st.get(k).toString());
	}
	
	public WorkStatus(Integer stateId, EnumMap<OnlineTaskType, OnlineTaskStatus> st, String time, String detail)
	{
		this.stateId=stateId;
		this.timeHorizon=time;
		this.status=new HashMap<String, String>();
				for(OnlineTaskType k :st.keySet())
					status.put(k.toString(), st.get(k).toString());
		this.detail=detail;
	}
	
	public Integer getStateId() {
		return stateId;
	}
	
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
	
	@Override
	public HashMap getStatus() {
		return status;
	}
	public void setStatus(HashMap<String, String> status) {
		this.status = status;
	}
	
	public String getTimeHorizon() {
		return timeHorizon;
	}

	public void setTimeHorizon(String timeHorizon) {
		this.timeHorizon = timeHorizon;
	}
	
	@Override
	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	//new
	public void setStatus(EnumMap<OnlineTaskType, OnlineTaskStatus> st){
		this.status=new HashMap<String, String>();
		for(OnlineTaskType k :st.keySet())
			status.put(k.toString(), st.get(k).toString());
	}
	
}
