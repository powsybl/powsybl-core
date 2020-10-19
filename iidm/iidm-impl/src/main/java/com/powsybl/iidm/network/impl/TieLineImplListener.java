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
            if (attribute.contains("half1") || attribute.contains("p1") || attribute.contains("q1")) {
                l.computeAndSetXnodeHalf1();
            } else if (attribute.contains("half2") || attribute.contains("p2") || attribute.contains("q2")) {
                l.computeAndSetXnodeHalf2();
            }
        }
        if (identifiable instanceof Bus && (attribute.contains("v") || attribute.contains("angle"))) {
            Bus b = (Bus) identifiable;
            b.getConnectedTerminalStream()
                    .filter(t -> t.getConnectable() instanceof TieLineImpl)
                    .map(t -> (TieLineImpl) t.getConnectable())
                    .forEach(tl -> {
                        tl.computeAndSetXnodeHalf1();
                        tl.computeAndSetXnodeHalf2();
                    });
        }
    }
}
