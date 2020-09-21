/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesBoundarySideAdderImpl extends AbstractExtensionAdder<DanglingLine, CgmesBoundarySide> implements CgmesBoundarySideAdder {

    private int boundarySide = -1;

    CgmesBoundarySideAdderImpl(DanglingLine extendable) {
        super(extendable);
    }

    @Override
    public CgmesBoundarySideAdder setBoundarySide(int boundarySide) {
        this.boundarySide = boundarySide;
        return this;
    }

    @Override
    protected CgmesBoundarySide createExtension(DanglingLine extendable) {
        if (boundarySide != 1 && boundarySide != 2) {
            throw new PowsyblException("Incorrect boundary side (" + boundarySide + ") for dangling line " + extendable.getId());
        }
        return new CgmesBoundarySideImpl(boundarySide);
    }
}
