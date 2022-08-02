/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class LineDiagramDataAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, LineDiagramData<I>> implements LineDiagramDataAdder<I> {

    protected LineDiagramDataAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    public LineDiagramData<I> createExtension(I extendable) {
        return new LineDiagramDataImpl<>(extendable);
    }
}
