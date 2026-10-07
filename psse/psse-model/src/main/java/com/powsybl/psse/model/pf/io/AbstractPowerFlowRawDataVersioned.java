/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractPowerFlowRawDataVersioned extends PowerFlowRawDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext), StandardCharsets.UTF_8))) {

            LegacyTextReader reader = new LegacyTextReader(bReader);

            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, context);
            caseIdentification.validate();
            PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);

            read(model, context, reader);

            return model;
        }
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion().major() != getVersion()) {
            throw new PsseException("Unexpected version " + context.getVersion().getMajorNumber());
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "raw", false))) {
            write(model, context, outputStream);
        }
    }

    abstract PsseVersion.Major getVersion();

    abstract void read(PssePowerFlowModel model, Context context, LegacyTextReader reader) throws IOException;

    abstract void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream);
}
