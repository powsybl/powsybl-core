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

            PssePowerFlowModel model = readCaseIdentification(reader, context);
            readBlocksA(model, reader, context);
            readBlocksB(model, reader, context);
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

        writeCaseIdentification(model, context, outputStream);
        writeBlocksA(model, context, outputStream);
        writeBlocksB(model, context, outputStream);
        writeEmpty(INDUCTION_MACHINE, outputStream);
        writeQ(outputStream);
    }
}
