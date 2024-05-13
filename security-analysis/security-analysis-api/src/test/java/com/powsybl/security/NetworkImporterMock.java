/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.VariantManager;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
@AutoService(Importer.class)
public class NetworkImporterMock implements Importer {
    @Override
    public String getFormat() {
        return "iidm";
    }

    @Override
    public String getComment() {
        return "iimd";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return true;
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory factory, Properties parameters) {
        Network network = mock(Network.class);
        when(network.getVariantManager()).thenReturn(mock(VariantManager.class));
        return network;
    }
}
