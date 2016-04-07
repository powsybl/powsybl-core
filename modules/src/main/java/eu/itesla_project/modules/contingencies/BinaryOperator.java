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
public class BinaryOperator implements Operator {

	
	
	private OperatorType type;
	private Operator first;
	private Operator second;
	
	
	public BinaryOperator(OperatorType type, Operator first, Operator second) {
		this.type=type;
		this.first=first;
		this.second=second;
	}
	
	public OperatorType getType(){
		return type;
	}
	
	public Operator getFirstOperator(){
		return first;
	}
	public Operator getSecondOperator(){
		return second;
	}
	
	public String toString(){
		StringBuffer sb=new StringBuffer();
		sb.append("("+first);
		sb.append(" "+getType()+" ");
		sb.append(second+")");
		
		return sb.toString();
	}
}
