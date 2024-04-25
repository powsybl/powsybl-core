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
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.io.PowerFlowRawData32;
import com.powsybl.psse.model.pf.io.PowerFlowRawData33;
import com.powsybl.psse.model.pf.io.PowerFlowRawData35;
import com.powsybl.psse.model.pf.io.PowerFlowRawxData35;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

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

        PssePowerFlowModel psseModel = network.getExtension(PsseModelExtension.class).getPsseModel();
        PssePowerFlowModel updatePsseModel = createUpdatePsseModel(network, psseModel);

        Context context = network.getExtension(PsseConversionContextExtension.class).getContext();
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
            case V35:
                PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
                try {
                    rawData35.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                break;
            case V33:
                PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
                try {
                    rawData33.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                break;
            case V32:
                PowerFlowRawData32 rawData32 = new PowerFlowRawData32();
                try {
                    rawData32.write(updatePsseModel, context, dataSource);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                break;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    private static PssePowerFlowModel createUpdatePsseModel(Network network, PssePowerFlowModel psseModel) {
        PssePowerFlowModel updatePsseModel = new PssePowerFlowModel(psseModel.getCaseIdentification());

        copyPermanentBlocks(psseModel, updatePsseModel);
        updateModifiedBlocks(network, psseModel, updatePsseModel);
        return updatePsseModel;
    }

    private static void copyPermanentBlocks(PssePowerFlowModel psseModel, PssePowerFlowModel updatePsseModel) {
        updatePsseModel.addAreas(psseModel.getAreas());
        updatePsseModel.addTwoTerminalDcTransmissionLines(psseModel.getTwoTerminalDcTransmissionLines());
        updatePsseModel.addVoltageSourceConverterDcTransmissionLines(psseModel.getVoltageSourceConverterDcTransmissionLines());
        updatePsseModel.addTransformerImpedanceCorrections(psseModel.getTransformerImpedanceCorrections());
        updatePsseModel.addMultiTerminalDcTransmissionLines(psseModel.getMultiTerminalDcTransmissionLines());
        updatePsseModel.addLineGrouping(psseModel.getLineGrouping());
        updatePsseModel.addZones(psseModel.getZones());
        updatePsseModel.addInterareaTransfer(psseModel.getInterareaTransfer());
        updatePsseModel.addOwners(psseModel.getOwners());
        updatePsseModel.addFacts(psseModel.getFacts());
        updatePsseModel.addGneDevice(psseModel.getGneDevice());
        updatePsseModel.addInductionMachines(psseModel.getInductionMachines());
    }

    private static void updateModifiedBlocks(Network network, PssePowerFlowModel psseModel, PssePowerFlowModel updatePsseModel) {
        BusConverter.updateBuses(network, psseModel, updatePsseModel);
        LoadConverter.updateLoads(network, psseModel, updatePsseModel);
        FixedShuntCompensatorConverter.updateFixedShunts(network, psseModel, updatePsseModel);
        GeneratorConverter.updateGenerators(network, psseModel, updatePsseModel);
        LineConverter.updateLines(network, psseModel, updatePsseModel);
        TransformerConverter.updateTransformers(network, psseModel, updatePsseModel);
        SwitchedShuntCompensatorConverter.updateSwitchedShunts(network, psseModel, updatePsseModel);
    }
}
