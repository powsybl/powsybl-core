/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import java.math.BigInteger;

import eu.itesla_project.modules.contingencies.VoltageLevel;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class VoltageLevelImpl implements VoltageLevel {
	
	private String id;
	private  BigInteger level;
	
	public VoltageLevelImpl(String id, BigInteger level){
		this.id=id;
		this.level=level;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public BigInteger getLevel() {
		return level;
	}

}
