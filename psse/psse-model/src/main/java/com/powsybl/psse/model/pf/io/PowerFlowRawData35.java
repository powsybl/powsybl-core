/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.writeBegin;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.writeEmpty;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.writeEnd;
import static com.powsybl.psse.model.io.RecordGroupIOLegacyText.writeQ;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.BUS;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.SYSTEM_SWITCHING_DEVICE;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.SYSTEM_WIDE;
import static com.powsybl.psse.model.pf.io.PsseDataClass.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PowerFlowRawData35 extends AbstractPowerFlowRawDataVersioned {

    // Classes in the order they are written in the file
    private static final List<PsseDataClass> PSSE_CLASSES_BUS_TO_NON_TRANSFORMER_BRANCH = List.of(
        BUS_DATA, LOAD_DATA, FIXED_BUS_SHUNT_DATA, GENERATOR_DATA, NON_TRANSFORMER_BRANCH_DATA);
    private static final List<PsseDataClass> PSSE_CLASSES_FROM_TRANSFORMER_BRANCH = List.of(
        TRANSFORMER_DATA, AREA_INTERCHANGE_DATA, TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA,
        VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, TRANSFORMER_IMPEDANCE_CORRECTION_TABLES_DATA,
        MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, MULTI_SECTION_LINE_GROUPING_DATA, ZONE_DATA, INTERAREA_TRANSFER_DATA,
        OWNER_DATA, FACTS_DEVICE_DATA, SWITCHED_SHUNT_DATA, GNE_DEVICE_DATA, INDUCTION_MACHINE_DATA,
        SUBSTATION_DATA);

    @Override
    void read(PssePowerFlowModel model, Context context, LegacyTextReader reader) throws IOException {
        reader.skip(SYSTEM_WIDE);
        readPsseData(model, context, reader, PSSE_CLASSES_BUS_TO_NON_TRANSFORMER_BRANCH);

        reader.skip(SYSTEM_SWITCHING_DEVICE);
        readPsseData(model, context, reader, PSSE_CLASSES_FROM_TRANSFORMER_BRANCH);
    }

    @Override
    void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) {

        new CaseIdentificationData().writeHead(model.getCaseIdentification(), context, outputStream);

        // Record group comments:
        // System wide data does not have start comment
        // We only write and empty record group end
        writeEnd("SYSTEM-WIDE", outputStream);
        // Until v33 Bus data did not write its start
        // From v35 we need the start
        RecordGroupIOLegacyText.write(", ", outputStream);
        writeBegin(BUS.getLegacyTextName(), outputStream);

        writePsseData(model, context, outputStream, PSSE_CLASSES_BUS_TO_NON_TRANSFORMER_BRANCH);

        writeEmpty(SYSTEM_SWITCHING_DEVICE, outputStream);
        writePsseData(model, context, outputStream, PSSE_CLASSES_FROM_TRANSFORMER_BRANCH);

        writeQ(outputStream);
    }

    @Override
    PsseVersion.Major getVersion() {
        return V35;
    }
}
