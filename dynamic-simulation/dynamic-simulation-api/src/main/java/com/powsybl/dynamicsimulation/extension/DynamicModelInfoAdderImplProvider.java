/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.extension;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class DynamicModelInfoAdderImplProvider<I extends Identifiable<I>> implements
        ExtensionAdderProvider<I, DynamicModelInfo<I>, DynamicModelInfoAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return DynamicModelInfo.NAME;
    }

    @Override
    public Class<DynamicModelInfoAdderImpl> getAdderClass() {
        return DynamicModelInfoAdderImpl.class;
    }

    @Override
    public DynamicModelInfoAdderImpl<I> newAdder(I extendable) {
        return new DynamicModelInfoAdderImpl<>(extendable);
    }
}
