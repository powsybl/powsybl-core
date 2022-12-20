/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesProfileExporterFactory;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.CgmesReports.inconsistentProfilesTPRequiredReport;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    private static final String INDENT = "    ";

    private final ParameterDefaultValueConfig defaultValueConfig;

    public CgmesExport(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    public CgmesExport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }

    @Override
    public void export(Network network, Properties params, DataSource ds, Reporter reporter) {
        Objects.requireNonNull(network);
        CgmesExportContext context = new CgmesExportContext(
                network,
                NamingStrategyFactory.create(Parameter.readString(getFormat(), params, NAMING_STRATEGY_PARAMETER, defaultValueConfig)))
                .setExportBoundaryPowerFlows(Parameter.readBoolean(getFormat(), params, EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER, defaultValueConfig))
                .setExportFlowsForSwitches(Parameter.readBoolean(getFormat(), params, EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER, defaultValueConfig))
                .setBoundaryEqId(getBoundaryId("EQ", network, params, BOUNDARY_EQ_ID_PARAMETER))
                .setBoundaryTpId(getBoundaryId("TP", network, params, BOUNDARY_TP_ID_PARAMETER))
                .setReporter(reporter);
        String cimVersionParam = Parameter.readString(getFormat(), params, CIM_VERSION_PARAMETER, defaultValueConfig);
        if (cimVersionParam != null) {
            context.setCimVersion(Integer.parseInt(cimVersionParam));
        }
        String baseName = baseName(params, ds, network);
        // Process the requested profiles in the proper order
        // First export EQ, then TP, then SSH, then SV
        Set<String> requestedProfiles = new HashSet<>(Parameter.readStringList(getFormat(), params, PROFILES_PARAMETER, defaultValueConfig));
        checkConsistency(requestedProfiles, context);
        String[] profiles = {"EQ", "TP", "SSH", "SV"};
        for (String profile : profiles) {
            if (requestedProfiles.contains(profile)) {
                export(profile, baseName, ds, context);
            } else {
                addProfilesIdentifiers(network, profile, context.getModelDescription(profile));
                if (profile.equals("EQ")) {
                    context.getEqModelDescription().addId(context.getNamingStrategy().getCgmesId(network));
                }
            }
        }
        context.getNamingStrategy().writeIdMapping(baseName + "_id_mapping.csv", ds);
    }

    private void export(String profile, String baseName, DataSource ds, CgmesExportContext context) {
        try {
            String filename = baseName + "_" + profile + ".xml";
            try (OutputStream out = new BufferedOutputStream(ds.newOutputStream(filename, false))) {
                XMLStreamWriter xmlWriter = XmlUtil.initializeWriter(true, INDENT, out);
                CgmesProfileExporterFactory.create(profile, context, xmlWriter).export();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private String getBoundaryId(String profile, Network network, Properties params, Parameter parameter) {
        if (network.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_BD_ID")) {
            return network.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_BD_ID");
        }
        return Parameter.readString(getFormat(), params, parameter, defaultValueConfig);
    }

    private static void addProfilesIdentifiers(Network network, String profile, CgmesExportContext.ModelDescription description) {
        description.setIds(network.getPropertyNames().stream()
                .filter(p -> p.startsWith(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_ID"))
                .map(network::getProperty)
                .collect(Collectors.toList()));
    }

    private static void checkConsistency(Set<String> profiles, CgmesExportContext context) {
        boolean networkIsNodeBreaker = context.getNetwork().getVoltageLevelStream()
                .map(VoltageLevel::getTopologyKind)
                .anyMatch(tk -> tk == TopologyKind.NODE_BREAKER);
        if (networkIsNodeBreaker
                && (profiles.contains("SSH") || profiles.contains("SV"))
                && !profiles.contains("TP")) {
            inconsistentProfilesTPRequiredReport(context.getReporter(), context.getNetwork().getId());
            LOG.error("Network {} contains node/breaker information. References to Topological Nodes in SSH/SV files will not be valid if TP is not exported.", context.getNetwork().getId());
        }
    }

    private String baseName(Properties params, DataSource ds, Network network) {
        String baseName = Parameter.readString(getFormat(), params, BASE_NAME_PARAMETER);
        if (baseName != null) {
            return baseName;
        } else if (ds.getBaseName() != null && !ds.getBaseName().isEmpty()) {
            return ds.getBaseName();
        }
        return network.getNameOrId();
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
    public static final String CIM_VERSION = "iidm.export.cgmes.cim-version";
    public static final String EXPORT_BOUNDARY_POWER_FLOWS = "iidm.export.cgmes.export-boundary-power-flows";
    public static final String EXPORT_POWER_FLOWS_FOR_SWITCHES = "iidm.export.cgmes.export-power-flows-for-switches";
    public static final String NAMING_STRATEGY = "iidm.export.cgmes.naming-strategy";
    public static final String PROFILES = "iidm.export.cgmes.profiles";
    public static final String BOUNDARY_EQ_ID = "iidm.export.cgmes.boundary-EQ-identifier";
    public static final String BOUNDARY_TP_ID = "iidm.export.cgmes.boundary-TP-identifier";

    private static final Parameter BASE_NAME_PARAMETER = new Parameter(
            BASE_NAME,
            ParameterType.STRING,
            "Basename for output files",
            null);
    private static final Parameter CIM_VERSION_PARAMETER = new Parameter(
            CIM_VERSION,
            ParameterType.STRING,
            "CIM version to export",
            null,
            CgmesNamespace.CIM_LIST.stream().map(cim -> Integer.toString(cim.getVersion())).collect(Collectors.toList()));
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
    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(
            NAMING_STRATEGY,
            ParameterType.STRING,
            "Configure what type of naming strategy you want",
            NamingStrategyFactory.IDENTITY,
            new ArrayList<>(NamingStrategyFactory.LIST));
    private static final Parameter PROFILES_PARAMETER = new Parameter(
            PROFILES,
            ParameterType.STRING_LIST,
            "Profiles to export",
            List.of("EQ", "TP", "SSH", "SV"),
            List.of("EQ", "TP", "SSH", "SV"));
    private static final Parameter BOUNDARY_EQ_ID_PARAMETER = new Parameter(
            BOUNDARY_EQ_ID,
            ParameterType.STRING,
            "Boundary EQ model identifier",
            null);
    private static final Parameter BOUNDARY_TP_ID_PARAMETER = new Parameter(
            BOUNDARY_TP_ID,
            ParameterType.STRING,
            "Boundary TP model identifier",
            null);

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            BASE_NAME_PARAMETER,
            CIM_VERSION_PARAMETER,
            EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER,
            EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER,
            NAMING_STRATEGY_PARAMETER,
            PROFILES_PARAMETER,
            BOUNDARY_EQ_ID_PARAMETER,
            BOUNDARY_TP_ID_PARAMETER);

    private static final Logger LOG = LoggerFactory.getLogger(CgmesExport.class);
}
