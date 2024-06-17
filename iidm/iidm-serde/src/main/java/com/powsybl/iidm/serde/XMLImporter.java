/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Importer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Importer.class)
public class XMLImporter extends AbstractTreeDataImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);
    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml", "iidm.xml"};

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    public XMLImporter() {
        super();
    }

    public XMLImporter(PlatformConfig platformConfig) {
        super(platformConfig);
    }

    @Override
    protected String[] getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    @Override
    public String getComment() {
        return "IIDM XML v " + CURRENT_IIDM_VERSION.toString(".") + " importer";
    }

    protected boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        try {
            if (ext != null) {
                try (InputStream is = dataSource.newInputStream(null, ext)) {
                    // check the first root element is network and namespace is IIDM
                    XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
                    try {
                        while (xmlsr.hasNext()) {
                            int eventType = xmlsr.next();
                            if (eventType == XMLStreamConstants.START_ELEMENT) {
                                String name = xmlsr.getLocalName();
                                String ns = xmlsr.getNamespaceURI();
                                return NetworkSerDe.NETWORK_ROOT_ELEMENT_NAME.equals(name)
                                        && (Stream.of(IidmVersion.values()).anyMatch(v -> v.getNamespaceURI().equals(ns))
                                        || Stream.of(IidmVersion.values()).filter(v -> v.compareTo(IidmVersion.V_1_7) >= 0).anyMatch(v -> v.getNamespaceURI(false).equals(ns)));
                            }
                        }
                    } finally {
                        cleanClose(xmlsr);
                    }
                }
            }
            return false;
        } catch (XMLStreamException e) {
            // not a valid xml file
            return false;
        }
    }

    private void cleanClose(XMLStreamReader xmlStreamReader) {
        try {
            xmlStreamReader.close();
            XmlUtil.gcXmlInputFactory(XML_INPUT_FACTORY_SUPPLIER.get());
        } catch (XMLStreamException e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
