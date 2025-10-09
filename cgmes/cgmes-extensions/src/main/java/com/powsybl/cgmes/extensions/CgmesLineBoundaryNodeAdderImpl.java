/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesLineBoundaryNodeAdderImpl extends AbstractExtensionAdder<TieLine, CgmesLineBoundaryNode> implements CgmesLineBoundaryNodeAdder {

    private boolean isHvdc;
    private String lineEnergyIdentificationCodeEic;

    CgmesLineBoundaryNodeAdderImpl(TieLine extendable) {
        super(extendable);
    }

    @Override
    protected CgmesLineBoundaryNode createExtension(TieLine extendable) {
        return new CgmesLineBoundaryNodeImpl(isHvdc, lineEnergyIdentificationCodeEic);
    }

    @Override
    public CgmesLineBoundaryNodeAdder setHvdc(boolean isHvdc) {
        this.isHvdc = isHvdc;
        return this;
    }

    @Override
    public CgmesLineBoundaryNodeAdder setLineEnergyIdentificationCodeEic(String lineEnergyIdentificationCodeEic) {
        this.lineEnergyIdentificationCodeEic = lineEnergyIdentificationCodeEic;
        return this;
    }
}
