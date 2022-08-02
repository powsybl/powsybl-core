/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CouplingDeviceDiagramDataAdderImplProvider<I extends Identifiable<I>> implements ExtensionAdderProvider<I, CouplingDeviceDiagramData<I>, CouplingDeviceDiagramDataAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CouplingDeviceDiagramData.NAME;
    }

    @Override
    public Class<? super CouplingDeviceDiagramDataAdderImpl<I>> getAdderClass() {
        return CouplingDeviceDiagramDataAdder.class;
    }

    @Override
    public CouplingDeviceDiagramDataAdderImpl<I> newAdder(I extendable) {
        return new CouplingDeviceDiagramDataAdderImpl<>(extendable);
    }
}
