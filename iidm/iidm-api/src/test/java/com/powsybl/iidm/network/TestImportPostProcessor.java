/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.computation.ComputationManager;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class TestImportPostProcessor implements ImportPostProcessor {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void process(Network network, ComputationManager computationManager) {
        network.getLoad("LOAD").setLoadType(LoadType.FICTITIOUS);
    }
}
