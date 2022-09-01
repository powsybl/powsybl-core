/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class NodeDiagramDataAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, NodeDiagramData<I>> implements NodeDiagramDataAdder<I> {

    protected NodeDiagramDataAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected NodeDiagramData<I> createExtension(I extendable) {
        return new NodeDiagramDataImpl<>(extendable);
    }
}
