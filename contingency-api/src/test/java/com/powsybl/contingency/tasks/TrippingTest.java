/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class TrippingTest {

    protected List<Boolean> getSwitchStates(Network network, Set<String> switchIds) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(switchIds);

        return network.getSwitchStream()
                .filter(s -> !switchIds.contains(s.getId()))
                .map(Switch::isOpen)
                .collect(Collectors.toList());
    }
}
