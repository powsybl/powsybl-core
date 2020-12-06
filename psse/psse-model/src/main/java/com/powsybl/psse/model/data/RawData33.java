/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.data.AbstractRecordGroup.PsseRecordGroup;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawData33 extends RawDataCommon {

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(reader, context);
            caseIdentification.validate();

            PsseRawModel model = new PsseRawModel(caseIdentification);
            model.addBuses(new BusData().read(reader, context));
            model.addLoads(new LoadData().read(reader, context));
            model.addFixedShunts(new FixedBusShuntData().read(reader, context));
            model.addGenerators(new GeneratorData().read(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));
            model.addTransformers(new TransformerData().read(reader, context));
            model.addAreas(new AreaInterchangeData().read(reader, context));
            // TODO complete discarded record groups
            Util.readDiscardedRecordGroup(PsseRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.MULTI_SECTION_LINE_GROUPING_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.MULTI_SECTION_LINE_GROUPING_DATA, reader);
            model.addZones(new ZoneData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.INTERAREA_TRANSFER_DATA, reader);
            model.addOwners(new OwnerData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.FACTS_DEVICE_DATA, reader);
            model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.GNE_DEVICE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.INDUCTION_MACHINE_DATA, reader);

            return model;
        }
    }

    @Override
    public void write(PsseRawModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);

        if (context.getVersion() != PsseVersion.VERSION_33) {
            throw new PsseException("Unexpected version " + context.getVersion());
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "raw", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PsseRawModel model, Context context, BufferedOutputStream outputStream) throws IOException {

        new CaseIdentificationData().write1(model, context, outputStream);
        new BusData().write(model, context, outputStream);
        new LoadData().write(model, context, outputStream);
        new FixedBusShuntData().write(model, context, outputStream);
        new GeneratorData().write(model, context, outputStream);
        new NonTransformerBranchData().write(model, context, outputStream);

        new TransformerData().write(model, context, outputStream);
        new AreaInterchangeData().write(model, context, outputStream);

        Util.writeEndOfBlockAndComment("END OF TWO-TERMINAL DC DATA, BEGIN VOLTAGE SOURCE CONVERTER DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF VOLTAGE SOURCE CONVERTER DATA, BEGIN IMPEDANCE CORRECTION DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF IMPEDANCE CORRECTION DATA, BEGIN MULTI-TERMINAL DC DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF MULTI-TERMINAL DC DATA, BEGIN MULTI-SECTION LINE DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF MULTI-SECTION LINE DATA, BEGIN ZONE DATA", outputStream);

        new ZoneData().write(model, context, outputStream);
        Util.writeEndOfBlockAndComment("END OF INTER-AREA TRANSFER DATA, BEGIN OWNER DATA", outputStream);
        new OwnerData().write(model, context, outputStream);

        Util.writeEndOfBlockAndComment("END OF FACTS CONTROL DEVICE DATA, BEGIN SWITCHED SHUNT DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF SWITCHED SHUNT DATA, BEGIN GNE DEVICE DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF GNE DEVICE DATA, BEGIN INDUCTION MACHINE DATA", outputStream);
        Util.writeEndOfBlockAndComment("END OF INDUCTION MACHINE DATA", outputStream);
        Util.writeQrecord(outputStream);

        outputStream.close();
    }
}
