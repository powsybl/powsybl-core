/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
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
        return Collections.unmodifiableList(STATIC_PARAMETERS);
    }

    private static final List<Parameter> STATIC_PARAMETERS = ImmutableList.of();

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
        if (context.getFileFormat() == FileFormat.JSON) {
            PsseVersion version = PsseVersion.fromRevision(updatePsseModel.getCaseIdentification().getRev());
            switch (version.major()) {
                case V35:
                    PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
                    try {
                        rawXData35.write(updatePsseModel, context, dataSource);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    break;
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        } else {
            PsseVersion version = PsseVersion.fromRevision(updatePsseModel.getCaseIdentification().getRev());
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
        NodeBreakerExport nodeBreakerExport = VoltageLevelConverter.mapSubstations(network, updatedPsseModel);

        // Only after mapping all substations, the substation data can be updated
        VoltageLevelConverter.updateSubstations(network, updatedPsseModel, nodeBreakerExport);

        BusConverter.updateBuses(network, updatedPsseModel, nodeBreakerExport);
        LoadConverter.updateLoads(network, updatedPsseModel, nodeBreakerExport);
        FixedShuntCompensatorConverter.updateFixedShunts(network, updatedPsseModel, nodeBreakerExport);
        GeneratorConverter.updateGenerators(network, updatedPsseModel, nodeBreakerExport);
        LineConverter.updateLines(network, updatedPsseModel, nodeBreakerExport);
        TransformerConverter.updateTransformers(network, updatedPsseModel, nodeBreakerExport);
        TwoTerminalDcConverter.updateTwoTerminalDcTransmissionLines(network, updatedPsseModel, nodeBreakerExport);
        SwitchedShuntCompensatorConverter.updateSwitchedShunts(network, updatedPsseModel, nodeBreakerExport);
    }
}
