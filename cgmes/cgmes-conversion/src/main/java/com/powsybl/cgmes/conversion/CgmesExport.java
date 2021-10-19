/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.export.*;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
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

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }

    @Override
    public void export(Network network, Properties params, DataSource ds) {
        Objects.requireNonNull(network);
        if (ConversionParameters.readBooleanParameter(getFormat(), params, USING_ONLY_NETWORK_PARAMETER)) {
            exportUsingOnlyNetwork(network, params, ds);
        } else {
            CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
            if (ext == null) {
                throw new CgmesModelException("CGMES model is required and not found in Network extension");
            }
            exportUsingOriginalCgmesModel(network, ds, ext);
        }
    }

    private String baseName(Network network, Properties params) {
        String baseName = ConversionParameters.readStringParameter(getFormat(), params, BASE_NAME_PARAMETER);
        return baseName != null ? baseName : network.getNameOrId();
    }

    private void exportUsingOnlyNetwork(Network network, Properties params, DataSource ds) {
        // At this point only SSH, SV can be exported when relying only in Network data
        // (minimum amount of CGMES references are expected as aliases/properties/extensions)
        String baseName = baseName(network, params);
        String filenameEq = baseName + "_EQ.xml";
        String filenameSv = baseName + "_SV.xml";
        String filenameSsh = baseName + "_SSH.xml";
        CgmesExportContext context = new CgmesExportContext(network)
                .setExportBoundaryPowerFlows(ConversionParameters.readBooleanParameter(getFormat(), params, EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER))
                .setExportFlowsForSwitches(ConversionParameters.readBooleanParameter(getFormat(), params, EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER));
        try (OutputStream oeq = ds.newOutputStream(filenameEq, false);
                OutputStream osv = ds.newOutputStream(filenameSv, false);
                OutputStream ossh = ds.newOutputStream(filenameSsh, false)) {
            XMLStreamWriter writer;
            writer = XmlUtil.initializeWriter(true, "    ", oeq);
            EquipmentExport.write(network, writer, context);
            writer = XmlUtil.initializeWriter(true, "    ", osv);
            StateVariablesExport.write(network, writer, context);
            writer = XmlUtil.initializeWriter(true, "    ", ossh);
            SteadyStateHypothesisExport.write(network, writer, context);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportUsingOriginalCgmesModel(Network network, DataSource ds, CgmesModelExtension ext) {
        CgmesUpdate cgmesUpdate = ext.getCgmesUpdate();
        CgmesModel cgmesSource = ext.getCgmesModel();
        CgmesModel cgmes = CgmesModelFactory.copy(cgmesSource);
        String variantId = network.getVariantManager().getWorkingVariantId();
        cgmesUpdate.update(cgmes, variantId);
        // Fill the State Variables data with the Network current state values
        StateVariablesAdder adder = new StateVariablesAdder(cgmes, network);
        adder.addStateVariablesToCgmes();
        cgmes.write(ds);
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    public static final String USING_ONLY_NETWORK = "iidm.export.cgmes.using-only-network";
    public static final String BASE_NAME = "iidm.export.cgmes.base-name";
    public static final String EXPORT_BOUNDARY_POWER_FLOWS = "iidm.export.cgmes.export-boundary-power-flows";
    public static final String EXPORT_POWER_FLOWS_FOR_SWITCHES = "iidm.export.cgmes.export-power-flows-for-switches";

    private static final Parameter USING_ONLY_NETWORK_PARAMETER = new Parameter(
            USING_ONLY_NETWORK,
            ParameterType.BOOLEAN,
            "Export to CGMES using only information present in IIDM Network (including extensions and aliases)",
            Boolean.FALSE);
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

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            USING_ONLY_NETWORK_PARAMETER,
            BASE_NAME_PARAMETER,
            EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER,
            EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER);
}
