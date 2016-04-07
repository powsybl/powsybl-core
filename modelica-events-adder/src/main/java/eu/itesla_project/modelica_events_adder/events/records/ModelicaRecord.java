/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.records;

import java.util.HashMap;
import java.util.Map;

import eu.itesla_project.modelica_events_adder.events.utils.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public abstract class ModelicaRecord {
	
	public ModelicaRecord() {
		this.data = new StringBuilder();
		this.currentLinePos = 2;
		this.data.append(String.format("%-" + this.currentLinePos + "s" , ""));
	}
	
	public void addValue(String nodeName) {
		this.data.append(nodeName);
	}

	public void deleteInicialWhiteSpaces(int whiteSpaces) {
		this.data.delete(0, whiteSpaces);
	}
	
	public void newLine() {
		this.currentLinePos = 2;
		this.data.append(StaticData.NEW_LINE);
	}

	public String toString() {
		return this.data.toString();
	}

	public String getModelicaName() {
		return this.modelicaName;
	}
	
	public void setModelicaName(String modelicaName) {
		this.modelicaName =  modelicaName;
	}
	
	public void  setModelicaType(String modelicaType) {
		this.modelicaType = modelicaType;
	}

	public String getModelicaType()
	{
		return modelicaType;
	}
	
	public String getModelData() {
		return modelData;
	}

	public void setModelData(String modelData) {
		this.modelData = modelData;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	
    public abstract void createModelicaName();

    public abstract void createRecord();
    
    public abstract String parseName(String name);
	
    public abstract ModelicaRecord getClassName();
    
    private StringBuilder		data;
    protected int				currentLinePos;
	private String				modelicaName;
    private String				modelicaType	= null;
    
    public String				modelData		= null;
    
    private boolean				isCorrect		= true;

    public Map<String, String>	mtcMapper		= new HashMap<String, String>();
}
