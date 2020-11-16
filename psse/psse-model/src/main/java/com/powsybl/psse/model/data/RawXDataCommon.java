/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * PSSE raw data common to all versions
 * Should be able to read the case identification to obtain the version (case identification "rev" attribute)
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawXDataCommon implements RawData {

    @Override
    public boolean isValidFile(ReadOnlyDataSource dataSource, String ext) throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode(jsonFile), new Context());
        caseIdentification.validate();
        return true;
    }

    @Override
    public PsseVersion readVersion(ReadOnlyDataSource dataSource, String ext) throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode(jsonFile), new Context());
        return PsseVersion.fromNumber(caseIdentification.getRev());
    }

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        throw new PsseException("RawXDataCommon does not know how to read complete data file. Specific version instance is required");
    }

    protected JsonNode networkNode(String jsonFile) throws IOException {
        return new ObjectMapper().readTree(jsonFile).get("network");
    }
}
