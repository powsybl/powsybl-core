/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.extensions.TrippingTempo;

@AutoService(ExtensionAdderProvider.class)
public class TrippingTempoAdderImplProvider implements
        ExtensionAdderProvider<OverloadManagementSystem.Tripping, TrippingTempo, TrippingTempoAdderImpl> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<TrippingTempoAdderImpl> getAdderClass() {
        return TrippingTempoAdderImpl.class;
    }

    @Override
    public TrippingTempoAdderImpl newAdder(OverloadManagementSystem.Tripping tripping) {
        return new TrippingTempoAdderImpl(tripping);
    }
}
