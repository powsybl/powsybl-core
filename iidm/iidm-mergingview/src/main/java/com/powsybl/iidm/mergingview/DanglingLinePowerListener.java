/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLinePowerListener extends AbstractListener {

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
                if (attribute.contains("p")) {
                    mergedLine.computeAndSetP0();
                } else if (attribute.contains("q")) {
                    mergedLine.computeAndSetQ0();
                }
            }
        }
    }
}
