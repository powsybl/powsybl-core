/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DanglingLineImplListener extends DefaultNetworkListener {

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        Objects.requireNonNull(identifiable, "identifiable is null");
        if (identifiable instanceof DanglingLineImpl) {
            final BoundaryPointImpl boundaryPoint = (BoundaryPointImpl) ((DanglingLine) identifiable).getBoundaryPoint();
            boundaryPoint.computeAndSetBoundaryPoint();
        }
        if (identifiable instanceof Bus && (attribute.contains("v") || attribute.contains("angle"))) {
            Bus b = (Bus) identifiable;
            b.getConnectedTerminalStream()
                    .filter(t -> t.getConnectable() instanceof DanglingLineImpl)
                    .map(t -> (DanglingLine) t.getConnectable())
                    .map(dl -> (BoundaryPointImpl) dl.getBoundaryPoint())
                    .forEach(BoundaryPointImpl::computeAndSetBoundaryPoint);
        }
    }
}
