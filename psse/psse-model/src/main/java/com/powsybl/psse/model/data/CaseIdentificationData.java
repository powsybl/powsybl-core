/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.ArrayData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CaseIdentificationData extends AbstractRecordGroup<PsseCaseIdentification> {

    private static final String[] QUOTED_FIELDS = {"title1", "title2"};
    private static final String[] EXCLUDED_FIELDS = {"title1", "title2"};

    CaseIdentificationData() {
        super(PsseRecordGroup.CASE_IDENTIFICATION, "ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2");
    }

    PsseCaseIdentification read1(BufferedReader reader, Context context) throws IOException {
        String line = Util.readLineAndRemoveComment(reader);
        context.detectDelimiter(line);

        String[] headers = fieldNames(context.getVersion());
        PsseCaseIdentification caseIdentification = parseSingleRecord(line, headers, context);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setFieldNames(getRecordGroup(), headers);
        context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
        context.setRawx(false);
        return caseIdentification;
    }

    void write1(PsseRawModel model, Context context, OutputStream outputStream) {
        String[] headers = context.getFieldNames(getRecordGroup());
        headers = Util.excludeFields(headers, EXCLUDED_FIELDS);

        List<PsseCaseIdentification> caseIdentificationList = new ArrayList<>();
        caseIdentificationList.add(model.getCaseIdentification());

        writeBlock(PsseCaseIdentification.class, caseIdentificationList, headers,
            Util.insideHeaders(QUOTED_FIELDS, headers), context.getDelimiter().charAt(0), outputStream);
        Util.writeString(model.getCaseIdentification().getTitle1(), outputStream);
        Util.writeString(model.getCaseIdentification().getTitle2(), outputStream);
    }

    PsseCaseIdentification read1(JsonNode networkNode, Context context) {
        context.setDelimiter(",");
        PsseCaseIdentification caseIdentification = read(networkNode, context).get(0);
        context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
        context.setRawx(true);
        return caseIdentification;
    }

    ArrayData write1(PsseRawModel model, Context context) {
        String[] headers = context.getFieldNames(getRecordGroup());
        List<PsseCaseIdentification> caseIdentificationList = new ArrayList<>();
        caseIdentificationList.add(model.getCaseIdentification());

        List<String> stringList = writexBlock(PsseCaseIdentification.class,
            caseIdentificationList, headers, Util.insideHeaders(QUOTED_FIELDS, headers),
            context.getDelimiter().charAt(0));

        return new ArrayData(headers, stringList);
    }

    @Override
    public String[] quotedFields(PsseVersion version) {
        return QUOTED_FIELDS;
    }

    @Override
    public Class<PsseCaseIdentification> psseTypeClass() {
        return PsseCaseIdentification.class;
    }
}
