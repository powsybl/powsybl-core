/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class DefaultMergingViewListener implements NetworkListener {

    protected final MergingViewIndex index;

    DefaultMergingViewListener(final MergingViewIndex index) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        // Nothing to do
    }

    @Override
    public void onCreation(final Identifiable identifiable) {
        // Nothing to do
    }

    @Override
    public void beforeRemoval(final Identifiable identifiable) {
        // Nothing to do
    }

    @Override
    public void afterRemoval(String id) {
        // Nothing to do
    }
}
