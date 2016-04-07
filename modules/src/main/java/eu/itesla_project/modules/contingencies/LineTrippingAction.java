/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import eu.itesla_project.modules.contingencies.tasks.BranchTripping;
import eu.itesla_project.modules.contingencies.tasks.ModificationTask;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LineTrippingAction implements ActionElement {

    private final String equipmentId;
    
    private Number implementationTime;
	
	private Number achievmentIndex;
	
	private String substationId = null;

    public LineTrippingAction(String lineId) {
        this.equipmentId = lineId;
    }
    
    @Override
    public String getEquipmentId() {
        return equipmentId;
    }

    
    public LineTrippingAction(String lineId, Number implementationTime, Number achievmentIndex ) {
		this.equipmentId=lineId;
		this.implementationTime=implementationTime;
		this.achievmentIndex=achievmentIndex;
	}
    
    public LineTrippingAction(String lineId, String substationId, Number implementationTime, Number achievmentIndex ) {
		this.equipmentId=lineId;
		this.substationId = substationId;
		this.implementationTime=implementationTime;
		this.achievmentIndex=achievmentIndex;
	}
    
    @Override
    public ActionElementType getType() {
    	return ActionElementType.LINE_TRIPPING;
    }
    
    @Override
    public ModificationTask toTask() {
    	if ( substationId != null )
    		return new BranchTripping(equipmentId, substationId);
        return new BranchTripping(equipmentId);
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
	public ModificationTask toTask(ActionParameters parameters) {
		throw new UnsupportedOperationException();
	}

}
