/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesDanglingLineBoundaryNodeAdder extends ExtensionAdder<DanglingLine, CgmesDanglingLineBoundaryNode> {

    CgmesDanglingLineBoundaryNodeAdder setHvdc(boolean isHvdc);

    CgmesDanglingLineBoundaryNodeAdder setLineEnergyIdentificationCodeEic(String lineEnergyIdentificationCodeEic);

    @Override
    default Class<CgmesDanglingLineBoundaryNode> getExtensionClass() {
        return CgmesDanglingLineBoundaryNode.class;
    }
}
