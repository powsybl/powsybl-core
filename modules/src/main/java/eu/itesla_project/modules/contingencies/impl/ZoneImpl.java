/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package eu.itesla_project.modules.contingencies.impl;


import java.math.BigInteger;
import java.util.List;



import eu.itesla_project.modules.contingencies.VoltageLevel;
import eu.itesla_project.modules.contingencies.Zone;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ZoneImpl implements Zone {

	private String name;
	
	private String description;
	
	private BigInteger number;
	
	private List<VoltageLevel> voltageLevels;
	
	
	public ZoneImpl(String _name, BigInteger _number, List<VoltageLevel> _voltageLevels ) 
	{
		this.name =_name;
		this.number = _number;
		this.voltageLevels = _voltageLevels;
		
	}
	
	public ZoneImpl(String _name, BigInteger _number, List<VoltageLevel> _voltageLevels, String _description) 
	{
		this.name =_name;
		this.number = _number;
		this.voltageLevels = _voltageLevels;
		this.description = _description;
		
	}

	@Override
	public String getName() 
	{
			return this.name;
	}
	
	
	@Override
	public BigInteger getNumber() 
	{	
		return this.number;
	}

	@Override
	public List<VoltageLevel> getVoltageLevels() 	
	{
		return voltageLevels;
	}

	@Override
	public String getDescription() {
		
		return this.description;
	}

}
