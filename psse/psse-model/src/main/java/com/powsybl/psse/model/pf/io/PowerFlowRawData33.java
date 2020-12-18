/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.*;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PowerFlowRawData33 extends PowerFlowRawDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, context);
            caseIdentification.validate();

            PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);
            model.addBuses(new BusData().read(reader, context));
            model.addLoads(new LoadData().read(reader, context));
            model.addFixedShunts(new FixedBusShuntData().read(reader, context));
            model.addGenerators(new GeneratorData().read(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));
            model.addTransformers(new TransformerData().read(reader, context));
            model.addAreas(new AreaInterchangeData().read(reader, context));
            // TODO complete discarded record groups
            skip(TWO_TERMINAL_DC_TRANSMISSION_LINE, reader);
            skip(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, reader);
            skip(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, reader);
            skip(MULTI_SECTION_LINE_GROUPING, reader);
            skip(MULTI_SECTION_LINE_GROUPING, reader);
            model.addZones(new ZoneData().read(reader, context));
            skip(INTERAREA_TRANSFER, reader);
            model.addOwners(new OwnerData().read(reader, context));
            skip(FACTS_CONTROL_DEVICE, reader);
            model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
            skip(GNE_DEVICE, reader);
            skip(INDUCTION_MACHINE, reader);

            return model;
        }
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion().major() != V33) {
            throw new PsseException("Unexpected version " + context.getVersion().getMajorNumber());
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "raw", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) {

        new CaseIdentificationData().writeHead(model.getCaseIdentification(), context, outputStream);

        new BusData().write(model.getBuses(), context, outputStream);
        new LoadData().write(model.getLoads(), context, outputStream);
        new FixedBusShuntData().write(model.getFixedShunts(), context, outputStream);
        new GeneratorData().write(model.getGenerators(), context, outputStream);
        new NonTransformerBranchData().write(model.getNonTransformerBranches(), context, outputStream);
        new TransformerData().write(model.getTransformers(), context, outputStream);
        new AreaInterchangeData().write(model.getAreas(), context, outputStream);

        writeEmpty(TWO_TERMINAL_DC_TRANSMISSION_LINE, outputStream);
        writeEmpty(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, outputStream);
        writeEmpty(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, outputStream);
        writeEmpty(MULTI_TERMINAL_DC_TRANSMISSION_LINE, outputStream);
        writeEmpty(MULTI_SECTION_LINE_GROUPING, outputStream);

        new ZoneData().write(model.getZones(), context, outputStream);
        writeEmpty(INTERAREA_TRANSFER, outputStream);
        new OwnerData().write(model.getOwners(), context, outputStream);

        writeEmpty(FACTS_CONTROL_DEVICE, outputStream);
        writeEmpty(SWITCHED_SHUNT, outputStream);
        writeEmpty(GNE_DEVICE, outputStream);
        writeEmpty(INDUCTION_MACHINE, outputStream);

        writeQ(outputStream);
    }
}
