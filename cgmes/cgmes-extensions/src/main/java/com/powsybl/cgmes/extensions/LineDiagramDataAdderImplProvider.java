/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class LineDiagramDataAdderImplProvider<I extends Identifiable<I>> implements ExtensionAdderProvider<I, LineDiagramData<I>, LineDiagramDataAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LineDiagramData.NAME;
    }

    @Override
    public Class<? super LineDiagramDataAdderImpl<I>> getAdderClass() {
        return LineDiagramDataAdder.class;
    }

    @Override
    public LineDiagramDataAdderImpl<I> newAdder(I extendable) {
        return new LineDiagramDataAdderImpl<>(extendable);
    }
}
