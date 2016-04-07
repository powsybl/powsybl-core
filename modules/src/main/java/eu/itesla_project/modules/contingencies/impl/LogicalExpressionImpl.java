/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;


import eu.itesla_project.modules.contingencies.LogicalExpression;
import eu.itesla_project.modules.contingencies.Operator;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LogicalExpressionImpl implements LogicalExpression {
	

	private Operator operator;
	
	public LogicalExpressionImpl (){
		
	}

	@Override
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator=operator;
		
	}

}
