/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;

@AutoService(ExtensionAdderProvider.class)
public class ActivePowerControlAdderImplProvider<I extends Injection<I>> implements
        ExtensionAdderProvider<I, ActivePowerControl<I>, ActivePowerControlAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return ActivePowerControl.NAME;
    }

    @Override
    public Class<ActivePowerControlAdderImpl> getAdderClass() {
        return ActivePowerControlAdderImpl.class;
    }

    @Override
    public ActivePowerControlAdderImpl<I> newAdder(I extendable) {
        return new ActivePowerControlAdderImpl<>(extendable);
    }

}
