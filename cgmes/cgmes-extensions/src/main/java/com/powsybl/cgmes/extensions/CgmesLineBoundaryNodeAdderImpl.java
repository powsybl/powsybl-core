/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesLineBoundaryNodeAdderImpl extends AbstractExtensionAdder<Line, CgmesLineBoundaryNode> implements CgmesLineBoundaryNodeAdder {

    private boolean isHvdc;
    private String lineEnergyIdentificationCodeEic;

    CgmesLineBoundaryNodeAdderImpl(Line extendable) {
        super(extendable);
    }

    @Override
    protected CgmesLineBoundaryNode createExtension(Line extendable) {
        if (!extendable.isTieLine()) {
            throw new PowsyblException(String.format("Unexpected extendable: line %s is not a tie line", extendable.getId()));
        }
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
