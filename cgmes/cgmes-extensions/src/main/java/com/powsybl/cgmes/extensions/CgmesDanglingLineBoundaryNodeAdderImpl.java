/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesDanglingLineBoundaryNodeAdderImpl extends AbstractExtensionAdder<DanglingLine, CgmesDanglingLineBoundaryNode> implements CgmesDanglingLineBoundaryNodeAdder {

    private boolean isHvdc;
    private String lineEnergyIdentificationCodeEic;

    CgmesDanglingLineBoundaryNodeAdderImpl(DanglingLine extendable) {
        super(extendable);
    }

    @Override
    protected CgmesDanglingLineBoundaryNode createExtension(DanglingLine extendable) {
        return new CgmesDanglingLineBoundaryNodeImpl(isHvdc, lineEnergyIdentificationCodeEic);
    }

    @Override
    public CgmesDanglingLineBoundaryNodeAdder setHvdc(boolean isHvdc) {
        this.isHvdc = isHvdc;
        return this;
    }

    @Override
    public CgmesDanglingLineBoundaryNodeAdder setLineEnergyIdentificationCodeEic(String lineEnergyIdentificationCodeEic) {
        this.lineEnergyIdentificationCodeEic = lineEnergyIdentificationCodeEic;
        return this;
    }
}
