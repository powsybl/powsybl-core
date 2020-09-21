/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesBoundarySideAdderImplProvider implements ExtensionAdderProvider<DanglingLine, CgmesBoundarySide, CgmesBoundarySideAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<? super CgmesBoundarySideAdderImpl> getAdderClass() {
        return CgmesBoundarySideAdderImpl.class;
    }

    @Override
    public CgmesBoundarySideAdderImpl newAdder(DanglingLine extendable) {
        return new CgmesBoundarySideAdderImpl(extendable);
    }
}
