/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;
import static com.powsybl.psse.model.io.AbstractRecordGroup.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PowerFlowRawData35 extends PowerFlowRawDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(reader, context);
            caseIdentification.validate();

            PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);
            // TODO Complete discarded record groups
            readLegacyTextAndIgnore(SYSTEM_WIDE, reader);
            model.addBuses(new BusData().readLegacyText(reader, context));
            model.addLoads(new LoadData().readLegacyText(reader, context));
            model.addFixedShunts(new FixedBusShuntData().readLegacyText(reader, context));
            model.addGenerators(new GeneratorData().readLegacyText(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().readLegacyText(reader, context));
            readLegacyTextAndIgnore(SYSTEM_SWITCHING_DEVICE, reader);
            model.addTransformers(new TransformerData().readLegacyText(reader, context));
            model.addAreas(new AreaInterchangeData().readLegacyText(reader, context));
            readLegacyTextAndIgnore(TWO_TERMINAL_DC_TRANSMISSION_LINE, reader);
            readLegacyTextAndIgnore(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, reader);
            readLegacyTextAndIgnore(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, reader);
            readLegacyTextAndIgnore(MULTI_TERMINAL_DC_TRANSMISSION_LINE, reader);
            readLegacyTextAndIgnore(MULTI_SECTION_LINE_GROUPING, reader);
            model.addZones(new ZoneData().readLegacyText(reader, context));
            readLegacyTextAndIgnore(INTERAREA_TRANSFER, reader);
            model.addOwners(new OwnerData().readLegacyText(reader, context));
            readLegacyTextAndIgnore(FACTS_CONTROL_DEVICE, reader);
            model.addSwitchedShunts(new SwitchedShuntData().readLegacyText(reader, context));
            readLegacyTextAndIgnore(GNE_DEVICE, reader);
            readLegacyTextAndIgnore(INDUCTION_MACHINE, reader);
            readLegacyTextAndIgnore(SUBSTATION, reader);

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

    private void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) throws IOException {
        new CaseIdentificationData().write1(model, context, outputStream);

        // Record group comments:
        // System wide data does not have start comment
        // We only write and empty record group end
        writeEnd("SYSTEM-WIDE", outputStream);
        // Until v33 Bus data did not write its start
        // From v35 we need the start
        AbstractRecordGroup.writeLegacyText(", ", outputStream);
        AbstractRecordGroup.writeBegin(BUS.getLegacyTextName(), outputStream);

        new BusData().writeLegacyText(model.getBuses(), context, outputStream);
        new LoadData().writeLegacyText(model.getLoads(), context, outputStream);
        new FixedBusShuntData().writeLegacyText(model.getFixedShunts(), context, outputStream);
        new GeneratorData().writeLegacyText(model.getGenerators(), context, outputStream);
        new NonTransformerBranchData().writeLegacyText(model.getNonTransformerBranches(), context, outputStream);
        writeEmpty(SYSTEM_SWITCHING_DEVICE, outputStream);
        new TransformerData().writeLegacyText(model.getTransformers(), context, outputStream);
        new AreaInterchangeData().writeLegacyText(model.getAreas(), context, outputStream);

        writeEmpty(TWO_TERMINAL_DC_TRANSMISSION_LINE, outputStream);
        writeEmpty(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, outputStream);
        writeEmpty(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, outputStream);
        writeEmpty(MULTI_TERMINAL_DC_TRANSMISSION_LINE, outputStream);

        writeEmpty(MULTI_SECTION_LINE_GROUPING, outputStream);

        new ZoneData().writeLegacyText(model.getZones(), context, outputStream);
        writeEmpty(INTERAREA_TRANSFER, outputStream);
        new OwnerData().writeLegacyText(model.getOwners(), context, outputStream);

        writeEmpty(FACTS_CONTROL_DEVICE, outputStream);
        writeEmpty(SWITCHED_SHUNT, outputStream);
        writeEmpty(GNE_DEVICE, outputStream);
        writeEmpty(INDUCTION_MACHINE, outputStream);

        writeEmpty(SUBSTATION, outputStream);

        AbstractRecordGroup.writeQ(outputStream);
    }
}
