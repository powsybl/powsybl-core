/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesModelExtension extends Extension<Network> {

    String NAME = "CgmesModel";

    CgmesModel getCgmesModel();

    @Override
    default String getName() {
        return NAME;
    }
}
