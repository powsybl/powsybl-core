/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadQuxExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;

import java.io.InputStream;
import java.util.List;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadQuxSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, LoadQuxExt> {

    public LoadQuxSerDe() {
        super("loadQux", LoadQuxExt.class, "lq",
                ImmutableMap.<IidmVersion, List<String>>builder()
                        .put(IidmVersion.V_1_0, List.of("1.0"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.powsybl.org/schema/iidm/ext/load_qux/1_0")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/V1_0/xsd/loadQux.xsd");
    }

    @Override
    public void write(LoadQuxExt extension, SerializerContext context) {
        // do nothing
    }

    @Override
    public LoadQuxExt read(Load extendable, DeserializerContext context) {
        checkReadingCompatibility((NetworkDeserializerContext) context);
        return new LoadQuxExt(extendable);
    }
}
