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
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.parameters.Parameter;
import eu.itesla_project.iidm.parameters.ParameterDefaultValueConfig;
import eu.itesla_project.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class XMLImporter implements Importer, XmlConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);

    private static final String[] EXTENSIONS = { "xiidm", "iidm", "xml" };

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND
            = new Parameter("throwExceptionIfExtensionNotFound", ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public XMLImporter() {
        this(PlatformConfig.defaultConfig());
    }

    public XMLImporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getFormat() {
        return "XIIDM";
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
            return exists(dataSource, ext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        try {
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
        }
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        try {
            String ext = findExtension(fromDataSource);
            if (!exists(fromDataSource, ext)) {
                throw new RuntimeException("From data source is not importable");
            }
            // copy iidm file
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null ,ext, false)) {
                ByteStreams.copy(is, os);
            }
            // and also anonymization file if exists
            if (fromDataSource.exists("_mapping", "csv")) {
                try (InputStream is = fromDataSource.newInputStream("_mapping", "csv");
                     OutputStream os = toDataSource.newOutputStream("_mapping", "csv", false)) {
                    ByteStreams.copy(is, os);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Network network;
        long startTime = System.currentTimeMillis();
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new RuntimeException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(EXTENSIONS) + " not found");
            }
            boolean throwExceptionIfExtensionNotFound = (Boolean) Importers.readParameter(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, defaultValueConfig);
            Anonymizer anonymizer = null;
            if (dataSource.exists("_mapping", "csv")) {
                anonymizer = new SimpleAnonymizer();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_mapping", "csv"), StandardCharsets.UTF_8))) {
                    anonymizer.read(reader);
                }
            }
            try (InputStream is = dataSource.newInputStream(null, ext)) {
                network = NetworkXml.read(is, new XmlImportConfig(throwExceptionIfExtensionNotFound), anonymizer);
            }
            LOGGER.debug("XIIDM import done in {} ms", (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            throw new ITeslaException(e);
        }
        return network;
    }

}

