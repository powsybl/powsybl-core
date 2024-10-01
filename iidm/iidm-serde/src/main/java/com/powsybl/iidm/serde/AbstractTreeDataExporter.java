/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Tree data export of an IIDM model.<p>
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
 *         <td>iidm.export.xml.anonymised</td>
 *         <td>if true then exported network is anonymous</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.iidm-version-incompatibility-behavior</td>
 *         <td>behavior when there is an IIDM version incompatibility</td>
 *         <td>THROW_EXCEPTION or LOG_ERROR</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.topology-level</td>
 *         <td>the detail level used in the export of voltage levels</td>
 *         <td>NODE_BREAKER, BUS_BREAKER, BUS_BRANCH</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.throw-exception-if-extension-not-found</td>
 *         <td>if true throw exception when extension not found</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.extensions</td>
 *         <td>list of exported extensions</td>
 *         <td>comma-separated string</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.sorted</td>
 *         <td>sort export output file</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.version</td>
 *         <td>version in which files will be generated</td>
 *         <td>1.5 or 1.4 etc</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.with-automation-systems</td>
 *         <td>if true automation systems are exported</td>
 *         <td>true or false</td>
 *     </tr>
 * </table>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractTreeDataExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTreeDataExporter.class);

    private static final Supplier<ExtensionProviders<ExtensionSerDe>> EXTENSIONS_SUPPLIER = Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerDe.class, "network"));

    public static final String INDENT = "iidm.export.xml.indent";
    public static final String WITH_BRANCH_STATE_VARIABLES = "iidm.export.xml.with-branch-state-variables";
    public static final String ONLY_MAIN_CC = "iidm.export.xml.only-main-cc";
    public static final String ANONYMISED = "iidm.export.xml.anonymised";
    public static final String IIDM_VERSION_INCOMPATIBILITY_BEHAVIOR = "iidm.export.xml.iidm-version-incompatibility-behavior";
    public static final String TOPOLOGY_LEVEL = "iidm.export.xml.topology-level";
    public static final String THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND = "iidm.export.xml.throw-exception-if-extension-not-found";
    public static final String EXTENSIONS_LIST = "iidm.export.xml.extensions";
    public static final String SORTED = "iidm.export.xml.sorted";
    public static final String VERSION = "iidm.export.xml.version";
    public static final String WITH_AUTOMATION_SYSTEMS = "iidm.export.xml.with-automation-systems";

    private static final Parameter INDENT_PARAMETER = new Parameter(INDENT, ParameterType.BOOLEAN, "Indent export output file", Boolean.TRUE);
    private static final Parameter WITH_BRANCH_STATE_VARIABLES_PARAMETER = new Parameter(WITH_BRANCH_STATE_VARIABLES, ParameterType.BOOLEAN, "Export network with branch state variables", Boolean.TRUE);
    private static final Parameter ONLY_MAIN_CC_PARAMETER = new Parameter(ONLY_MAIN_CC, ParameterType.BOOLEAN, "Export only main CC", Boolean.FALSE);
    private static final Parameter ANONYMISED_PARAMETER = new Parameter(ANONYMISED, ParameterType.BOOLEAN, "Anonymise exported network", Boolean.FALSE);
    private static final Parameter IIDM_VERSION_INCOMPATIBILITY_BEHAVIOR_PARAMETER = new Parameter(IIDM_VERSION_INCOMPATIBILITY_BEHAVIOR, ParameterType.STRING, "Behavior when there is an IIDM version incompatibility", "THROW_EXCEPTION",
            List.of("LOG_ERROR", "THROW_EXCEPTION"));
    private static final Parameter TOPOLOGY_LEVEL_PARAMETER = new Parameter(TOPOLOGY_LEVEL, ParameterType.STRING, "Export network in this topology level",
            TopologyLevel.NODE_BREAKER.name(),
            Arrays.stream(TopologyLevel.values()).map(Enum::name).collect(Collectors.toList()));
    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER = new Parameter(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE);
    private static final Parameter EXTENSIONS_LIST_PARAMETER = new Parameter(EXTENSIONS_LIST, ParameterType.STRING_LIST,
            "The list of exported extensions", null,
            EXTENSIONS_SUPPLIER.get().getProviders().stream().map(ExtensionProvider::getExtensionName).collect(Collectors.toList()));
    private static final Parameter SORTED_PARAMETER = new Parameter(SORTED, ParameterType.BOOLEAN, "Sort export output file", Boolean.FALSE);
    private static final Parameter VERSION_PARAMETER = new Parameter(VERSION, ParameterType.STRING, "IIDM version in which files will be generated", IidmSerDeConstants.CURRENT_IIDM_VERSION.toString("."),
            Arrays.stream(IidmVersion.values()).map(v -> v.toString(".")).collect(Collectors.toList()));
    private static final Parameter WITH_AUTOMATION_SYSTEMS_PARAMETER = new Parameter(WITH_AUTOMATION_SYSTEMS, ParameterType.BOOLEAN,
            "Export network with automation systems", Boolean.TRUE);
    private static final List<Parameter> STATIC_PARAMETERS = List.of(INDENT_PARAMETER, WITH_BRANCH_STATE_VARIABLES_PARAMETER,
            ONLY_MAIN_CC_PARAMETER, ANONYMISED_PARAMETER, IIDM_VERSION_INCOMPATIBILITY_BEHAVIOR_PARAMETER,
            TOPOLOGY_LEVEL_PARAMETER, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, EXTENSIONS_LIST_PARAMETER,
            SORTED_PARAMETER, VERSION_PARAMETER, WITH_AUTOMATION_SYSTEMS_PARAMETER);

    private final ParameterDefaultValueConfig defaultValueConfig;

    protected AbstractTreeDataExporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    protected abstract TreeDataFormat getTreeDataFormat();

    protected abstract String getExtension();

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }
        ExportOptions options = createExportOptions(parameters);
        try {
            long startTime = System.currentTimeMillis();
            NetworkSerDe.write(network, options, dataSource, getExtension());
            LOGGER.debug("{} export done in {} ms", getFormat(), System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Parameter> getParameters() {
        return ConfiguredParameter.load(STATIC_PARAMETERS, getFormat(), defaultValueConfig);
    }

    private void addExtensionsVersions(Properties parameters, ExportOptions options) {
        EXTENSIONS_SUPPLIER.get().getProviders().forEach(extensionSerializer -> {
            String extensionName = extensionSerializer.getExtensionName();
            Parameter parameter = new Parameter("iidm.export.xml." + extensionName + ".version",
                    ParameterType.STRING, "Version of " + extensionName, null);
            String extensionVersion = Parameter.readString(getFormat(), parameters, parameter, defaultValueConfig);
            if (extensionVersion != null) {
                if (options.getExtensions().map(extensions -> extensions.contains(extensionName)).orElse(true)) {
                    options.addExtensionVersion(extensionName, extensionVersion);
                } else {
                    LOGGER.warn(String.format("Version of %s is ignored since %s is not in the extensions list to export.",
                            extensionName, extensionName));
                }
            }
        });
    }

    private ExportOptions createExportOptions(Properties parameters) {
        ExportOptions options = new ExportOptions()
                .setIndent(Parameter.readBoolean(getFormat(), parameters, INDENT_PARAMETER, defaultValueConfig))
                .setWithBranchSV(Parameter.readBoolean(getFormat(), parameters, WITH_BRANCH_STATE_VARIABLES_PARAMETER, defaultValueConfig))
                .setOnlyMainCc(Parameter.readBoolean(getFormat(), parameters, ONLY_MAIN_CC_PARAMETER, defaultValueConfig))
                .setAnonymized(Parameter.readBoolean(getFormat(), parameters, ANONYMISED_PARAMETER, defaultValueConfig))
                .setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.valueOf(Parameter.readString(getFormat(), parameters, IIDM_VERSION_INCOMPATIBILITY_BEHAVIOR_PARAMETER, defaultValueConfig)))
                .setTopologyLevel(TopologyLevel.valueOf(Parameter.readString(getFormat(), parameters, TOPOLOGY_LEVEL_PARAMETER, defaultValueConfig)))
                .setThrowExceptionIfExtensionNotFound(Parameter.readBoolean(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, defaultValueConfig))
                .setExtensions(Parameter.readStringList(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig) != null ? new HashSet<>(Parameter.readStringList(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig)) : null)
                .setSorted(Parameter.readBoolean(getFormat(), parameters, SORTED_PARAMETER, defaultValueConfig))
                .setVersion(Parameter.readString(getFormat(), parameters, VERSION_PARAMETER, defaultValueConfig))
                .setFormat(getTreeDataFormat())
                .setWithAutomationSystems(Parameter.readBoolean(getFormat(), parameters, WITH_AUTOMATION_SYSTEMS_PARAMETER, defaultValueConfig));
        addExtensionsVersions(parameters, options);
        return options;
    }
}
