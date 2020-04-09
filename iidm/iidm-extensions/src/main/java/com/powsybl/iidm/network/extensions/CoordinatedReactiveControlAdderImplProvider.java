/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;

/**
 * @author Thomas ADAM <tadam at silicom.fr>
 */
@AutoService(ExtensionAdderProvider.class)
public class CoordinatedReactiveControlAdderImplProvider implements
        ExtensionAdderProvider<Generator, CoordinatedReactiveControl, CoordinatedReactiveControlAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<CoordinatedReactiveControlAdderImpl> getAdderClass() {
        return CoordinatedReactiveControlAdderImpl.class;
    }

    @Override
    public CoordinatedReactiveControlAdderImpl newAdder(Generator extendable) {
        return new CoordinatedReactiveControlAdderImpl(extendable);
    }

}
