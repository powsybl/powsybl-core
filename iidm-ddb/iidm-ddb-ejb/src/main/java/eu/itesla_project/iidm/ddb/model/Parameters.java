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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="PARAMETERS")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters implements Serializable {
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
    
    @OneToOne
	private SimulatorInst simulator;
	public SimulatorInst getSimulator() {
		return simulator;
	}
	public void setSimulator(SimulatorInst simulator) {
		this.simulator = simulator;
	}
    
    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)   
    @JoinTable(name="PARAMETERS_PARAMETER", joinColumns={@JoinColumn(name="PS_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="P_ID", referencedColumnName="id")})
    @OrderColumn(name="pindx")
	private List<Parameter> pars =new ArrayList<Parameter>();
	public List<Parameter> getParameters() {
		return pars;
	}
	public void setParameters(List<Parameter> parameters) {
		this.pars = parameters;
	}
	
	public boolean containsParameterWithName(String name) {
		boolean found=false;
		for (Parameter p : getParameters()) {
			if (p.name.equals(name)) {
				found=true;
				break;
			}
		}
		return found;
	}
	
	public void addParameter(Parameter parameter) {
		if (!containsParameterWithName(parameter.name)) {
			getParameters().add(parameter);
        } else {
        	throw new RuntimeException("parameter with name "+parameter.name+" already exist in this parameter list");
        }
	}

	private int defParamSetNum;
	public int getDefParamSetNum() {
		return defParamSetNum;
	}
	public void setDefParamSetNum(int defParamSetNum) {
		this.defParamSetNum = defParamSetNum;
	}
	
	public Parameters() {
		super();
	}
	
	public Parameters(SimulatorInst simulatorInst) {
		super();
		this.setSimulator(simulatorInst);
	}

	
}
