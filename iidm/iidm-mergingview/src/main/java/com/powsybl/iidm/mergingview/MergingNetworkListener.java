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
        index.remove(identifiable);
    }

    @Override
    public void onUpdate(final Identifiable identifiable, final String attribute, final Object oldValue, final Object newValue) {
        if (Objects.nonNull(oldValue) && Objects.isNull(newValue)) {
            if (oldValue instanceof PhaseTapChanger) {
                index.remove((PhaseTapChanger) oldValue);
            } else if (oldValue instanceof RatioTapChanger) {
                index.remove((RatioTapChanger) oldValue);
            }
        }
    }
}
