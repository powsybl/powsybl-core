/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.api.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.api.Load;
import com.powsybl.iidm.api.LoadType;
import com.powsybl.iidm.api.Network;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(Importer.class)
public class TestImporter implements Importer {

    private static final Network NETWORK = Mockito.mock(Network.class);
    private static Load loadUndefined;

    private static Network setNetwork() {
        if (loadUndefined == null) {
            loadUndefined = Mockito.mock(Load.class);
            Mockito.when(loadUndefined.getLoadType()).thenReturn(LoadType.UNDEFINED);
        }
        Mockito.when(NETWORK.getLoad("LOAD")).thenReturn(loadUndefined);
        return NETWORK;
    }

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public String getComment() {
        return "Dummy importer to test Importers";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource == null || dataSource.exists(null, "tst");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        return setNetwork();
    }
}
