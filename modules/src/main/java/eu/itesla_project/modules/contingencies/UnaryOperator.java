/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class UnaryOperator implements Operator {
	
	
	private String actionId;
	
	public UnaryOperator(String actionId){
		this.actionId=actionId;
	}

	public String getActionId(){
		return actionId;
	}

	public String toString(){
		StringBuffer sb=new StringBuffer();
		sb.append(getActionId());
		return sb.toString();
	}
}
