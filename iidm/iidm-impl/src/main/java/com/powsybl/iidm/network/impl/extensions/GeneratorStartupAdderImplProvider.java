/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorStartupAdderImplProvider
        implements ExtensionAdderProvider<Generator, GeneratorStartup, GeneratorStartupAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return GeneratorStartup.NAME;
    }

    @Override
    public Class<GeneratorStartupAdderImpl> getAdderClass() {
        return GeneratorStartupAdderImpl.class;
    }

    @Override
    public GeneratorStartupAdderImpl newAdder(Generator extendable) {
        return new GeneratorStartupAdderImpl(extendable);
    }
}
