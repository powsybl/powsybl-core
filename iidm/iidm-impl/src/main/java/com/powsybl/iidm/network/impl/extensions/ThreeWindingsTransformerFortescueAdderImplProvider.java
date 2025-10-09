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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class ThreeWindingsTransformerFortescueAdderImplProvider
        implements ExtensionAdderProvider<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue, ThreeWindingsTransformerFortescueAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return ThreeWindingsTransformerFortescue.NAME;
    }

    @Override
    public Class<ThreeWindingsTransformerFortescueAdder> getAdderClass() {
        return ThreeWindingsTransformerFortescueAdder.class;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdder newAdder(ThreeWindingsTransformer extendable) {
        return new ThreeWindingsTransformerFortescueAdderImpl(extendable);
    }
}
