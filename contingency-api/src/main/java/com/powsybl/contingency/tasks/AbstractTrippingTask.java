/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractTrippingTask implements ModificationTask {

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();

        traverse(network, computationManager, switchesToOpen, terminalsToDisconnect);

        switchesToOpen.forEach(s -> s.setOpen(true));
        terminalsToDisconnect.forEach(Terminal::disconnect);
    }

    public abstract void traverse(Network network, ComputationManager computationManager, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect);
}
