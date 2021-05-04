/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Line;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface CgmesLineBoundaryNodeAdder extends ExtensionAdder<Line, CgmesLineBoundaryNode> {

    CgmesLineBoundaryNodeAdder setHvdc(boolean isHvdc);

    CgmesLineBoundaryNodeAdder setLineEnergyIdentificationCodeEic(String lineEnergyIdentificationCodeEic);

    @Override
    default Class<CgmesLineBoundaryNode> getExtensionClass() {
        return CgmesLineBoundaryNode.class;
    }
}
