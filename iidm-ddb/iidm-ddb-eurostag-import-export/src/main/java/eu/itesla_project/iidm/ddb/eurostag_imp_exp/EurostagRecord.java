/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EurostagRecord {
	String typeName;
	
	String keyName=null;
	
	HashMap<String,Object> data = new HashMap<String,Object>();

	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public HashMap<String, Object> getData() {
		return data;
	}
	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}
	public EurostagRecord(String typeName, HashMap<String, Object> data) {
		super();
		this.typeName = typeName;
		this.data = data;
	}

	public String getKeyName() {
		return keyName;
	}
	
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EurostagRecord [typeName=");
		builder.append(typeName);
		builder.append(", keyName=");
		builder.append(keyName);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}
	
	
	
	
}
