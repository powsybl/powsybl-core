/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.IidmXmlVersion;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadMockExt;

import java.io.InputStream;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadMockXmlSerializer implements ExtensionXmlSerializer<Load, LoadMockExt> {

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return ImmutableList.of(getClass().getResourceAsStream("/V1_0/xsd/loadMock.xsd"),
                getClass().getResourceAsStream("/V1_1/xsd/loadMock.xsd"));
    }

    @Override
    public String getNamespaceUri(IidmXmlVersion version) {
        switch (version) {
            case V_1_0:
                return "http://wwww.itesla_project.eu/schema/iidm/ext/loadMock/1_0";
            case V_1_1:
            default:
                return "http://www.powsybl.org/schema/iidm/ext/loadMock/1_1";
        }
    }

    @Override
    public String getNamespacePrefix() {
        return "lmock";
    }

    @Override
    public void write(LoadMockExt extension, XmlWriterContext context) {
        // do nothing
    }

    @Override
    public LoadMockExt read(Load extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        IidmXmlVersion version = networkContext.getVersion();
        if (!networkContext.containsExtensionNamespaceUri(getNamespaceUri(version))) {
            throw new PowsyblException("IIDM-XML version of network (" + version.toString(".")
                    + ") is not compatible with the IIDM-XML version of LoadMock extension's namespace URI. "
                    + "LoadMock extension's namespace URI must be '" + getNamespaceUri(version) + "'");
        }
        return new LoadMockExt(extendable);
    }

    @Override
    public String getExtensionName() {
        return "loadMock";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super LoadMockExt> getExtensionClass() {
        return LoadMockExt.class;
    }
}
