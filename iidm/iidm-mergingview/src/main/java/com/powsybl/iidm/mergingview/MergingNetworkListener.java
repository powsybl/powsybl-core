/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MergingNetworkListener implements NetworkListener {

    private final MergingViewIndex index;

    MergingNetworkListener(final MergingViewIndex index) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public void onCreation(final Identifiable identifiable) {
        if (identifiable instanceof DanglingLine) {
            // Check DanglingLine creation from Network merged into MergingView
            // in order to create a new MergedLine if it's needed
            index.checkNewDanglingLine((DanglingLine) identifiable);
        }
    }

    @Override
    public void onRemoval(final Identifiable identifiable) {
        // Not implemented yet !
    }

    @Override
    public void onUpdate(final Identifiable identifiable, final String attribute, final Object oldValue, final Object newValue) {
        // Not implemented yet !
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        Objects.requireNonNull(identifiable, "identifiable is null");
        if (identifiable instanceof DanglingLine) {
            final DanglingLine dl = ((DanglingLine) identifiable);
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
