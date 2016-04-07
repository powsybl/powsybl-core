/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.itesla_project.iidm.ddb.web.data;

import java.util.logging.Logger;

import javax.inject.Inject;

import eu.itesla_project.iidm.ddb.model.Parameter;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ParameterWeb {

    @Inject
	private Logger log;

    private String type;
    private String name;
    private Object value;
    
    
    public ParameterWeb() {
    	this.type = "";
		this.name = "";
		this.value = "";
		
	}
    
    public ParameterWeb(String type, String name, Object value) {
		super();
		this.type = type;
		this.name = name;
		this.value = value;
	}

    public ParameterWeb( String name, Object value) {
		super();
		this.type = null;
		this.name = name;
		this.value = value;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Object getValue() {
		return value;
	}


	public void setValue(Object value) {
		this.value = value;
	}

    
    public String toString(){
    	return "Parameter [ Type: " +this.type+ " Name: "+ this.name+" Value: "+this.value + "]";
    	
    }
    
  
    
}
