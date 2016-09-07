/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.parameters.Parameter;
import eu.itesla_project.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class XMLImporter implements Importer, XmlConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);

    private final String[] EXTENSIONS = { "xml", "iidm" };

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND
            = new Parameter("throwExceptionIfExtensionNotFound", ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE);

    @Override
    public String getFormat() {
        return "XML";
    }

    @Override
    public InputStream get16x16Icon() {
        return XMLImporter.class.getResourceAsStream("/icons/itesla16x16.png");
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND);
    }

    @Override
    public String getComment() {
        return "IIDM XML v " + VERSION + " importer";
    }

    private String findExtension(ReadOnlyDataSource dataSource) throws IOException {
        for (String ext : EXTENSIONS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            if (ext != null) {
                try (InputStream is = dataSource.newInputStream(null, ext)) {
                    // check the first root element is network and namespace is IIDM
                    XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
                    try {
                        while (xmlsr.hasNext()) {
                            int eventType = xmlsr.next();
                            if (eventType == XMLEvent.START_ELEMENT) {
                                String name = xmlsr.getLocalName();
                                String ns = xmlsr.getNamespaceURI();
                                return NetworkXml.NETWORK_ROOT_ELEMENT_NAME.equals(name) && IIDM_URI.equals(ns);
                            }
                        }
                    } finally {
                        try {
                            xmlsr.close();
                        } catch (XMLStreamException e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }
                }
            }
            return false;
        } catch (XMLStreamException e) {
            // not a valid xml file
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Network import_(ReadOnlyDataSource dataSource, Properties parameters) {
        Network network;
        long startTime = System.currentTimeMillis();
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new RuntimeException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(EXTENSIONS) + " not found");
            }
            boolean throwExceptionIfExtensionNotFound = (Boolean) Importers.readParameter(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND);
            try (InputStream is = dataSource.newInputStream(null, ext)) {
                network = NetworkXml.read(is, new XmlImportConfig(throwExceptionIfExtensionNotFound));
            }
            LOGGER.debug("XML import done in {} ms", (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            throw new ITeslaException(e);
        }
        return network;
    }

}

