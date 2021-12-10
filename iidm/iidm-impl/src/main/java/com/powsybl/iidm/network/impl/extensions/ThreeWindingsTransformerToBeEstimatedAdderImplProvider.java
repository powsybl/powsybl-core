/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class ThreeWindingsTransformerToBeEstimatedAdderImplProvider implements
        ExtensionAdderProvider<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated, ThreeWindingsTransformerToBeEstimatedAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionsName() {
        return ThreeWindingsTransformerToBeEstimated.NAME;
    }

    @Override
    public Class<? super ThreeWindingsTransformerToBeEstimatedAdderImpl> getAdderClass() {
        return ThreeWindingsTransformerToBeEstimatedAdderImpl.class;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimatedAdderImpl newAdder(ThreeWindingsTransformer extendable) {
        return new ThreeWindingsTransformerToBeEstimatedAdderImpl(extendable);
    }
}
