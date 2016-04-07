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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.Transient;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="TABLEROW")
public class TableRow implements Serializable{

	private static final long serialVersionUID = 1L;

	//The synthetic id of the object.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    public Long getId() {
        return id;
    }
//    public void setId(Long id) {
//        this.id = id;
//    }
    
    /* if List had been in place, without the OrderColumn annotation, hibernate/JPA implementation would have produced an error
     * ERROR: org.hibernate.LazyInitializationException failed to lazily initialize a collection of role: xxx.entity.core.User.roleSet, no session or session was closed
     * Apparently, two nested OneToMany relation handling is not supported  
     * ref http://blog.eyallupu.com/2010/06/hibernate-exception-simultaneously.html
     *     http://en.wikibooks.org/wiki/Java_Persistence/Relationships
     */
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinTable(name="TABLEROW_PARAMETER", joinColumns={@JoinColumn(name="T_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="P_ID", referencedColumnName="id")})
    @OrderColumn(name="tbindx")
	private List<Parameter> elements = new ArrayList<Parameter>();
	public List<Parameter> getElements() {
		return elements;
	}
	public void setElements(List<Parameter> parameters) {
		this.elements = parameters;
	}

	public void addParameter(Parameter parameter) {
		this.getElements().add(parameter);
	}

	public TableRow() {
		super();
	}
	
	public TableRow(Parameter...parameters) {
		super();
		 for (Parameter parameter : parameters) {
		      addParameter(parameter);
		  }
	}
}
