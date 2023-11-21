/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadQuxExt;
import com.powsybl.iidm.serializer.extensions.AbstractVersionableNetworkExtensionSerializer;

import java.io.InputStream;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class LoadQuxSerializer extends AbstractVersionableNetworkExtensionSerializer<Load, LoadQuxExt> {

    public LoadQuxSerializer() {
        super("loadQux", LoadQuxExt.class, "lq",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0"))
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
    public void write(LoadQuxExt extension, WriterContext context) {
        // do nothing
    }

    @Override
    public LoadQuxExt read(Load extendable, ReaderContext context) {
        checkReadingCompatibility((NetworkSerializerReaderContext) context);
        return new LoadQuxExt(extendable);
    }
}
