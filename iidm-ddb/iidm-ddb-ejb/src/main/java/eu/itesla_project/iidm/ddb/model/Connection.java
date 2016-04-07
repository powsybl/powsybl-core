/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Embeddable
public class Connection implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int EQUIPMENT_TYPE=0;
	public static final int INTERNAL_TYPE=1;
	public static final int INSIDE_CONNECTION=0;
	public static final int OUTSIDE_CONNECTION=1;

	String id1=null;  //nativeid or cimId
	int id1Type;  //0 equipment, 1 internal
	String id2=null; //nativeid or cimId
	int id2Type;  //0 equipment, 1 internal
	String conPointName1=null;
	String conPointName2=null;
	int conType=0;  //0 inside, 1 outside
	
	public String getId1() {
		return id1;
	}

	public void setId1(String id1) {
		this.id1 = id1;
	}

	public int getId1Type() {
		return id1Type;
	}

	public void setId1Type(int id1Type) {
		this.id1Type = id1Type;
	}

	public String getId2() {
		return id2;
	}

	public void setId2(String id2) {
		this.id2 = id2;
	}

	public int getId2Type() {
		return id2Type;
	}

	public void setId2Type(int id2Type) {
		this.id2Type = id2Type;
	}

	public String getConPointName1() {
		return conPointName1;
	}

	public void setConPointName1(String conPointName1) {
		this.conPointName1 = conPointName1;
	}

	public String getConPointName2() {
		return conPointName2;
	}

	public void setConPointName2(String conPointName2) {
		this.conPointName2 = conPointName2;
	}

	public int getConType() {
		return conType;
	}

	public void setConType(int conType) {
		this.conType = conType;
	}

	public Connection() {
	}
	
	/**
	 * @param id1 ; the cimID of an Equipment or the nativeId for an Internal
	 * @param id1Type ; 0 Equipment, 1 Internal
	 * @param id2 ; the cimID of an Equipment or the nativeId for an Internal
	 * @param id2Type ; 0 Equipment, 1 Internal
	 * @param conPointName1 ;  the connection point name for id1
	 * @param conPointName2 ;  the connection point name for id2
	 * @param conType ; 0 inside connection, 1 outside connection
	 */
	public Connection(String id1, int id1Type, String id2, int id2Type, String conPointName1,
			String conPointName2, int conType) {
		super();
		this.id1 = id1;
		this.id1Type=id1Type;
		this.id2 = id2;
		this.id2Type=id2Type;
		this.conPointName1 = conPointName1;
		this.conPointName2 = conPointName2;
		this.conType = conType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Connection [id1=");
		builder.append(id1);
		builder.append(", id1Type=");
		builder.append(id1Type);
		builder.append(", id2=");
		builder.append(id2);
		builder.append(", id2Type=");
		builder.append(id2Type);
		builder.append(", conPointName1=");
		builder.append(conPointName1);
		builder.append(", conPointName2=");
		builder.append(conPointName2);
		builder.append(", conType=");
		builder.append(conType);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conPointName1 == null) ? 0 : conPointName1.hashCode());
		result = prime * result
				+ ((conPointName2 == null) ? 0 : conPointName2.hashCode());
		result = prime * result + conType;
		result = prime * result + ((id1 == null) ? 0 : id1.hashCode());
		result = prime * result + id1Type;
		result = prime * result + ((id2 == null) ? 0 : id2.hashCode());
		result = prime * result + id2Type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Connection other = (Connection) obj;
		if (conPointName1 == null) {
			if (other.conPointName1 != null)
				return false;
		} else if (!conPointName1.equals(other.conPointName1))
			return false;
		if (conPointName2 == null) {
			if (other.conPointName2 != null)
				return false;
		} else if (!conPointName2.equals(other.conPointName2))
			return false;
		if (conType != other.conType)
			return false;
		if (id1 == null) {
			if (other.id1 != null)
				return false;
		} else if (!id1.equals(other.id1))
			return false;
		if (id1Type != other.id1Type)
			return false;
		if (id2 == null) {
			if (other.id2 != null)
				return false;
		} else if (!id2.equals(other.id2))
			return false;
		if (id2Type != other.id2Type)
			return false;
		return true;
	}


}
