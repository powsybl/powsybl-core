/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.converter;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@FacesConverter(forClass=ParametersContainer.class,value="parametersContainerConverter")
@ManagedBean
public class ParametersContainerConverter implements Converter{
		   
	@EJB
	private DDBManager dbManager;
	
	    public Object getAsObject(FacesContext context, UIComponent component, String value) {
	    	// Convert the unique String representation of SimulatorInst to the actual SimulatorInst object.	    
	    	System.out.println("getAsObject :"+value);
	    	ParametersContainer pc=dbManager.findParametersContainer(value);
	    	return pc;
	    }

	    public String getAsString(FacesContext context, UIComponent component, Object value) {
	        // Convert the SimulatorInt object to its unique String representation.
	    	
	    	System.out.println("getAsString :"+value);
	    	
	    	ParametersContainer pc = (ParametersContainer) value;
	        return pc.toString();

	    }

	}


