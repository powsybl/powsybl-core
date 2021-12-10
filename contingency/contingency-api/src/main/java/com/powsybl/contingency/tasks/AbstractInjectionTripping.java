/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public abstract class AbstractInjectionTripping extends AbstractTrippingTask {

    protected final String id;

    public AbstractInjectionTripping(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public void traverse(Network network, ComputationManager computationManager, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        Objects.requireNonNull(network);

        ContingencyTopologyTraverser.traverse(getInjection(network).getTerminal(), switchesToOpen, terminalsToDisconnect);
    }

    protected abstract Injection getInjection(Network network);

}
