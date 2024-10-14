/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorTanPhi;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorTanPhiAdderImplProvider
        implements ExtensionAdderProvider<Generator, GeneratorTanPhi, GeneratorTanPhiAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return GeneratorTanPhi.NAME;
    }

    @Override
    public Class<GeneratorTanPhiAdderImpl> getAdderClass() {
        return GeneratorTanPhiAdderImpl.class;
    }

    @Override
    public GeneratorTanPhiAdderImpl newAdder(Generator extendable) {
        return new GeneratorTanPhiAdderImpl(extendable);
    }
}
