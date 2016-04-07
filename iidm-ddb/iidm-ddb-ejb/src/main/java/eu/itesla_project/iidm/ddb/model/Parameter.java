/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Entity implementation class for Entity: Parameter
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="PARAMETER")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="PAR_TYPE", discriminatorType=DiscriminatorType.STRING)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Parameter implements Serializable {
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

    @Column(nullable=false)
    @NotEmpty
    protected String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	protected Parameter() {
		super();
	}
	public Parameter(String name) {
		super();
		this.name=name;
	}
   
	public abstract Object getValue();
}
