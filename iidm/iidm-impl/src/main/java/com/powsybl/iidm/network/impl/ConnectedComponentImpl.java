/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.components.AbstractConnectedComponent;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ConnectedComponentImpl extends AbstractConnectedComponent {

    private final Ref<NetworkImpl> networkRef;

    ConnectedComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        super(num, size);
        this.networkRef = Objects.requireNonNull(networkRef);
    }

    @Override
    protected Network getNetwork() {
        return networkRef.get();
    }
}
