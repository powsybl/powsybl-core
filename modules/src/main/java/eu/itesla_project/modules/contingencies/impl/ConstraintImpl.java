/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import eu.itesla_project.modules.contingencies.Constraint;
import eu.itesla_project.modules.contingencies.ConstraintType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintImpl implements Constraint {
	
	
	private String equipment;
	private Number value;
	private ConstraintType type;
	
	
	public ConstraintImpl(String _equipment, Number _value, ConstraintType _type)
	{
		this.equipment = _equipment;
		this.value     = _value;
		this.type	   =  _type;
		
	}

	@Override
	public String getEquipment() 
	{
		
		return equipment;
	}

	@Override
	public Number getValue() 
	{
		return value;
	}

	@Override
	public ConstraintType getType()
	{
		return type;
	}

	
	public String toString()
	{
		StringBuffer sb= new StringBuffer();
		sb.append(" Equipment: ").append(this.equipment).append(" , Value: ").append(this.value).append(" , Type: ").append(this.type);
		return sb.toString();
		
	}
	
}
