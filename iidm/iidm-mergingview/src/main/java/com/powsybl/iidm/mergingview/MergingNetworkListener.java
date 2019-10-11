/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MergingNetworkListener implements NetworkListener {
    @Override
    public void onCreation(final Identifiable identifiable) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void onRemoval(final Identifiable identifiable) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void onUpdate(final Identifiable identifiable, final String attribute, final Object oldValue, final Object newValue) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
