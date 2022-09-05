/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import org.mockito.Mock;
import org.mockito.Mockito;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkFactoryMock implements NetworkFactory {

    @Override
    public Network createNetwork(String id, String sourceFormat) {
        Network network = Mockito.mock(Network.class);
        Load load = Mockito.mock(Load.class);
        Mockito.when(network.getLoad(Mockito.anyString()))
                .thenReturn(load);
        return network;
    }
}
