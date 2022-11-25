/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public abstract class AbstractTripping extends AbstractNetworkModification implements Tripping {

    protected final String id;

    protected AbstractTripping(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();

        traverse(network, switchesToOpen, terminalsToDisconnect);

        switchesToOpen.forEach(s -> s.setOpen(true));
        terminalsToDisconnect.forEach(Terminal::disconnect);
    }
}
