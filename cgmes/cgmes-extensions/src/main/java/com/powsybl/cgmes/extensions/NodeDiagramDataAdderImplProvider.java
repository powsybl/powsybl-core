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
public class NodeDiagramDataAdderImplProvider<I extends Identifiable<I>> implements ExtensionAdderProvider<I, NodeDiagramData<I>, NodeDiagramDataAdderImpl<I>> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return NodeDiagramData.NAME;
    }

    @Override
    public Class<? super NodeDiagramDataAdderImpl<I>> getAdderClass() {
        return NodeDiagramDataAdder.class;
    }

    @Override
    public NodeDiagramDataAdderImpl<I> newAdder(I extendable) {
        return new NodeDiagramDataAdderImpl<>(extendable);
    }
}
