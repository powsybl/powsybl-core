/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import eu.itesla_project.modules.contingencies.tasks.BreakerClosing;
import eu.itesla_project.contingency.tasks.ModificationTask;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SwitchClosingAction implements ActionElement {

	private String voltageLevelId;
	
	private String equipmentId;

	private Number implementationTime;
	
	private Number achievmentIndex;
	
	public SwitchClosingAction(String vlId, String switchId) {
		this.voltageLevelId=vlId;
		this.equipmentId=switchId;
	}
	
	public SwitchClosingAction(String vlId, String switchId, Number implementationTime, Number achievmentIndex ) {
		this.voltageLevelId=vlId;
		this.equipmentId=switchId;
		this.implementationTime=implementationTime;
		this.achievmentIndex=achievmentIndex;
	}

	@Override
	public ActionElementType getType() {
		return ActionElementType.SWITCH_CLOSE;	
	}
	
	public String getVoltageLevelId() {
		return voltageLevelId;
	}

	@Override
	public String getEquipmentId() {
		return equipmentId;
	}

	@Override
	public Number getImplementationTime() {
		return implementationTime;
	}

	@Override
	public Number getAchievmentIndex() {
		return achievmentIndex;
	}
	
	@Override
	public ModificationTask toTask() {
		return new BreakerClosing(voltageLevelId, equipmentId);
	}

	@Override
	public ModificationTask toTask(ActionParameters parameters) {
		throw new UnsupportedOperationException();
	}
	

}
