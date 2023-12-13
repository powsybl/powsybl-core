/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ConnectableConnection extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectableConnection.class);
    final String connectableId;
    Predicate<Switch> isTypeSwitchToOperate = SwitchPredicates.IS_NON_NULL;

    ConnectableConnection(String connectableId, boolean openFictitiousSwitches, boolean operateOnlyBreakers) {
        this.connectableId = Objects.requireNonNull(connectableId);

        // If selected, only non-fictitious switches can be operated
        if (!openFictitiousSwitches) {
            isTypeSwitchToOperate = isTypeSwitchToOperate.and(SwitchPredicates.IS_NONFICTIONAL);
        }

        // If selected, only breakers can be operated
        if (operateOnlyBreakers) {
            isTypeSwitchToOperate = isTypeSwitchToOperate.and(SwitchPredicates.IS_BREAKER);
        }
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        // Get the connectable
        Connectable<?> connectable = network.getConnectable(connectableId);

        // Disconnect the connectable
        boolean hasBeenConnected = connectable.connect(isTypeSwitchToOperate, reporter);

        if (hasBeenConnected) {
            LOG.info("Connectable {} has been connected.", connectableId);
        } else {
            LOG.info("Connectable {} has NOT been connected.", connectableId);
        }

    }
}
