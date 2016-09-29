/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import eu.itesla_project.contingency.tasks.GeneratorTripping;
import eu.itesla_project.contingency.tasks.ModificationTask;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class GeneratorStopAction implements ActionElement {

	private String equipmentId;
	
	private Number implementationTime;
	
	private Number achievmentIndex;

	public GeneratorStopAction(String genId) {
		this.equipmentId=genId;
	}
	
	public GeneratorStopAction(String genId, Number implementationTime, Number achievmentIndex ) {
		this.equipmentId=genId;
		this.implementationTime=implementationTime;
		this.achievmentIndex=achievmentIndex;
	}

	@Override
	public ActionElementType getType() {
		
		 return ActionElementType.GENERATOR_STOP;
	}
	
	@Override
	public String getEquipmentId() {
		return equipmentId;
	}

	@Override
	public ModificationTask toTask() {
		return new GeneratorTripping(equipmentId);
	}

	@Override
	public Number getImplementationTime() {
		return implementationTime;
	}

	@Override
	public Number getAchievmentIndex() {
		return 	 achievmentIndex;	
	}

	@Override
	public ModificationTask toTask(ActionParameters parameters) {
		throw new UnsupportedOperationException();
	}

}
