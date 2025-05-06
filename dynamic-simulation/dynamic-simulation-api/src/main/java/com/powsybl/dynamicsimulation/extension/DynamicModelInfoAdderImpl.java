/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.extension;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.impl.extensions.AbstractIidmExtensionAdder;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicModelInfoAdderImpl<I extends Identifiable<I>> extends AbstractIidmExtensionAdder<I, DynamicModelInfo<I>>
        implements DynamicModelInfoAdder<I> {

    private String modelName;

    protected DynamicModelInfoAdderImpl(I identifiable) {
        super(identifiable);
    }

    @Override
    protected DynamicModelInfoImpl<I> createExtension(I extendable) {
        return new DynamicModelInfoImpl<>(extendable, modelName);
    }

    @Override
    public DynamicModelInfoAdder<I> setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }
}
