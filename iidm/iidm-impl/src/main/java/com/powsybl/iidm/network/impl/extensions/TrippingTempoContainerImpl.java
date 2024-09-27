/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.extensions.TrippingTempoAdder;
import com.powsybl.iidm.network.extensions.TrippingTempoContainer;

public class TrippingTempoContainerImpl extends AbstractExtension<OverloadManagementSystem> implements TrippingTempoContainer {
    private OverloadManagementSystem.Tripping tripping;

    public TrippingTempoContainerImpl(OverloadManagementSystem oms, String trippingKey, int tempo) {
        super(oms);
        oms.getTripping(trippingKey)
                .ifPresent(tripping -> {
                    this.tripping = tripping;
                    tripping.newExtension(TrippingTempoAdder.class).withTempo(tempo).add();
                });
    }

    @Override
    public OverloadManagementSystem.Tripping getTripping() {
        return tripping;
    }
}
