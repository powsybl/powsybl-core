/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadMockExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;

import java.io.InputStream;
import java.util.List;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadMockSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, LoadMockExt> {

    public LoadMockSerDe() {
        super("loadMock", LoadMockExt.class, "lmock",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.1", "1.2"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_1")
                        .put("1.2", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_2")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/V1_1/xsd/loadMock_V1_2.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return ImmutableList.of(getClass().getResourceAsStream("/V1_0/xsd/loadMock_V1_0.xsd"),
                getClass().getResourceAsStream("/V1_1/xsd/loadMock_V1_1.xsd"),
                getClass().getResourceAsStream("/V1_1/xsd/loadMock_V1_2.xsd"));
    }

    @Override
    public void write(LoadMockExt extension, SerializerContext context) {
        // do nothing
    }

    @Override
    public LoadMockExt read(Load extendable, DeserializerContext context) {
        checkReadingCompatibility((NetworkDeserializerContext) context);
        context.getReader().readEndNode();
        return new LoadMockExt(extendable);
    }
}
