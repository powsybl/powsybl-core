/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;
import static com.powsybl.iidm.xml.IidmXmlConstants.VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class XMLImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND
            = new Parameter("throwExceptionIfExtensionNotFound", ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE);

    private final ParameterDefaultValueConfig defaultValueConfig;

    private static final String SUFFIX_MAPPING = "_mapping";

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
    public List<Parameter> getParameters() {
        return Collections.singletonList(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND);
    }

    @Override
    public String getComment() {
        return "IIDM XML v " + VERSION + " importer";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource.getMainFileName() != null
                    && dataSource.fileExists(dataSource.getMainFileName())
                    && XmlConverterUtil.findExtension(dataSource) != null
                    && checkXmlNs(dataSource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getPrettyName(ReadOnlyDataSource dataSource) {
        String extension = XmlConverterUtil.findExtension(dataSource);
        return dataSource.getMainFileName().substring(0, dataSource.getMainFileName().length() - extension.length());
    }

    private boolean checkXmlNs(ReadOnlyDataSource dataSource) throws IOException {
        try {
            try (InputStream is = dataSource.newInputStream(dataSource.getMainFileName())) {
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
            return false;
        } catch (XMLStreamException e) {
            // not a valid xml file
            return false;
        }
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        try {
            // copy iidm file
            try (InputStream is = fromDataSource.newInputStream(fromDataSource.getMainFileName());
                 OutputStream os = toDataSource.newOutputStream(toDataSource.getMainFileName(), false)) {
                ByteStreams.copy(is, os);
            }
            String fromBaseName = XmlConverterUtil.getBaseName(fromDataSource);
            String toBaseName = XmlConverterUtil.getBaseName(toDataSource);
            // and also anonymization file if exists
            if (fromDataSource.fileExists(fromBaseName + "_mapping.csv")) {
                try (InputStream is = fromDataSource.newInputStream(fromBaseName + "_mapping.csv");
                     OutputStream os = toDataSource.newOutputStream(toBaseName + "_mapping.csv", false)) {
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
            boolean throwExceptionIfExtensionNotFound = (Boolean) Importers.readParameter(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, defaultValueConfig);
            Anonymizer anonymizer = null;
            String baseName = XmlConverterUtil.getBaseName(dataSource);
            if (dataSource.fileExists(baseName + "_mapping.csv")) {
                anonymizer = new SimpleAnonymizer();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(baseName + "_mapping.csv"), StandardCharsets.UTF_8))) {
                    anonymizer.read(reader);
                }
            }
            try (InputStream is = dataSource.newInputStream(dataSource.getMainFileName())) {
                network = NetworkXml.read(is, new ImportOptions(throwExceptionIfExtensionNotFound), anonymizer);
            }
            LOGGER.debug("XIIDM import done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new PowsyblException(e);
        }
        return network;
    }

}

