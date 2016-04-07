/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@Entity
@Table(name = "MODELTEMPLATE")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelTemplate  implements Serializable{
	private static final long serialVersionUID = 1L;

	//The synthetic id of the object.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	public Long getId() {
		return id;
	}
//	public void setId(Long id) {
//		this.id = id;
//	}

	@Column(name="typename")
    private String typeName="";
    
    
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	

	private String comment;
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
	
	@OneToOne
	private SimulatorInst simulator;
	public SimulatorInst getSimulator() {
		return simulator;
	}

	public void setSimulator(SimulatorInst simulator) {
		this.simulator = simulator;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)   
	@JoinTable(name="MODELTEMPLATE_DEFAULTPARAMETERS", joinColumns={@JoinColumn(name="MT_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="DP_ID", referencedColumnName="id")})
	@OrderColumn(name="mtindx")
	private List<DefaultParameters> defaultParameters=new ArrayList<DefaultParameters>();


	public List<DefaultParameters> getDefaultParameters() {
		return defaultParameters;
	}

	public void setDefaultParameters(List<DefaultParameters> defaultParameters) {
		this.defaultParameters = defaultParameters;
	}
	
	public DefaultParameters defaultParametersBySetNum(int defSetNum) {
		int defParsSetsSize=defaultParameters.size();
		if ((defParsSetsSize>0)) {
			DefaultParameters foundDP=null;
			for (DefaultParameters defaultParams : defaultParameters) {
				if (defaultParams.getSetNum()==defSetNum) {
					foundDP=defaultParams;
					break;
				}
			}
			return ((foundDP!=null) ? foundDP : null);
		}
		return null;
	}

	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="MODEL_DATA")
	@MapKeyColumn(name="DNAME")
	private Map<String,ModelData> modelDataMap=new HashMap<String, ModelData>();;

	public Map<String, ModelData> modelDataMap() {
		return modelDataMap;
	}

	public void setMdata(Map<String, ModelData> modelDataMap) {
		this.modelDataMap = modelDataMap;
	}
	
	public byte[] getData(String name){
		if (this.modelDataMap.containsKey(name)) {
			return this.modelDataMap.get(name).getData();
		} else {
			return null;
		}
	}
	
	public void setData(String name, byte[] data){
		this.modelDataMap().put(name, new ModelData(data));
	}
	
	public ModelTemplate() {
	}

	public ModelTemplate(SimulatorInst simulatorInst, String comment) {
		this.simulator=simulatorInst;
		this.comment = comment;
	}

	public ModelTemplate(SimulatorInst simulatorInst, String typeName, String comment) {
		this.simulator=simulatorInst;
		this.typeName=typeName;
		this.comment = comment;
	}

}
