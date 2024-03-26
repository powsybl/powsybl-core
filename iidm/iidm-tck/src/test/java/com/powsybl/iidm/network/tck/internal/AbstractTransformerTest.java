/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.internal;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractTransformerTest {

    protected Network network;
    protected Substation substation;

    @BeforeEach
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        substation = network.getSubstation("sub");
    }

}
