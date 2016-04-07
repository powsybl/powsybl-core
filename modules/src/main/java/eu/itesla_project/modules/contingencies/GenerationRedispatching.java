/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import eu.itesla_project.modules.contingencies.tasks.GeneratorsRedispatching;
import eu.itesla_project.modules.contingencies.tasks.ModificationTask;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GenerationRedispatching implements ActionElement {

    private final Number implementationTime;

    private final Number achievmentIndex;

    private final List<String> generatorIds;

    public GenerationRedispatching(List<String> generatorIds, Number achievmentIndex, Number implementationTime) {
        this.generatorIds = generatorIds;
        this.achievmentIndex = achievmentIndex;
        this.implementationTime = implementationTime;
    }

    public List<String> getGeneratorIds() {
        return generatorIds;
    }

    @Override
    public Number getAchievmentIndex() {
        return achievmentIndex;
    }

    @Override
    public String getEquipmentId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionElementType getType() {
        return ActionElementType.GENERATION_REDISPATCHING;
    }

    @Override
    public ModificationTask toTask() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number getImplementationTime() {
        return implementationTime;
    }

	@Override
	public ModificationTask toTask(ActionParameters parameters) {
		return new GeneratorsRedispatching(generatorIds, parameters);
	}
}
