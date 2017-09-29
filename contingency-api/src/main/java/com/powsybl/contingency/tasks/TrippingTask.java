/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.Terminal;

import java.util.Set;

/**
 * Use {@link AbstractTrippingTask} instead
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Deprecated
public class TrippingTask extends AbstractTrippingTask {

    @Override
    public void traverse(Network network, ComputationManager computationManager, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        throw new IllegalStateException("traverse has to be implemented");
    }
}
