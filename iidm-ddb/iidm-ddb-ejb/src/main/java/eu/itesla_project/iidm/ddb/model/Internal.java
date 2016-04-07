/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.constraints.NotEmpty;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(
	name = "INTERNAL",  
	uniqueConstraints={
		   @UniqueConstraint (columnNames={"nativeId"})
	}
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Internal  implements Serializable{
	private static final long serialVersionUID = 1L;

	// The synthetic id of the object.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}
//	public void setId(Long id) {
//		this.id = id;
//	}

	//TBD: unique=true to be matched with versions&co 
	@Column(nullable=false)
	@NotEmpty
	private String nativeId;
	public String getNativeId() {
		return nativeId;
	}
	
	//LP if thie method is not defined, it's impossible to create a new Internal from interface (internals/create.jsf)
	public void setNativeId(String nativeId) {
		this.nativeId = nativeId;
	}

	
	
	public ModelTemplateContainer getModelContainer() {
		return modelContainer;
	}

	public void setModelContainer(ModelTemplateContainer modelContainer) {
		this.modelContainer = modelContainer;
	}

	public ParametersContainer getParametersContainer() {
		return parametersContainer;
	}

	public void setParametersContainer(ParametersContainer parametersContainer) {
		this.parametersContainer = parametersContainer;
	}



	//@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@ManyToOne(fetch=FetchType.EAGER)
	ModelTemplateContainer modelContainer;

	//@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@ManyToOne(fetch=FetchType.EAGER)
	ParametersContainer parametersContainer;
	
	protected Internal() {
	}
	
	public Internal(String nativeId) {
		this.nativeId=nativeId;
	}
	
}
