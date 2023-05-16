/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TieLine;

import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesLineBoundaryNodeImpl extends AbstractExtension<TieLine> implements CgmesLineBoundaryNode {

    private final boolean isHvdc;
    private final String lineEnergyIdentificationCodeEic;

    CgmesLineBoundaryNodeImpl(boolean isHvdc, String lineEnergyIdentificationCodeEic) {
        this.isHvdc = isHvdc;
        this.lineEnergyIdentificationCodeEic = lineEnergyIdentificationCodeEic;
    }

    @Override
    public boolean isHvdc() {
        return isHvdc;
    }

    @Override
    public Optional<String> getLineEnergyIdentificationCodeEic() {
        return Optional.ofNullable(lineEnergyIdentificationCodeEic);
    }
}
