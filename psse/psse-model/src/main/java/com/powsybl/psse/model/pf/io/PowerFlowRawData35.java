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
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {

            PssePowerFlowModel model = readCaseIdentification(reader, context);
            skip(SYSTEM_WIDE, reader);
            readBlocksA(model, reader, context);
            skip(SYSTEM_SWITCHING_DEVICE, reader);
            readBlocksB(model, reader, context);
            skip(INDUCTION_MACHINE, reader);
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
        writeCaseIdentification(model, context, outputStream);

        // Record group comments:
        // System wide data does not have start comment
        // We only write and empty record group end
        writeEnd("SYSTEM-WIDE", outputStream);
        // Until v33 Bus data did not write its start
        // From v35 we need the start
        RecordGroupIOLegacyText.write(", ", outputStream);
        writeBegin(BUS.getLegacyTextName(), outputStream);

        writeBlocksA(model, context, outputStream);
        writeEmpty(SYSTEM_SWITCHING_DEVICE, outputStream);
        writeBlocksB(model, context, outputStream);
        writeEmpty(INDUCTION_MACHINE, outputStream);
        writeEmpty(SUBSTATION, outputStream);

        writeQ(outputStream);
    }
}
