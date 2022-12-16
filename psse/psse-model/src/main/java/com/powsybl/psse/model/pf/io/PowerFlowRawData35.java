/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.*;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PowerFlowRawData35 extends PowerFlowRawDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext), StandardCharsets.UTF_8))) {

            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, context);
            caseIdentification.validate();
            PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);

            skip(SYSTEM_WIDE, reader);
            model.addBuses(new BusData().read(reader, context));
            model.addLoads(new LoadData().read(reader, context));
            model.addFixedShunts(new FixedBusShuntData().read(reader, context));
            model.addGenerators(new GeneratorData().read(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));

            skip(SYSTEM_SWITCHING_DEVICE, reader);
            model.addTransformers(new TransformerData().read(reader, context));
            model.addAreas(new AreaInterchangeData().read(reader, context));

            model.addTwoTerminalDcTransmissionLines(new TwoTerminalDcTransmissionLineData().read(reader, context));
            model.addVoltageSourceConverterDcTransmissionLines(new VoltageSourceConverterDcTransmissionLineData().read(reader, context));
            model.addTransformerImpedanceCorrections(new TransformerImpedanceCorrectionTablesData().read(reader, context));
            model.addMultiTerminalDcTransmissionLines(new MultiTerminalDcTransmissionLineData().read(reader, context));

            model.addLineGrouping(new MultiSectionLineGroupingData().read(reader, context));
            model.addZones(new ZoneData().read(reader, context));
            model.addInterareaTransfer(new InterareaTransferData().read(reader, context));
            model.addOwners(new OwnerData().read(reader, context));
            model.addFacts(new FactsDeviceData().read(reader, context));
            model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
            model.addGneDevice(new GneDeviceData().read(reader, context));
            model.addInductionMachines(new InductionMachineData().read(reader, context));
            skip(SUBSTATION, reader);

            return model;
        }
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion().major() != V35) {
            throw new PsseException("Unexpected version " + context.getVersion().getMajorNumber());
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "raw", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) {

        new CaseIdentificationData().writeHead(model.getCaseIdentification(), context, outputStream);

        // Record group comments:
        // System wide data does not have start comment
        // We only write and empty record group end
        writeEnd("SYSTEM-WIDE", outputStream);
        // Until v33 Bus data did not write its start
        // From v35 we need the start
        RecordGroupIOLegacyText.write(", ", outputStream);
        writeBegin(BUS.getLegacyTextName(), outputStream);

        new BusData().write(model.getBuses(), context, outputStream);
        new LoadData().write(model.getLoads(), context, outputStream);
        new FixedBusShuntData().write(model.getFixedShunts(), context, outputStream);
        new GeneratorData().write(model.getGenerators(), context, outputStream);
        new NonTransformerBranchData().write(model.getNonTransformerBranches(), context, outputStream);

        writeEmpty(SYSTEM_SWITCHING_DEVICE, outputStream);
        new TransformerData().write(model.getTransformers(), context, outputStream);
        new AreaInterchangeData().write(model.getAreas(), context, outputStream);

        new TwoTerminalDcTransmissionLineData().write(model.getTwoTerminalDcTransmissionLines(), context, outputStream);
        new VoltageSourceConverterDcTransmissionLineData().write(model.getVoltageSourceConverterDcTransmissionLines(), context, outputStream);
        new TransformerImpedanceCorrectionTablesData().write(model.getTransformerImpedanceCorrections(), context, outputStream);
        new MultiTerminalDcTransmissionLineData().write(model.getMultiTerminalDcTransmissionLines(), context, outputStream);

        new MultiSectionLineGroupingData().write(model.getLineGrouping(), context, outputStream);

        new ZoneData().write(model.getZones(), context, outputStream);
        new InterareaTransferData().write(model.getInterareaTransfer(), context, outputStream);
        new OwnerData().write(model.getOwners(), context, outputStream);

        new FactsDeviceData().write(model.getFacts(), context, outputStream);
        new SwitchedShuntData().write(model.getSwitchedShunts(), context, outputStream);
        new GneDeviceData().write(model.getGneDevice(), context, outputStream);
        new InductionMachineData().write(model.getInductionMachines(), context, outputStream);

        writeEmpty(SUBSTATION, outputStream);

        writeQ(outputStream);
    }
}
