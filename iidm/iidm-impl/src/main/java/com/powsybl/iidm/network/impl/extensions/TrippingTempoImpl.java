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
import com.powsybl.iidm.network.extensions.TrippingTempo;

public class TrippingTempoImpl extends
        AbstractExtension<OverloadManagementSystem.Tripping> implements TrippingTempo {
    private final int tempo;

    public TrippingTempoImpl(OverloadManagementSystem.Tripping tripping, int tempo) {
        super(tripping);
        this.tempo = tempo;
    }

    @Override
    public int getTempo() {
        return tempo;
    }
}
