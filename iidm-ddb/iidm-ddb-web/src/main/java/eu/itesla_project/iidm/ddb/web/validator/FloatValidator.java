/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.validator;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@FacesValidator(value = "floatValidator")
public class FloatValidator implements Validator {
 
    @Override
    public void validate(FacesContext context, UIComponent component, Object objValue) throws ValidatorException {
        String objString = objValue.toString();
    	ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
        boolean valid = true;
        try {
        	Float valueFloat=Float.valueOf(objString);
        }catch(NumberFormatException nEx){
        	valid=false;
        	
        }
        if (!valid) {
            FacesMessage message = new FacesMessage( FacesMessage.SEVERITY_ERROR, bundle.getString("invalid.float.summary.msg"),
            		bundle.getString("invalid.float.detail.msg"));
            		
            throw new ValidatorException(message);
        }
    }
}
