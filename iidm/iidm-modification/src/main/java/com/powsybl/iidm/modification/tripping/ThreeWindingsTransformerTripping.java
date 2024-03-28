/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;
import java.util.Set;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public class ThreeWindingsTransformerTripping extends AbstractTripping {

    public ThreeWindingsTransformerTripping(String id) {
        super(id);
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer(id);
        if (twt3 == null) {
            throw createNotFoundException();
        }
        TrippingTopologyTraverser.traverse(twt3.getLeg1().getTerminal(), switchesToOpen, terminalsToDisconnect, traversedTerminals);
        TrippingTopologyTraverser.traverse(twt3.getLeg2().getTerminal(), switchesToOpen, terminalsToDisconnect, traversedTerminals);
        TrippingTopologyTraverser.traverse(twt3.getLeg3().getTerminal(), switchesToOpen, terminalsToDisconnect, traversedTerminals);
    }

    protected PowsyblException createNotFoundException() {
        return new PowsyblException("ThreeWindingsTransformer '" + id + "' not found");
    }
}
