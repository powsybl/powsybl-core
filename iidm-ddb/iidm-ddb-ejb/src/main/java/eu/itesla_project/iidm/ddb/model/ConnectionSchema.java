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
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
@Table(
		name = "CONNECTIONSCHEMA",  
		uniqueConstraints={
			   @UniqueConstraint (columnNames={"cimId","simulator_id"})
		}
	)
@XmlAccessorType(XmlAccessType.FIELD)
public class ConnectionSchema implements Serializable{
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
    
    @Column(nullable=false,unique=false)
    @NotEmpty
    private String cimId;
	public String getCimId() {
		return cimId;
	}
	public void setCimId(String cimId) {
		this.cimId = cimId;
	}
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="CONNECTION")
	private List<Connection> connections = new ArrayList<Connection>();

	public List<Connection> getConnections() {
		return connections;
	}
	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	
	@OneToOne()
	private SimulatorInst simulator;

	
	public SimulatorInst getSimulator() {
		return simulator;
	}
	public void setSimulator(SimulatorInst simulator) {
		this.simulator = simulator;
	}
	protected ConnectionSchema() {
	}

	public ConnectionSchema(String cimId,SimulatorInst sim) {
		this.cimId = cimId;
		this.simulator=sim;
	}
	
	public ConnectionSchema(String cimId) {
		this.cimId = cimId;
		this.simulator=null;
	}
	
}
