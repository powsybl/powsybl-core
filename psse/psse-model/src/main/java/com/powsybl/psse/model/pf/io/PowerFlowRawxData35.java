/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.io.FileFormat.JSON;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PowerFlowRawxData35 extends PowerFlowRawxDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        context.setFileFormat(JSON);
        try (InputStream is = dataSource.newInputStream(null, ext)) {
            return read(is, context);
        }
    }

    private PssePowerFlowModel read(InputStream stream, Context context) throws IOException {
        JsonNode networkNode = networkNode(stream);
        context.setNetworkNode(networkNode);
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(null, context);
        caseIdentification.validate();

        PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);

        // Call read with inputStream as null will force reading from context.networkNode
        model.addBuses(new BusData().read(null, context));
        model.addLoads(new LoadData().read(null, context));
        model.addFixedShunts(new FixedBusShuntData().read(null, context));
        model.addGenerators(new GeneratorData().read(null, context));
        model.addNonTransformerBranches(new NonTransformerBranchData().read(null, context));
        model.addTransformers(new TransformerData().read(null, context));

        model.addAreas(new AreaInterchangeData().read(null, context));
        model.addTwoTerminalDcTransmissionLines(new TwoTerminalDcTransmissionLineData().read(null, context));
        model.addVoltageSourceConverterDcTransmissionLines(new VoltageSourceConverterDcTransmissionLineData().read(null, context));
        model.addTransformerImpedanceCorrections(new TransformerImpedanceCorrectionTablesData().read(null, context));
        model.addMultiTerminalDcTransmissionLines(new MultiTerminalDcTransmissionLineData().read(null, context));

        model.addLineGrouping(new MultiSectionLineGroupingData().read(null, context));
        model.addZones(new ZoneData().read(null, context));
        model.addInterareaTransfer(new InterareaTransferData().read(null, context));
        model.addOwners(new OwnerData().read(null, context));

        model.addFacts(new FactsDeviceData().read(null, context));
        model.addSwitchedShunts(new SwitchedShuntData().read(null, context));
        model.addGneDevice(new GneDeviceData().read(null, context));
        model.addInductionMachines(new InductionMachineData().read(null, context));

        return model;
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion().major() != V35) {
            throw new PsseException("Unexpected version " + context.getVersion().getMajorNumber());
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "rawx", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) throws IOException {
        try (JsonGenerator generator = new JsonFactory().createGenerator(outputStream).setPrettyPrinter(new DefaultPrettyPrinter())) {
            context.setJsonGenerator(generator);
            generator.writeStartObject();
            generator.writeFieldName("network");
            generator.writeStartObject();
            generator.flush();

            new CaseIdentificationData().writeHead(model.getCaseIdentification(), context, null);
            new BusData().write(model.getBuses(), context, null);
            new LoadData().write(model.getLoads(), context, null);
            new FixedBusShuntData().write(model.getFixedShunts(), context, null);
            new GeneratorData().write(model.getGenerators(), context, null);
            new NonTransformerBranchData().write(model.getNonTransformerBranches(), context, null);
            new TransformerData().write(model.getTransformers(), context, null);
            new AreaInterchangeData().write(model.getAreas(), context, null);
            new TwoTerminalDcTransmissionLineData().write(model.getTwoTerminalDcTransmissionLines(), context, null);
            new VoltageSourceConverterDcTransmissionLineData().write(model.getVoltageSourceConverterDcTransmissionLines(), context, null);
            new TransformerImpedanceCorrectionTablesData().write(model.getTransformerImpedanceCorrections(), context, null);
            new MultiTerminalDcTransmissionLineData().write(model.getMultiTerminalDcTransmissionLines(), context, null);

            new MultiSectionLineGroupingData().write(model.getLineGrouping(), context, null);
            new ZoneData().write(model.getZones(), context, null);
            new InterareaTransferData().write(model.getInterareaTransfer(), context, null);
            new OwnerData().write(model.getOwners(), context, null);
            new FactsDeviceData().write(model.getFacts(), context, null);
            new SwitchedShuntData().write(model.getSwitchedShunts(), context, null);

            new GneDeviceData().write(model.getGneDevice(), context, null);
            new InductionMachineData().write(model.getInductionMachines(), context, null);

            generator.writeEndObject(); // network
            generator.writeEndObject(); // root
            generator.flush();
        }
    }
}
