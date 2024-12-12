/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import org.mockito.Mockito;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkFactoryMock implements NetworkFactory {

    @Override
    public Network createNetwork(String id, String sourceFormat) {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getCaseDate())
                .thenReturn(ZonedDateTime.of(2021, 12, 20, 0, 0, 0, 0, ZoneOffset.UTC));
        Load load = Mockito.mock(Load.class);
        Mockito.when(network.getLoad(Mockito.anyString()))
                .thenReturn(load);
        LoadType[] loadType = new LoadType[1];
        loadType[0] = LoadType.UNDEFINED;
        Mockito.doAnswer(invocationOnMock -> {
            loadType[0] = (LoadType) invocationOnMock.getArguments()[0];
            return load;
        }).when(load).setLoadType(Mockito.any(LoadType.class));
        Mockito.when(load.getLoadType())
                .thenAnswer(invocationOnMock -> loadType[0]);

        // The load P0 in the mocked network returns a specific value after update
        double[] loadP = new double[1];
        loadP[0] = Double.NaN;
        Mockito.doAnswer(invocationOnMock -> {
            loadP[0] = 123.0;
            return null;
        }).when(network).update(Mockito.any(ReadOnlyDataSource.class));
        Mockito.when(load.getP0())
                .thenAnswer(invocationOnMock -> loadP[0]);

        return network;
    }

    @Override
    public Network merge(String id, Network... networks) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Network merge(Network... networks) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
