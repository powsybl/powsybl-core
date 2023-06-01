/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.BoundaryLine;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesBoundaryLineBoundaryNodeAdderImpl extends AbstractExtensionAdder<BoundaryLine, CgmesBoundaryLineBoundaryNode> implements CgmesBoundaryLineBoundaryNodeAdder {

    private boolean isHvdc;
    private String lineEnergyIdentificationCodeEic;

    CgmesBoundaryLineBoundaryNodeAdderImpl(BoundaryLine extendable) {
        super(extendable);
    }

    @Override
    protected CgmesBoundaryLineBoundaryNode createExtension(BoundaryLine extendable) {
        return new CgmesBoundaryLineBoundaryNodeImpl(isHvdc, lineEnergyIdentificationCodeEic);
    }

    @Override
    public CgmesBoundaryLineBoundaryNodeAdder setHvdc(boolean isHvdc) {
        this.isHvdc = isHvdc;
        return this;
    }

    @Override
    public CgmesBoundaryLineBoundaryNodeAdder setLineEnergyIdentificationCodeEic(String lineEnergyIdentificationCodeEic) {
        this.lineEnergyIdentificationCodeEic = lineEnergyIdentificationCodeEic;
        return this;
    }
}
