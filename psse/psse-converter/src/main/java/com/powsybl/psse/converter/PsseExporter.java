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
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.psse.converter.extensions.PsseConversionContextExtension;
import com.powsybl.psse.converter.extensions.PsseModelExtension;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseSubstation;
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

    private static final String FORMAT = "PSS/E";

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
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
        if (network.getExtension(PsseModelExtension.class) == null) {
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
        ContextExport contextExport = createContextExport(network, updatedPsseModel);

        VoltageLevelConverter.createAndUpdateSubstations(updatedPsseModel, contextExport);

        BusConverter.updateAndCreateBuses(network, updatedPsseModel, contextExport);
        LoadConverter.updateAndCreateLoads(network, updatedPsseModel, contextExport);
        FixedShuntCompensatorConverter.updateFixedShunts(network, updatedPsseModel, contextExport);
        GeneratorConverter.updateGenerators(network, updatedPsseModel, contextExport);
        LineConverter.updateLines(network, updatedPsseModel, contextExport);
        TransformerConverter.updateTransformers(network, updatedPsseModel, contextExport);
        TwoTerminalDcConverter.updateTwoTerminalDcTransmissionLines(network, updatedPsseModel, contextExport);
        SwitchedShuntCompensatorConverter.updateSwitchedShunts(network, updatedPsseModel, contextExport);
    }

    private static PssePowerFlowModel createPsseModel(Network network) {
        PsseCaseIdentification caseIdentification = createCaseIdentification(network);
        PssePowerFlowModel psseModel = new PssePowerFlowModel(caseIdentification);
        ContextExport contextExport = createContextExport(network, psseModel);

        VoltageLevelConverter.createAndUpdateSubstations(psseModel, contextExport);

        BusConverter.updateAndCreateBuses(network, psseModel, contextExport);
        LoadConverter.updateAndCreateLoads(network, psseModel, contextExport);
        return psseModel;
    }

    private static PsseCaseIdentification createCaseIdentification(Network network) {
        PsseCaseIdentification caseIdentification = new PsseCaseIdentification();
        caseIdentification.setIc(0);
        caseIdentification.setSbase(100.0);
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

    private static ContextExport createContextExport(Network network, PssePowerFlowModel psseModel) {
        int maxPsseBus = psseModel.getBuses().stream().map(PsseBus::getI).max(Comparator.naturalOrder()).orElse(0);
        int maxPsseSubstationIs = psseModel.getSubstations().stream().map(PsseSubstation::getIs).max(Comparator.naturalOrder()).orElse(0);
        ContextExport contextExport = new ContextExport(maxPsseBus, maxPsseSubstationIs);

        // First busBreaker voltageLevels
        List<VoltageLevel> busBreakerVoltageLevels = network.getVoltageLevelStream().filter(vl -> vl.getTopologyKind().equals(TopologyKind.BUS_BREAKER)).toList();
        busBreakerVoltageLevels.forEach(voltageLevel -> createBusBreakerExport(voltageLevel, contextExport));

        // Always after busBreaker voltageLevels
        findNodeBreakerVoltageLevelsAndMapPsseSubstation(network, psseModel, contextExport);
        contextExport.getNodeBreakerExport().getVoltageLevels().forEach(voltageLevel -> {
            Optional<PsseSubstation> psseSubstation = contextExport.getNodeBreakerExport().getPsseSubstationMappedToVoltageLevel(voltageLevel);
            if (psseSubstation.isPresent()) {
                createNodeBreakerExport(voltageLevel, psseSubstation.get(), contextExport);
            } else {
                createNodeBreakerExport(voltageLevel, contextExport);
            }
        });

        return contextExport;
    }
}
