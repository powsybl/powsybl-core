/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadQuxExt;
import com.powsybl.iidm.xml.extensions.AbstractVersionableNetworkExtensionXmlSerializer;

import java.io.InputStream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadQuxXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<Load, LoadQuxExt> {

    public LoadQuxXmlSerializer() {
        super("loadQux", LoadQuxExt.class, false, "lq",
                ImmutableMap.<IidmXmlVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmXmlVersion.V_1_0, ImmutableSortedSet.of("1.0"))
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
    public void write(LoadQuxExt extension, XmlWriterContext context) {
        // do nothing
    }

    @Override
    public LoadQuxExt read(Load extendable, XmlReaderContext context) {
        checkReadingCompatibility((NetworkXmlReaderContext) context);
        return new LoadQuxExt(extendable);
    }
}
