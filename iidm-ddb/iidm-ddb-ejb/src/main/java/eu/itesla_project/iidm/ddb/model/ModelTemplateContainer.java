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
@Table(name="MODELTEMPLATECONTAINER")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelTemplateContainer  implements Serializable{

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
    
    @Column(name="ddbid", nullable=false, unique=true)
    @NotEmpty
    private String ddbId;
    
    
	public String getDdbId() {
		return ddbId;
	}
	public void setDdbId(String ddbId) {
		this.ddbId = ddbId;
	}
	
	private String comment;
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,fetch=FetchType.EAGER)
	@JoinTable(name="MODELTEMPLATECONTAINER_MODELTEMPLATE", joinColumns={@JoinColumn(name="MTC_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="MT_ID", referencedColumnName="id")})
	@OrderColumn(name="mtcindx")
	private List<ModelTemplate> modelTemplates= new ArrayList<ModelTemplate>();
	public List<ModelTemplate> getModelTemplates() {
		return modelTemplates;
	}
	public void setModelTemplates(List<ModelTemplate> modelTemplates) {
		this.modelTemplates = modelTemplates;
	}
	
	
	protected ModelTemplateContainer() {
	}
	
	public ModelTemplateContainer(String ddbId) {
		setDdbId(ddbId);
	}
	
	public ModelTemplateContainer(String ddbId, String comment) {
		setDdbId(ddbId);
		setComment(comment);
	}
	
	// add form L.P 
	@Override
	public String toString() {
		return this.getDdbId();
	}
	
	@Override
	// This must return true for another ModelTemplateContainer this method is used to manage <f:selectItems value=""/> 
    public boolean equals(Object other) {
        return other instanceof ModelTemplateContainer ? 
        		ddbId.equals( ( (ModelTemplateContainer) other).getDdbId())  : false;
    }

}
