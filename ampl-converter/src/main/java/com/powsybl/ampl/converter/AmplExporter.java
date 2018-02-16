/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;

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

    public static final String EXPORT_RATIOTAPCHANGER_VT_PROPERTY = "iidm.export.ampl.exportRatioTapChangerVoltageTarget";
    public static final String EXTENSION_EXPORT_FACTORY_PROPERTY = "iidm.export.ampl.extensionExportFactoryClass";
    public static final String SPECIFIC_COMPATIBILITY_PROPERTY = "iidm.export.ampl.specificCompatibility";

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
            AmplExtensionExportFactory extensionFactory = null;
            boolean exportRatioTapChangerVoltageTarget = false;
            boolean specificCompatibility = false;
            if (parameters != null) {
                String extensionFactoryClassName = parameters.getProperty(EXTENSION_EXPORT_FACTORY_PROPERTY);
                if (extensionFactoryClassName != null) {
                    extensionFactory = Class.forName(extensionFactoryClassName).asSubclass(AmplExtensionExportFactory.class).newInstance();
                }
                exportRatioTapChangerVoltageTarget = Boolean.valueOf(parameters.getProperty(EXPORT_RATIOTAPCHANGER_VT_PROPERTY, "false"));
                specificCompatibility = Boolean.valueOf(parameters.getProperty(SPECIFIC_COMPATIBILITY_PROPERTY, "false"));
            }
            new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, false, AmplExportConfig.ExportActionType.CURATIVE, exportRatioTapChangerVoltageTarget, specificCompatibility, extensionFactory))
                    .write();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        } catch (InstantiationException e) {
            throw new UncheckedInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        }
    }
}
