/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TieLine;

import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesLineBoundaryNode extends Extension<TieLine> {

    String NAME = "cgmesLineBoundaryNode";

    boolean isHvdc();

    Optional<String> getLineEnergyIdentificationCodeEic();

    @Override
    default String getName() {
        return NAME;
    }
}
