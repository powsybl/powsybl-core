/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.components.AbstractSynchronousComponent;
import com.powsybl.commons.ref.Ref;

import java.util.Objects;

/**
 * @author Thomas ADAM {@literal <tadam at silicom.fr>}
 */
class SynchronousComponentImpl extends AbstractSynchronousComponent {

    private final Ref<NetworkImpl> networkRef;

    SynchronousComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        super(num, size);
        this.networkRef = Objects.requireNonNull(networkRef);
    }

    @Override
    protected Network getNetwork() {
        return networkRef.get();
    }
}
