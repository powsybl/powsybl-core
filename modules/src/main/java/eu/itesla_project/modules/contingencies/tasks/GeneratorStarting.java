/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.contingency.tasks.ModificationTask;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorStarting implements ModificationTask {

    private final String generatorId;

    public GeneratorStarting(String generatorId) {
        this.generatorId = generatorId;
    }

    @Override
    public void modify(Network network) {
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            throw new ITeslaException("Generator '" + generatorId + "' not found");
        }
        g.getTerminal().connect();
    }

}
