/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CaseIdentificationData extends AbstractRecordGroup<PsseCaseIdentification> {

    private static final String[] FIELD_NAMES = {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};

    CaseIdentificationData() {
        super(PsseRecordGroup.CASE_IDENTIFICATION_DATA);
    }

    public PsseCaseIdentification read1(BufferedReader reader, PsseContext context) throws IOException {
        String line = Util.readLineAndRemoveComment(reader);
        context.setDelimiter(Util.detectDelimiter(line));

        String[] headers = fieldNames(context.getVersion());
        PsseCaseIdentification caseIdentification = parseSingleRecord(line, headers, context);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setFieldNames(getRecordGroup(), headers);
        context.setVersion(PsseVersion.fromNumber(caseIdentification.getRev()));
        return caseIdentification;
    }

    PsseCaseIdentification read1(JsonNode networkNode, PsseContext context) {
        context.setDelimiter(",");
        PsseCaseIdentification caseIdentification = super.read(networkNode, context).get(0);
        context.setVersion(PsseVersion.fromNumber(caseIdentification.getRev()));
        return caseIdentification;
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        return FIELD_NAMES;
    }

    @Override
    public Class<? extends PsseCaseIdentification> psseTypeClass(PsseVersion version) {
        return PsseCaseIdentification.class;
    }
}
