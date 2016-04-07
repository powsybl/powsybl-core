/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="PARAMETERSTRING")
@DiscriminatorValue("string")
public class ParameterString extends Parameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private String value;
	
	protected ParameterString() {
		super();
	}
	public ParameterString(String name, String value) {
		super();
		this.name=name;
		this.value=value;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
   
}
