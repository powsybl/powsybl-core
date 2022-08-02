/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;


import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Injection;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class InjectionDiagramDataAdderImplProvider<I extends Injection<I>> implements ExtensionAdderProvider<I, InjectionDiagramData<I>, InjectionDiagramDataAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return InjectionDiagramData.NAME;
    }

    @Override
    public Class<? super InjectionDiagramDataAdderImpl<I>> getAdderClass() {
        return InjectionDiagramDataAdder.class;
    }

    @Override
    public InjectionDiagramDataAdderImpl<I> newAdder(I extendable) {
        return new InjectionDiagramDataAdderImpl<>(extendable);
    }
}
