/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.multi.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.XMLExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(Exporter.class)
public class MultiXMLExporter extends XMLExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiXMLExporter.class);

    public static final String EXPORT_MODE = "iidm.export.xml.export-mode";

    private static final Parameter EXPORT_MODE_PARAMETER = new Parameter(EXPORT_MODE, ParameterType.STRING, "export each extension in a separate file", String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

    @Override
    public String getFormat() {
        return "M-XIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM multi-files XML v" + IidmXmlConstants.CURRENT_IIDM_XML_VERSION.toString(".") + " exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }
        MultiXMLExportOptions options = new MultiXMLExportOptions();
        buildExportOptions(parameters, options);
        IidmImportExportMode mode = IidmImportExportMode.valueOf(ConversionParameters.readStringParameter(getFormat(), parameters, EXPORT_MODE_PARAMETER, defaultValueConfig));
        if (!IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE.equals(mode)
                && !IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE.equals(mode)) {
            throw new PowsyblException("Unexpected mode for multi-files IIDM-XML export: " + mode);
        }
        options.setMode(mode);
        try {
            long startTime = System.currentTimeMillis();
            NetworkMultiXml.write(network, options, dataSource, "xiidm");
            LOGGER.debug("XIIDM export done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
}
