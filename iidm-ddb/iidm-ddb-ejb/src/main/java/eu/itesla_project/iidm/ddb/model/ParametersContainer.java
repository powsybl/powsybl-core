/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="PARAMETERSCONTAINER")
@XmlAccessorType(XmlAccessType.FIELD)
public class ParametersContainer implements Serializable{
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
    
    @Column(nullable=false, unique=true)
    @NotEmpty
    private String ddbId;
    
    public String getDdbId() {
		return ddbId;
	}
	public void setDdbId(String ddbId) {
		this.ddbId = ddbId;
	}

	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER, orphanRemoval=true)   
	@JoinTable(name="PARAMETERSCONTAINER_PARAMETERS", joinColumns={@JoinColumn(name="PC_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="P_ID", referencedColumnName="id")})
	@OrderColumn(name="pcindx")
	private List<Parameters> parameters = new ArrayList<Parameters>();
	public List<Parameters> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameters> parameters) {
		this.parameters = parameters;
	}
	
	protected ParametersContainer() {
	}

	public ParametersContainer(String ddbId) {
		this.ddbId = ddbId;
	}
	
	// add from L.P
	@Override
	public String toString() {
		return this.getDdbId();
	}
	
	@Override
	// This must return true for another ParametersContainer this method is used to manage <f:selectItems value=""/> 
    public boolean equals(Object other) {
        return other instanceof ParametersContainer ? 
        		ddbId.equals( ( (ParametersContainer) other).getDdbId())  : false;
    }
}
