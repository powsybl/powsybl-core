/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class XMLImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImporter.class);

    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml", "iidm.xml"};

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    public static final String THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND = "iidm.import.xml.throw-exception-if-extension-not-found";

    public static final String EXTENSIONS_LIST = "iidm.import.xml.extensions";

    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER
            = new Parameter(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE)
            .addAdditionalNames("throwExceptionIfExtensionNotFound");

    private static final Parameter EXTENSIONS_LIST_PARAMETER
            = new Parameter(EXTENSIONS_LIST, ParameterType.STRING_LIST, "The list of extension files ", null);

    private final ParameterDefaultValueConfig defaultValueConfig;

    static final String SUFFIX_MAPPING = "_mapping";

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
        return ImmutableList.of(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, EXTENSIONS_LIST_PARAMETER);
    }

    @Override
    public String getComment() {
        return "IIDM XML v " + CURRENT_IIDM_XML_VERSION.toString(".") + " importer";
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
                                return NetworkXml.NETWORK_ROOT_ELEMENT_NAME.equals(name)
                                        && Stream.of(IidmXmlVersion.values()).anyMatch(v -> v.getNamespaceURI().equals(ns));
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
                throw new PowsyblException("From data source is not importable");
            }
            // copy iidm file
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
                ByteStreams.copy(is, os);
            }
            // and also anonymization file if exists
            if (fromDataSource.exists(SUFFIX_MAPPING, "csv")) {
                try (InputStream is = fromDataSource.newInputStream(SUFFIX_MAPPING, "csv");
                     OutputStream os = toDataSource.newOutputStream(SUFFIX_MAPPING, "csv", false)) {
                    ByteStreams.copy(is, os);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Network network;

        ImportOptions options = createImportOptions(parameters);
        long startTime = System.currentTimeMillis();
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new PowsyblException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(EXTENSIONS) + " not found");
            }

            network = NetworkXml.read(dataSource, networkFactory, options, ext);
            LOGGER.debug("XIIDM import done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new PowsyblException(e);
        }
        return network;
    }

    private ImportOptions createImportOptions(Properties parameters) {
        return new ImportOptions()
                .setThrowExceptionIfExtensionNotFound(ConversionParameters.readBooleanParameter(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, defaultValueConfig))
                .setExtensions(ConversionParameters.readStringListParameter(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig) != null ? new HashSet<>(ConversionParameters.readStringListParameter(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig)) : null);
    }
}

