/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.List;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesSvMetadata extends Extension<Network> {

    String NAME = "cgmesSvMetadata";

    String getDescription();

    int getSvVersion();

    List<String> getDependencies();

    String getModelingAuthoritySet();

    @Override
    default String getName() {
        return NAME;
    }
}
