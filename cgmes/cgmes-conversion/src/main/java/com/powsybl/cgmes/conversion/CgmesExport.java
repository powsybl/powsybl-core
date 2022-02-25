/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.export.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    private static final String INDENT = "    ";

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }

    @Override
    public void export(Network network, Properties params, DataSource ds) {
        Objects.requireNonNull(network);
        String baseName = baseName(network, params);
        String filenameEq = baseName + "_EQ.xml";
        String filenameTp = baseName + "_TP.xml";
        String filenameSsh = baseName + "_SSH.xml";
        String filenameSv = baseName + "_SV.xml";
        CgmesExportContext context = new CgmesExportContext(network)
                .setExportBoundaryPowerFlows(ConversionParameters.readBooleanParameter(getFormat(), params, EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER))
                .setExportFlowsForSwitches(ConversionParameters.readBooleanParameter(getFormat(), params, EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER));
        try {
            List<String> profiles = ConversionParameters.readStringListParameter(getFormat(), params, PROFILES_PARAMETER);
            if (profiles.contains("EQ")) {
                try (OutputStream out = new BufferedOutputStream(ds.newOutputStream(filenameEq, false))) {
                    XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
                    EquipmentExport.write(network, writer, context);
                }
            }
            if (profiles.contains("TP")) {
                try (OutputStream out = new BufferedOutputStream(ds.newOutputStream(filenameTp, false))) {
                    XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
                    TopologyExport.write(network, writer, context);
                }
            }
            if (profiles.contains("SSH")) {
                try (OutputStream out = new BufferedOutputStream(ds.newOutputStream(filenameSsh, false))) {
                    XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
                    SteadyStateHypothesisExport.write(network, writer, context);
                }
            }
            if (profiles.contains("SV")) {
                try (OutputStream out = new BufferedOutputStream(ds.newOutputStream(filenameSv, false))) {
                    XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
                    StateVariablesExport.write(network, writer, context);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private String baseName(Network network, Properties params) {
        String baseName = ConversionParameters.readStringParameter(getFormat(), params, BASE_NAME_PARAMETER);
        return baseName != null ? baseName : network.getNameOrId();
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    public static final String BASE_NAME = "iidm.export.cgmes.base-name";
    public static final String EXPORT_BOUNDARY_POWER_FLOWS = "iidm.export.cgmes.export-boundary-power-flows";
    public static final String EXPORT_POWER_FLOWS_FOR_SWITCHES = "iidm.export.cgmes.export-power-flows-for-switches";
    public static final String PROFILES = "iidm.export.cgmes.profiles";

    private static final Parameter BASE_NAME_PARAMETER = new Parameter(
            BASE_NAME,
            ParameterType.STRING,
            "Basename for output files",
            null);
    private static final Parameter EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER = new Parameter(
            EXPORT_BOUNDARY_POWER_FLOWS,
            ParameterType.BOOLEAN,
            "Export boundaries' power flows",
            Boolean.TRUE);
    private static final Parameter EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER = new Parameter(
            EXPORT_POWER_FLOWS_FOR_SWITCHES,
            ParameterType.BOOLEAN,
            "Export power flows for switches",
            Boolean.FALSE);
    private static final Parameter PROFILES_PARAMETER = new Parameter(
            PROFILES,
            ParameterType.STRING_LIST,
            "Profiles to export",
            List.of("EQ", "TP", "SSH", "SV"));

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            BASE_NAME_PARAMETER,
            EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER,
            EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER,
            PROFILES_PARAMETER);
}
