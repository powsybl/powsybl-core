/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.extensions.TrippingTempoContainer;
import com.powsybl.iidm.network.extensions.TrippingTempoContainerAdder;

public class TrippingTempoContainerAdderImpl extends AbstractExtensionAdder<OverloadManagementSystem, TrippingTempoContainer> implements TrippingTempoContainerAdder {
    private int tempo;
    private String tripping;

    protected TrippingTempoContainerAdderImpl(OverloadManagementSystem extendable) {
        super(extendable);
    }

    @Override
    protected TrippingTempoContainer createExtension(OverloadManagementSystem oms) {
        return new TrippingTempoContainerImpl(oms, tripping, tempo);
    }

    @Override
    public Class<? super TrippingTempoContainer> getExtensionClass() {
        return TrippingTempoContainer.class;
    }

    @Override
    public TrippingTempoContainerAdder withTempo(String tripping, int tempo) {
        this.tripping = tripping;
        this.tempo = tempo;
        return this;
    }
}
