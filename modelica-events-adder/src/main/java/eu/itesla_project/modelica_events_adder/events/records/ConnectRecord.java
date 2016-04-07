/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.records;


/**
 * Class of Connect record
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectRecord {
	
	public ConnectRecord(String nodeF, String nodeT, String connectLine) {
        this.nodeF = nodeF;
        this.nodeT = nodeT;
        this.connectLine = connectLine;
    }

	public boolean containsElement(String elemModName) {
		boolean contains = false;
		
		if(this.nodeF.equalsIgnoreCase(elemModName) || this.nodeT.equalsIgnoreCase(elemModName)) {
			contains = true;
		}
		
		return contains;
	}
	
	public String getConnectedElement(String elemModName) {
		String connectedElem = null;
		
		if(this.nodeF.equalsIgnoreCase(elemModName)) {
			connectedElem = this.nodeT;
		}
		else if(this.nodeT.equalsIgnoreCase(elemModName)) {
			connectedElem = this.nodeF;
		}
		
		return connectedElem;
	}
        
    public String getNodeF() {
		return nodeF;
	}


	public void setNodeF(String nodeF) {
		this.nodeF = nodeF;
	}


	public String getNodeT() {
		return nodeT;
	}


	public void setNodeT(String nodeT) {
		this.nodeT = nodeT;
	}

	public String getConnectLine() {
		return connectLine;
	}


	public void setConnectLine(String connectLine) {
		this.connectLine = connectLine;
	}
	protected String			nodeF			= null;
	protected String			nodeT			= null;
	protected String			connectLine		= null;
}
