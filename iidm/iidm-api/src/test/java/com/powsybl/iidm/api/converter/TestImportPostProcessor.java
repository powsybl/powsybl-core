/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.api.converter;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.api.Load;
import com.powsybl.iidm.api.LoadType;
import com.powsybl.iidm.api.Network;
import org.mockito.Mockito;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class TestImportPostProcessor implements ImportPostProcessor {

    private static Load loadFictitious;

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void process(Network network, ComputationManager computationManager) {
        if (loadFictitious == null) {
            loadFictitious = Mockito.mock(Load.class);
            Mockito.when(loadFictitious.getLoadType()).thenReturn(LoadType.FICTITIOUS);
        }

        Mockito.when(network.getLoad("LOAD")).thenReturn(loadFictitious);
    }
}
