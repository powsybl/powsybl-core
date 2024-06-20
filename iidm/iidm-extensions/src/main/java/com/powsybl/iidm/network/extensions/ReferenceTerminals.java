/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ReferenceTerminals iIDM extension allows storing the angle references that were chosen by a load flow, for
 * example (but not necessarily) chosen using {@link ReferencePriorities} inputs.<br/>
 * There should be one reference terminal per SynchronousComponent calculated.
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminals extends Extension<Network> {

    String NAME = "referenceTerminals";

    @Override
    default String getName() {
        return NAME;
    }

    Set<Terminal> getReferenceTerminals();

    void setReferenceTerminals(Set<Terminal> terminals);

    ReferenceTerminals reset();

    ReferenceTerminals addReferenceTerminal(Terminal terminal);

    /**
     * Deletes all defined reference terminals in the network and all its subnetworks for the current variant
     * @param network network whose reference terminals should be deleted
     */
    static void reset(Network network) {
        Objects.requireNonNull(network);
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        if (ext == null) {
            ext = network.newExtension(ReferenceTerminalsAdder.class)
                    .withTerminals(Set.of())
                    .add();
        }
        ext.reset();
        // reset also all subnetwork
        network.getSubnetworks().forEach(ReferenceTerminals::reset);
    }

    /**
     * Defines/add a terminal as reference in the network for the current variant.
     * In case of a merged network with subnetwork, the extension is placed on the root/merged network.
     * @param terminal terminal to be added as reference terminal
     */
    static void addTerminal(Terminal terminal) {
        Objects.requireNonNull(terminal);
        Network network = terminal.getVoltageLevel().getNetwork();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        if (ext == null) {
            ext = network.newExtension(ReferenceTerminalsAdder.class)
                    .withTerminals(Set.of())
                    .add();
        }
        ext.addReferenceTerminal(terminal);
    }

    /**
     * Gets the reference terminals in the network for the current variant.
     * In case of a merged network with subnetworks, reference terminals from the merged network
     * and reference terminals from subnetworks are returned altogether.
     * @param network network to get reference terminals from
     */
    static Set<Terminal> getTerminals(Network network) {
        Objects.requireNonNull(network);
        Set<Terminal> terminals = new HashSet<>();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        if (ext != null) {
            terminals.addAll(ext.getReferenceTerminals());
        }
        // also from subnetworks
        network.getSubnetworks().forEach(subNetwork -> {
            ReferenceTerminals subNetworkExt = subNetwork.getExtension(ReferenceTerminals.class);
            if (subNetworkExt != null) {
                terminals.addAll(subNetworkExt.getReferenceTerminals());
            }
        });
        return ImmutableSet.copyOf(terminals);
    }
}
