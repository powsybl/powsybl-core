/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.psse.converter.extensions.PsseConversionContextExtension;
import com.powsybl.psse.converter.extensions.PsseModelExtension;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.io.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.powsybl.psse.converter.VoltageLevelConverter.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(Exporter.class)
public class PsseExporter implements Exporter {

    private static final double BASE_MVA = 100;

    private static final String FORMAT = "PSS/E";

    @Override
    public List<Parameter> getParameters() {
        return ConfiguredParameter.load(STATIC_PARAMETERS, getFormat(), ParameterDefaultValueConfig.INSTANCE);
    }

    private static final List<Parameter> STATIC_PARAMETERS = List.of();

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getComment() {
        return "Update IIDM to PSS/E ";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        PssePowerFlowModel updatePsseModel;
        Context context;
        boolean isFullExport = isFullExport(network);
        if (isFullExport) {
            updatePsseModel = createPsseModel(network);
            context = PowerFlowDataFactory.createPsseContext();
        } else {
            PssePowerFlowModel psseModel = network.getExtension(PsseModelExtension.class).getPsseModel();
            updatePsseModel = createUpdatePsseModel(network, psseModel);
            context = network.getExtension(PsseConversionContextExtension.class).getContext();
        }

        PsseVersion version = PsseVersion.fromRevision(updatePsseModel.getCaseIdentification().getRev());
        if (context.getFileFormat() == FileFormat.JSON) {
            if (Objects.requireNonNull(version.major()) == PsseVersion.Major.V35) {
                PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
                try {
                    rawXData35.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                throw new PsseException("Unsupported version " + version);
            }
        } else {
            exportNotJson(context, updatePsseModel, version, dataSource);
        }
    }

    // TODO, it should be defined properly, or it may need to be specified by the user
    private static boolean isFullExport(Network network) {
        return network.getExtension(PsseModelExtension.class) == null;
    }

    private void exportNotJson(Context context, PssePowerFlowModel updatePsseModel, PsseVersion version, DataSource dataSource) {
        switch (version.major()) {
            case V35 -> {
                PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
                try {
                    rawData35.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            case V33 -> {
                PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
                try {
                    rawData33.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            case V32 -> {
                PowerFlowRawData32 rawData32 = new PowerFlowRawData32();
                try {
                    rawData32.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            default -> throw new PsseException("Unsupported version " + version);
        }
    }

    // New equipment is not supported
    // Antennas (Branches connected only at one end) are exported as out of service (both sides open)
    // New buses are created in voltageLevels with nodeBreaker topology when buses are split
    private static PssePowerFlowModel createUpdatePsseModel(Network network, PssePowerFlowModel psseModel) {
        // Only the updated blocks are copied, non-updated blocks are referenced
        PssePowerFlowModel updatedPsseModel = psseModel.referenceAndCopyPssePowerFlowModel();
        updateModifiedBlocks(network, updatedPsseModel);
        return updatedPsseModel;
    }

    private static void updateModifiedBlocks(Network network, PssePowerFlowModel updatedPsseModel) {
        ContextExport contextExport = VoltageLevelConverter.createContextExport(network, updatedPsseModel, false);

        VoltageLevelConverter.updateSubstations(network, contextExport);

        BusConverter.updateBuses(updatedPsseModel, contextExport);
        LoadConverter.updateLoads(network, updatedPsseModel);
        FixedShuntCompensatorConverter.updateFixedShunts(network, updatedPsseModel);
        GeneratorConverter.updateGenerators(network, updatedPsseModel);
        LineConverter.updateLines(network, updatedPsseModel);
        TransformerConverter.updateTransformers(network, updatedPsseModel);
        TwoTerminalDcConverter.updateTwoTerminalDcTransmissionLines(network, updatedPsseModel);
        VscDcTransmissionLineConverter.updateVscDcTransmissionLines(network, updatedPsseModel);
        FactsDeviceConverter.updateFactsDevices(network, updatedPsseModel);
        SwitchedShuntCompensatorConverter.updateSwitchedShunts(network, updatedPsseModel);
    }

    private static PssePowerFlowModel createPsseModel(Network network) {
        PerUnitContext perUnitContext = new PerUnitContext(BASE_MVA);
        PsseCaseIdentification caseIdentification = createCaseIdentification(network, perUnitContext);
        PssePowerFlowModel psseModel = new PssePowerFlowModel(caseIdentification);
        ContextExport contextExport = createContextExport(network, psseModel, true);

        VoltageLevelConverter.createSubstations(network, psseModel, contextExport);

        BusConverter.createBuses(psseModel, contextExport);
        LoadConverter.createLoads(network, psseModel, contextExport);
        FixedShuntCompensatorConverter.createFixedShunts(network, psseModel, contextExport);
        GeneratorConverter.createGenerators(network, psseModel, contextExport, perUnitContext);
        LineConverter.createLines(network, psseModel, contextExport, perUnitContext);
        TransformerConverter.createTransformers(network, psseModel, contextExport, perUnitContext);
        TwoTerminalDcConverter.createTwoTerminalDcTransmissionLines(network, psseModel, contextExport);
        VscDcTransmissionLineConverter.createVscDcTransmissionLines(network, psseModel, contextExport);
        FactsDeviceConverter.createFactsDevices(network, psseModel, contextExport);
        SwitchedShuntCompensatorConverter.createSwitchedShunts(network, psseModel, contextExport);

        BatteryConverter.createBatteries(network, psseModel, contextExport);
        TieLineConverter.createTieLines(network, psseModel, contextExport, perUnitContext);
        DanglingLineConverter.createDanglingLines(network, psseModel, contextExport, perUnitContext);
        return psseModel;
    }

    private static PsseCaseIdentification createCaseIdentification(Network network, PerUnitContext perUnitContext) {
        PsseCaseIdentification caseIdentification = new PsseCaseIdentification();
        caseIdentification.setIc(0);
        caseIdentification.setSbase(perUnitContext.sBase);
        caseIdentification.setRev(35);
        caseIdentification.setXfrrat(0.0);
        caseIdentification.setNxfrat(0.0);
        caseIdentification.setBasfrq(50.0);
        String caseDate = network.getCaseDate().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String caseName = network.getNameOrId();
        caseIdentification.setTitle1(String.format("%s %s", caseDate, caseName));
        caseIdentification.setTitle2("");

        return caseIdentification;
    }

    record PerUnitContext(double sBase) {
    }
}
