/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TieLineImplListener extends DefaultNetworkListener {

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        Objects.requireNonNull(identifiable, "identifiable is null");
        if (identifiable instanceof TieLine) {
            final TieLineImpl l = (TieLineImpl) identifiable;
            if (attribute.contains("p")) {
                l.computeAndSetXnodeP();
            } else if (attribute.contains("q")) {
                l.computeAndSetXnodeQ();
            }
        }
        if (identifiable instanceof Bus && attribute.contains("v")) {
            Bus b = (Bus) identifiable;
            b.getConnectedTerminalStream()
                    .filter(t -> t.getConnectable() instanceof TieLineImpl)
                    .map(t -> (TieLineImpl) t.getConnectable())
                    .forEach(TieLineImpl::computeAndSetXnodeV);
        }
        if (identifiable instanceof Bus && attribute.contains("angle")) {
            Bus b = (Bus) identifiable;
            b.getConnectedTerminalStream()
                    .filter(t -> t.getConnectable() instanceof TieLineImpl)
                    .map(t -> (TieLineImpl) t.getConnectable())
                    .forEach(TieLineImpl::computeAndSetXnodeAngle);
        }
    }
}
