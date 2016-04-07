/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import eu.itesla_project.modules.contingencies.LogicalExpression;
import eu.itesla_project.modules.contingencies.ActionPlanOption;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OptionImpl implements ActionPlanOption {

	private BigInteger priority;
	
	private LogicalExpression logicalExpression;
	
	//[key= num value= actionId]
	private Map<BigInteger, String> actions = new HashMap<BigInteger,String>();
	
	
	
	public OptionImpl(BigInteger _priority, LogicalExpression _logicalExpression, Map<BigInteger, String> actions) 
	{
		this.priority=			_priority;
		this.logicalExpression= _logicalExpression;
		this.actions= 	actions;
	}

	@Override
	public BigInteger getPriority() 
	{
		return this.priority;
	}

	@Override
	public Map<BigInteger, String> getActions() 
	{
			return this.actions;
			
	}

	@Override
	public LogicalExpression getLogicalExpression() {
		
		return this.logicalExpression;
	}


}
