/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class InjectionDiagramDataAdderImpl<I extends Injection<I>> extends AbstractExtensionAdder<I, InjectionDiagramData<I>> implements InjectionDiagramDataAdder<I> {

    private final Set<InjectionDiagramData.InjectionDiagramDetails> diagramsDetails = new HashSet<>();


    protected InjectionDiagramDataAdderImpl(I extendable) {
        super(extendable);
    }

    //TODO : what to do about terminalPoints
    @Override
    public InjectionDiagramDataAdder<I> addDiagramDetails(DiagramPoint point, double rotation) {
        diagramsDetails.add(new InjectionDiagramDataImpl.InjectionDiagramDetailsImpl(point, rotation));
        return this;
    }

    @Override
    protected InjectionDiagramData<I> createExtension(I extendable) {
        return new InjectionDiagramDataImpl<>(extendable);
    }
}
