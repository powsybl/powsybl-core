/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;

import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.powsybl.commons.xml.IidmXmlConstants.*;

/**
 * XML export of an IIDM model.<p>
 * <table border="1">
 *     <tr>
 *         <td><b>property name</b></td>
 *         <td><b>comment</b></td>
 *         <td><b>possible values</b></td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.indent</td>
 *         <td>if true write indented xml (4 spaces)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.with-branch-state-variables</td>
 *         <td>if true export branches state (active and reactive flow)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.only-main-cc</td>
 *         <td>if true only export equipments of the main connected component</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.topology-level</td>
 *         <td>the detail level used in the export of voltage levels</td>
 *         <td>NODE_BREAKER, BUS_BREAKER, BUS_BRANCH</td>
 *     </tr>
 * </table>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class XMLExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLExporter.class);

    public static final String INDENT = "iidm.export.xml.indent";
    public static final String WITH_BRANCH_STATE_VARIABLES = "iidm.export.xml.with-branch-state-variables";
    public static final String ONLY_MAIN_CC = "iidm.export.xml.only-main-cc";
    public static final String ANONYMISED = "iidm.export.xml.anonymised";
    public static final String TOPOLOGY_LEVEL = "iidm.export.xml.topology-level";
    public static final String THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND = "iidm.export.xml.throw-exception-if-extension-not-found";
    public static final String EXPORT_MODE = "iidm.export.xml.export-mode";
    public static final String EXTENSIONS_LIST = "iidm.export.xml.extensions";
    public static final String SKIP_EXTENSIONS = "iidm.export.xml.skip-extensions";

    private static final Parameter INDENT_PARAMETER = new Parameter(INDENT, ParameterType.BOOLEAN, "Indent export output file", Boolean.TRUE);
    private static final Parameter WITH_BRANCH_STATE_VARIABLES_PARAMETER = new Parameter(WITH_BRANCH_STATE_VARIABLES, ParameterType.BOOLEAN, "Export network with branch state variables", Boolean.TRUE);
    private static final Parameter ONLY_MAIN_CC_PARAMETER = new Parameter(ONLY_MAIN_CC, ParameterType.BOOLEAN, "Export only main CC", Boolean.FALSE);
    private static final Parameter ANONYMISED_PARAMETER = new Parameter(ANONYMISED, ParameterType.BOOLEAN, "Anonymise exported network", Boolean.FALSE);
    private static final Parameter TOPOLOGY_LEVEL_PARAMETER = new Parameter(TOPOLOGY_LEVEL, ParameterType.STRING, "Export network in this topology level", "NODE_BREAKER");
    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER = new Parameter(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE);
    private static final Parameter EXPORT_MODE_PARAMETER = new Parameter(EXPORT_MODE, ParameterType.STRING, "export each extension in a separate file", String.valueOf(IidmImportExportMode.UNIQUE_FILE));
    private static final Parameter EXTENSIONS_LIST_PARAMETER = new Parameter(EXTENSIONS_LIST, ParameterType.STRING_LIST, "The list of exported extensions", null);
    private static final Parameter SKIP_EXTENSIONS_PARAMETER = new Parameter(SKIP_EXTENSIONS, ParameterType.BOOLEAN, "Skip exporting the extensions", Boolean.FALSE);
    private final ParameterDefaultValueConfig defaultValueConfig;

    public XMLExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public XMLExporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM XML v" + CURRENT_IIDM_XML_VERSION.toString(".") + " exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }
        ExportOptions options = new ExportOptions()
                .setIndent(ConversionParameters.readBooleanParameter(getFormat(), parameters, INDENT_PARAMETER, defaultValueConfig))
                .setWithBranchSV(ConversionParameters.readBooleanParameter(getFormat(), parameters, WITH_BRANCH_STATE_VARIABLES_PARAMETER, defaultValueConfig))
                .setOnlyMainCc(ConversionParameters.readBooleanParameter(getFormat(), parameters, ONLY_MAIN_CC_PARAMETER, defaultValueConfig))
                .setAnonymized(ConversionParameters.readBooleanParameter(getFormat(), parameters, ANONYMISED_PARAMETER, defaultValueConfig))
                .setTopologyLevel(TopologyLevel.valueOf(ConversionParameters.readStringParameter(getFormat(), parameters, TOPOLOGY_LEVEL_PARAMETER, defaultValueConfig)))
                .setThrowExceptionIfExtensionNotFound(ConversionParameters.readBooleanParameter(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, defaultValueConfig))
                .setMode(IidmImportExportMode.valueOf(ConversionParameters.readStringParameter(getFormat(), parameters, EXPORT_MODE_PARAMETER, defaultValueConfig)))
                .setSkipExtensions(ConversionParameters.readBooleanParameter(getFormat(), parameters, SKIP_EXTENSIONS_PARAMETER, defaultValueConfig))
                .setExtensions(ConversionParameters.readStringListParameter(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig) != null ? new HashSet<>(ConversionParameters.readStringListParameter(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig)) : null);
        try {
            long startTime = System.currentTimeMillis();
            NetworkXml.write(network, options, dataSource, "xiidm");
            LOGGER.debug("XIIDM export done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
