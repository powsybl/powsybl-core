/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class AmplExporter implements Exporter {

    public static final String EXPORT_RATIOTAPCHANGER_VT = "iidm.export.ampl.export-ratio-tap-changer-voltage-target";
    public static final String SPECIFIC_COMPATIBILITY = "iidm.export.ampl.specific-compatibility";

    private static final Parameter EXPORT_RATIOTAPCHANGER_VT_PARAMETER = new Parameter(EXPORT_RATIOTAPCHANGER_VT, ParameterType.BOOLEAN, "Export ratio tap changer voltage target", Boolean.FALSE)
            .addAdditionalNames("iidm.export.ampl.exportRatioTapChangerVoltageTarget");
    private static final Parameter SPECIFIC_COMPATIBILITY_PARAMETER = new Parameter(SPECIFIC_COMPATIBILITY, ParameterType.BOOLEAN, "Export specific compatibility", Boolean.FALSE)
            .addAdditionalNames("iidm.export.ampl.specificCompatibility");

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
            boolean exportRatioTapChangerVoltageTarget = ConversionParameters.readBooleanParameter(getFormat(), parameters, EXPORT_RATIOTAPCHANGER_VT_PARAMETER, defaultValueConfig);
            boolean specificCompatibility = ConversionParameters.readBooleanParameter(getFormat(), parameters, SPECIFIC_COMPATIBILITY_PARAMETER, defaultValueConfig);
            new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, false, AmplExportConfig.ExportActionType.CURATIVE, exportRatioTapChangerVoltageTarget, specificCompatibility))
                    .write();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
