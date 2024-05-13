/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.google.auto.service.AutoService;
import com.powsybl.ampl.converter.version.AmplExportVersion;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Exporter.class)
public class AmplExporter implements Exporter {

    public static final String EXPORT_SCOPE = "iidm.export.ampl.scope";
    public static final String EXPORT_XNODES = "iidm.export.ampl.with-xnodes";
    public static final String EXPORT_ACTION_TYPE = "iidm.export.ampl.action-type";
    public static final String EXPORT_RATIOTAPCHANGER_VT = "iidm.export.ampl.export-ratio-tap-changer-voltage-target";
    public static final String TWT_SPLIT_SHUNT_ADMITTANCE = "iidm.export.ampl.twt-split-shunt-admittance";
    public static final String EXPORT_VERSION = "iidm.export.ampl.export-version";
    public static final String EXPORT_SORTED = "iidm.export.ampl.export-sorted";

    private static final Parameter EXPORT_SCOPE_PARAMETER = new Parameter(EXPORT_SCOPE, ParameterType.STRING, "Export scope",
            AmplExportConfig.ExportScope.ALL.name(),
        Arrays.stream(AmplExportConfig.ExportScope.values()).map(Enum::name).collect(Collectors.toList()));
    private static final Parameter EXPORT_XNODES_PARAMETER = new Parameter(EXPORT_XNODES, ParameterType.BOOLEAN,
        "Export Xnodes of tie-lines", Boolean.FALSE);
    private static final Parameter EXPORT_ACTION_TYPE_PARAMETER = new Parameter(EXPORT_ACTION_TYPE,
        ParameterType.STRING, "Type of the remedial actions (preventive or curative)",
        AmplExportConfig.ExportActionType.CURATIVE.name(),
        Arrays.stream(AmplExportConfig.ExportActionType.values()).map(Enum::name).collect(Collectors.toList()));
    private static final Parameter EXPORT_RATIOTAPCHANGER_VT_PARAMETER = new Parameter(EXPORT_RATIOTAPCHANGER_VT,
        ParameterType.BOOLEAN, "Export ratio tap changer voltage target", Boolean.FALSE)
        .addAdditionalNames("iidm.export.ampl.exportRatioTapChangerVoltageTarget");
    private static final Parameter TWT_SPLIT_SHUNT_ADMITTANCE_PARAMETER = new Parameter(TWT_SPLIT_SHUNT_ADMITTANCE,
        ParameterType.BOOLEAN, "Export twt split shunt admittance", Boolean.FALSE)
        .addAdditionalNames("iidm.export.ampl.specific-compatibility")
        .addAdditionalNames("iidm.export.ampl.specificCompatibility");
    private static final Parameter EXPORT_VERSION_PARAMETER = new Parameter(EXPORT_VERSION, ParameterType.STRING,
        "The version of the export.",
            AmplExportVersion.defaultVersion().getExporterId(),
            new ArrayList<>(AmplExportVersion.exporterIdValues()));

    private static final Parameter EXPORT_SORTED_PARAMETER = new Parameter(EXPORT_SORTED, ParameterType.BOOLEAN, "Export alphabetically sorted by equipment id", Boolean.FALSE);

    private static final List<Parameter> STATIC_PARAMETERS = List.of(EXPORT_SCOPE_PARAMETER, EXPORT_XNODES_PARAMETER,
        EXPORT_ACTION_TYPE_PARAMETER, EXPORT_RATIOTAPCHANGER_VT_PARAMETER, TWT_SPLIT_SHUNT_ADMITTANCE_PARAMETER,
        EXPORT_VERSION_PARAMETER, EXPORT_SORTED_PARAMETER);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public AmplExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public AmplExporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getFormat() {
        return "AMPL";
    }

    @Override
    public String getComment() {
        return "IIDM to AMPL converter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        try {
            AmplExportConfig.ExportScope scope = AmplExportConfig.ExportScope.valueOf(Parameter.readString(getFormat(), parameters, EXPORT_SCOPE_PARAMETER, defaultValueConfig));
            boolean exportXnodes = Parameter.readBoolean(getFormat(), parameters, EXPORT_XNODES_PARAMETER, defaultValueConfig);
            AmplExportConfig.ExportActionType actionType = AmplExportConfig.ExportActionType.valueOf(Parameter.readString(getFormat(), parameters, EXPORT_ACTION_TYPE_PARAMETER, defaultValueConfig));
            boolean exportRatioTapChangerVoltageTarget = Parameter.readBoolean(getFormat(), parameters, EXPORT_RATIOTAPCHANGER_VT_PARAMETER, defaultValueConfig);
            boolean twtSplitShuntAdmittance = Parameter.readBoolean(getFormat(), parameters,
                TWT_SPLIT_SHUNT_ADMITTANCE_PARAMETER, defaultValueConfig);
            AmplExportVersion exportVersion = AmplExportVersion.fromExporterId(
                Parameter.readString(getFormat(), parameters, EXPORT_VERSION_PARAMETER, defaultValueConfig));
            boolean exportSorted = Parameter.readBoolean(getFormat(), parameters, EXPORT_SORTED_PARAMETER, defaultValueConfig);

            AmplExportConfig config = new AmplExportConfig(scope, exportXnodes, actionType,
                exportRatioTapChangerVoltageTarget, twtSplitShuntAdmittance, exportVersion, exportSorted);

            new AmplNetworkWriter(network, dataSource, config).write();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void export(Network network, AmplExportConfig config, DataSource dataSource) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(config);
        try {
            new AmplNetworkWriter(network, dataSource, config).write();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }
}
