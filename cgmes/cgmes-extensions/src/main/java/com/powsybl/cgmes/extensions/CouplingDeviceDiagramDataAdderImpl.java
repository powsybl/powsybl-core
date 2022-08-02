/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import jdk.jshell.Diag;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CouplingDeviceDiagramDataAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, CouplingDeviceDiagramData<I>> implements CouplingDeviceDiagramDataAdder<I> {

    private final Set<CouplingDeviceDiagramData.CouplingDeviceDiagramDetails> couplingDeviceDiagramDetails = new HashSet<>();

    protected CouplingDeviceDiagramDataAdderImpl(I extendable) {
        super(extendable);
    }

    public CouplingDeviceDiagramDataAdder<I> addCouplingDeviceDiagramDetails(DiagramPoint point, double rotation) {
        couplingDeviceDiagramDetails.add(new CouplingDeviceDiagramDataImpl.CouplingDeviceDiagramDetailsImpl(point, rotation));
        return this;
    }

    @Override
    protected CouplingDeviceDiagramData<I> createExtension(I extendable) {
        return new CouplingDeviceDiagramDataImpl(extendable);
    }

}
