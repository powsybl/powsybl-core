/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.PsseVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * PSSE RAW data common to all versions
 * Should be able to read the case identification to obtain the version (case identification "rev" attribute)
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PowerFlowRawDataAllVersions implements PowerFlowData {

    @Override
    public boolean isValidFile(ReadOnlyDataSource dataSource, String extension) throws IOException {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, extension)))) {
            LegacyTextReader reader = new LegacyTextReader(bReader);
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, new Context());
            caseIdentification.validate();
            return true;
        }
    }

    @Override
    public PsseVersion readVersion(ReadOnlyDataSource dataSource, String extension) throws IOException {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, extension)))) {
            LegacyTextReader reader = new LegacyTextReader(bReader);
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, new Context());
            caseIdentification.validate();
            return PsseVersion.fromRevision(caseIdentification.getRev());
        }
    }

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        throw new PsseException("Here we don't know how to read a complete data file. Specific version instance is required");
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        throw new PsseException("Here we don't know how to write a complete data file. Specific version instance is required");
    }
}
