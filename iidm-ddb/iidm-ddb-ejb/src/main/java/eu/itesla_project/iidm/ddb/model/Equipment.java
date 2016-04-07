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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="EQUIPMENT")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Equipment implements Serializable{
	private static final long serialVersionUID = 1L;

	//The synthetic id of the object.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() {
        return id;
    }
//    public void setId(Long id) {
//        this.id = id;
//    }
    
    //TBD: unique=true to be matched with versions&co 
    @Column(nullable=false,unique=true)
    @NotEmpty
    private String cimId;
	public String getCimId() {
		return cimId;
	}
	public void setCimId(String cimId) {
		this.cimId = cimId;
	}
    
	//@OneToOne(cascade = CascadeType.PERSIST, fetch=FetchType.EAGER)
	//@OneToOne(fetch=FetchType.EAGER)
	@ManyToOne(fetch=FetchType.EAGER)
	ModelTemplateContainer modelContainer;  
    
	//@OneToOne(cascade = CascadeType.PERSIST, fetch=FetchType.EAGER)
	@ManyToOne(fetch=FetchType.EAGER)
	ParametersContainer parametersContainer;
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
    
	protected Equipment() {
	}

	public Equipment(String cimId) {
		this.cimId=cimId;
	}
	
}
