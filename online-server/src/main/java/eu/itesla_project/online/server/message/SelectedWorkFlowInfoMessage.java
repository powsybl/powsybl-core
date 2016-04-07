/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server.message;


import java.util.HashMap;


import javax.json.stream.JsonGenerator;

import com.google.gson.Gson;

import eu.itesla_project.online.server.OnlineWorkflowInfo;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SelectedWorkFlowInfoMessage extends Message<OnlineWorkflowInfo>  {

  
	private String type="selectedWorkFlowInfo";
    

    public SelectedWorkFlowInfoMessage(OnlineWorkflowInfo workflow) {
    	super(workflow);
        
    }
  

	@Override
	protected String getType() {
		return type;
	}
   
	@Override
	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	
	@Override
	protected void toJson(JsonGenerator generator) {
	
	}


}
