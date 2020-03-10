/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.multi.xml;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.iidm.xml.XMLImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(Importer.class)
public class MultiXMLImporter extends XMLImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiXMLImporter.class);

    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml", "iidm.xml"};

    public static final String IMPORT_MODE = "iidm.import.xml.import-mode";

    private static final Parameter IMPORT_MODE_PARAMETER
            = new Parameter(IMPORT_MODE, ParameterType.STRING, "import mode for multi-files import", String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

    @Override
    public String getFormat() {
        return "M-XIIDM";
    }

    @Override
    public List<Parameter> getParameters() {
        return ImmutableList.of(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, EXTENSIONS_LIST_PARAMETER, IMPORT_MODE_PARAMETER);
    }

    @Override
    public String getComment() {
        return "IIDM multi-files XML v " + CURRENT_IIDM_XML_VERSION.toString(".") + " importer";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            return exists(dataSource, ext) && extensionsExist(dataSource, ext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Network network;
        MultiXMLImportOptions options = new MultiXMLImportOptions();
        buildImportOptions(parameters, options);
        options.setMode(IidmImportExportMode.valueOf(ConversionParameters.readStringParameter(getFormat(), parameters, IMPORT_MODE_PARAMETER, defaultValueConfig)));
        long startTime = System.currentTimeMillis();
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new PowsyblException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(EXTENSIONS) + " not found");
            }
            network = NetworkMultiXml.read(dataSource, networkFactory, options, ext);
            LOGGER.debug("XIIDM import done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new PowsyblException(e);
        }
        return network;
    }
}
