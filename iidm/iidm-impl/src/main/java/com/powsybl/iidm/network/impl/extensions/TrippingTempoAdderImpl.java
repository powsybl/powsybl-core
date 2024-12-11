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
import com.powsybl.iidm.network.extensions.TrippingTempo;
import com.powsybl.iidm.network.extensions.TrippingTempoAdder;

public class TrippingTempoAdderImpl extends AbstractExtensionAdder<OverloadManagementSystem.Tripping, TrippingTempo> implements TrippingTempoAdder {
    private int tempo;

    protected TrippingTempoAdderImpl(OverloadManagementSystem.Tripping extendable) {
        super(extendable);
    }

    @Override
    protected TrippingTempo createExtension(OverloadManagementSystem.Tripping tripping) {
        return new TrippingTempoImpl(tripping, tempo);
    }

    @Override
    public TrippingTempoAdder withTempo(int tempo) {
        this.tempo = tempo;
        return this;
    }

    @Override
    public Class<TrippingTempo> getExtensionClass() {
        return TrippingTempo.class;
    }
}
