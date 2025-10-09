/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Substation;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class EntsoeAreaAdderImplProvider implements
        ExtensionAdderProvider<Substation, EntsoeArea, EntsoeAreaAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return EntsoeArea.NAME;
    }

    @Override
    public Class<EntsoeAreaAdderImpl> getAdderClass() {
        return EntsoeAreaAdderImpl.class;
    }

    @Override
    public EntsoeAreaAdderImpl newAdder(Substation extendable) {
        return new EntsoeAreaAdderImpl(extendable);
    }
}
