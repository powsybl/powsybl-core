/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.data.AbstractRecordGroup.PsseRecordGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawData35 extends RawData33 {

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(reader, context);
            caseIdentification.validate();

            PsseRawModel model = new PsseRawModel(caseIdentification);
            // TODO Complete discarded record groups
            Util.readDiscardedRecordGroup(PsseRecordGroup.SYSTEM_WIDE_DATA, reader);
            model.addBuses(new BusData().read(reader, context));
            model.addLoads(new LoadData().read(reader, context));
            model.addFixedShunts(new FixedBusShuntData().read(reader, context));
            model.addGenerators(new GeneratorData().read(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.SYSTEM_SWITCHING_DEVICE_DATA, reader);
            model.addTransformers(new TransformerData().read(reader, context));
            model.addAreas(new AreaInterchangeData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.MULTI_SECTION_LINE_GROUPING_DATA, reader);
            model.addZones(new ZoneData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.INTERAREA_TRANSFER_DATA, reader);
            model.addOwners(new OwnerData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.FACTS_DEVICE_DATA, reader);
            model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
            Util.readDiscardedRecordGroup(PsseRecordGroup.GNE_DEVICE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.INDUCTION_MACHINE_DATA, reader);
            Util.readDiscardedRecordGroup(PsseRecordGroup.SUBSTATION_DATA, reader);

            return model;
        }
    }
}
