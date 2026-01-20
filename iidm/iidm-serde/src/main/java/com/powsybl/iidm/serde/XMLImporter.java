/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Importer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static com.powsybl.commons.xml.XmlUtil.getXMLInputFactory;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Importer.class)
public class XMLImporter extends AbstractTreeDataImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);
    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml"};

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
    public String getComment() {
        return "IIDM XML v " + CURRENT_IIDM_VERSION.toString(".") + " importer";
    }

    protected boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext == null) {
            return false;
        }

        try (InputStream is = dataSource.newInputStream(null, ext)) {
            XMLStreamReader xmlsr = getXMLInputFactory().createXMLStreamReader(is);
            try {
                return isValidNetworkRoot(xmlsr);
            } finally {
                cleanClose(xmlsr);
            }
        } catch (XMLStreamException e) {
            return false; // not a valid XML file
        }
    }

    private boolean isValidNetworkRoot(XMLStreamReader xmlsr) throws XMLStreamException {
        while (xmlsr.hasNext()) {
            if (xmlsr.next() == XMLStreamConstants.START_ELEMENT) {
                String name = xmlsr.getLocalName();
                String ns = xmlsr.getNamespaceURI();

                if (!NetworkSerDe.NETWORK_ROOT_ELEMENT_NAME.equals(name) || ns.isEmpty()) {
                    return false;
                }

                String currentPrefix = extractPrefix(ns);
                if (isValidPrefix(currentPrefix)) {
                    String version = extractVersion(ns);
                    if (!version.isEmpty()) {
                        // If it is not supported, this will throw an exception giving details on the encountered version
                        // and the maximum supported version.
                        IidmVersion.of(version, "_");
                        return isValidNamespace(ns);
                    }
                }
            }
        }
        return false;
    }

    private String extractPrefix(String namespace) {
        int lastSlash = namespace.lastIndexOf('/');
        return (lastSlash > 0) ? namespace.substring(0, lastSlash) : namespace;
    }

    private String extractVersion(String namespace) {
        int lastSlash = namespace.lastIndexOf('/');
        return (lastSlash >= 0 && lastSlash < namespace.length() - 1) ? namespace.substring(lastSlash + 1) : "";
    }

    private boolean isValidPrefix(String prefix) {
        return Stream.of(IidmVersion.values())
                .map(v -> extractPrefix(v.getNamespaceURI(true)))
                .anyMatch(prefix::equals)
                || Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(IidmVersion.V_1_7) >= 0)
                .map(v -> extractPrefix(v.getNamespaceURI(false)))
                .anyMatch(prefix::equals);
    }

    private boolean isValidNamespace(String ns) {
        return Stream.of(IidmVersion.values())
                .anyMatch(v -> v.getNamespaceURI().equals(ns))
                || Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(IidmVersion.V_1_7) >= 0)
                .anyMatch(v -> v.getNamespaceURI(false).equals(ns));
    }

    private void cleanClose(XMLStreamReader xmlStreamReader) {
        try {
            xmlStreamReader.close();
            XmlUtil.gcXmlInputFactory(getXMLInputFactory());
        } catch (XMLStreamException e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
