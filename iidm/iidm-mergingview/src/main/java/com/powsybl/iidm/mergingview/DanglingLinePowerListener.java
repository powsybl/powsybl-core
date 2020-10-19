/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLinePowerListener extends DefaultMergingViewListener {

    DanglingLinePowerListener(final MergingViewIndex index) {
        super(index);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        Objects.requireNonNull(identifiable, "identifiable is null");
        if (identifiable instanceof DanglingLine) {
            final DanglingLine dl = (DanglingLine) identifiable;
            final String ucteCode = dl.getUcteXnodeCode();
            final MergedLine mergedLine = index.getMergedLineByCode(ucteCode);
            if (mergedLine != null) {
                mergedLine.computeAndSetXnodeHalf1();
                mergedLine.computeAndSetXnodeHalf2();
            }
        }
        if (identifiable instanceof Bus && (attribute.contains("v") || attribute.contains("angle"))) {
            Bus b = (Bus) identifiable;
            b.getConnectedTerminalStream()
                    .filter(t -> t.getConnectable() instanceof DanglingLine)
                    .map(t -> (DanglingLine) t.getConnectable())
                    .map(DanglingLine::getUcteXnodeCode)
                    .map(index::getMergedLineByCode)
                    .filter(Objects::nonNull)
                    .forEach(ml -> {
                        ml.computeAndSetXnodeHalf1();
                        ml.computeAndSetXnodeHalf2();
                    });
        }
    }
}
