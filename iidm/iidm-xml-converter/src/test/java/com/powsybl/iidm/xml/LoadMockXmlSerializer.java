/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadMockExt;
import com.powsybl.iidm.xml.extensions.AbstractVersionableNetworkExtensionXmlSerializer;

import java.io.InputStream;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadMockXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<Load, LoadMockExt> {

    public LoadMockXmlSerializer() {
        super("loadMock", LoadMockExt.class, false, "lmock",
                ImmutableMap.<IidmXmlVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmXmlVersion.V_1_0, ImmutableSortedSet.of("1.0"))
                        .put(IidmXmlVersion.V_1_1, ImmutableSortedSet.of("1.1", "1.2"))
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
    public String getNamespaceUri(String version) {
        switch (version) {
            case "1.0":
                return "http://wwww.itesla_project.eu/schema/iidm/ext/loadMock/1_0";
            case "1.1":
                return "http://www.powsybl.org/schema/iidm/ext/loadMock/1_1";
            case "1.2":
                return "http://www.powsybl.org/schema/iidm/ext/loadMock/1_2";
            default:
                throw new PowsyblException("Version " + version + " of LoadMock XML serializer does not exist");
        }
    }

    @Override
    public void write(LoadMockExt extension, XmlWriterContext context) {
        // do nothing
    }

    @Override
    public LoadMockExt read(Load extendable, XmlReaderContext context) {
        checkReadingCompatibility((NetworkXmlReaderContext) context);
        return new LoadMockExt(extendable);
    }
}
