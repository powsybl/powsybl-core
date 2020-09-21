/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesBoundarySideImpl extends AbstractExtension<DanglingLine> implements CgmesBoundarySide {

    private final int boundarySide;

    CgmesBoundarySideImpl(int boundarySide) {
        if (boundarySide != 1 && boundarySide != 2) {
            throw new PowsyblException("Incorrect boundary side (" + boundarySide + ")");
        }
        this.boundarySide = boundarySide;
    }

    @Override
    public int getBoundarySide() {
        return boundarySide;
    }
}
