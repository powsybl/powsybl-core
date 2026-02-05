/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import org.mockito.Mockito;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkFactoryMock implements NetworkFactory {

    private int createNetworkCount = 0;

    public int getCreateNetworkCount() {
        return createNetworkCount;
    }

    public void resetCreateNetworkCount() {
        createNetworkCount = 0;
    }

    @Override
    public Network createNetwork(String id, String sourceFormat) {
        createNetworkCount++;
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

        // When called to be updated, rely on default method, that will call importer
        Mockito.doCallRealMethod().when(network).update(
                Mockito.any(ReadOnlyDataSource.class),
                Mockito.any(ComputationManager.class),
                Mockito.any(ImportConfig.class),
                Mockito.nullable(Properties.class),
                Mockito.any(ImportersLoader.class),
                Mockito.any(ReportNode.class));
        Mockito.doCallRealMethod().when(network).update(
                Mockito.any(ReadOnlyDataSource.class),
                Mockito.nullable(Properties.class),
                Mockito.any(ReportNode.class));
        Mockito.doCallRealMethod().when(network).update(
                Mockito.any(ReadOnlyDataSource.class),
                Mockito.nullable(Properties.class));
        Mockito.doCallRealMethod().when(network).update(
                Mockito.any(ReadOnlyDataSource.class));

        // Allow setting a value for P0
        double[] loadP = new double[1];
        loadP[0] = Double.NaN;
        Mockito.doAnswer(invocationOnMock -> {
            loadP[0] = (double) invocationOnMock.getArguments()[0];
            return load;
        }).when(load).setP0(Mockito.anyDouble());
        Mockito.when(load.getP0())
                .thenAnswer(invocationOnMock -> loadP[0]);
        Properties properties = new Properties();
        Mockito.doAnswer(invocationOnMock -> {
            properties.setProperty((String) invocationOnMock.getArguments()[0], (String) invocationOnMock.getArguments()[1]);
            return null;
        }).when(network).setProperty(Mockito.anyString(), Mockito.anyString());
        Mockito.when(network.getProperty(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> properties.getProperty((String) invocationOnMock.getArguments()[0]));
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
