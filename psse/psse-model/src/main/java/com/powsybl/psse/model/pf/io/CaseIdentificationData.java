/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.Util;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CaseIdentificationData extends AbstractRecordGroup<PsseCaseIdentification> {

    CaseIdentificationData() {
        super(PowerFlowRecordGroup.CASE_IDENTIFICATION, "ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2");
        withQuotedFields("title1", "title2");
    }

    PsseCaseIdentification read1(BufferedReader reader, Context context) throws IOException {
        String line = Util.readLineAndRemoveComment(reader);
        context.detectDelimiter(line);

        String[] headers = fieldNames(context.getVersion());
        PsseCaseIdentification caseIdentification = parseSingleRecord(line, headers, context);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setFieldNames(recordGroup, headers);
        context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
        context.setFileFormat(LEGACY_TEXT);
        return caseIdentification;
    }

    PsseCaseIdentification read1x(BufferedReader reader, Context context) throws IOException {
        context.setFileFormat(JSON);
        PsseCaseIdentification caseIdentification = readJson(reader, context).get(0);
        context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
        return caseIdentification;
    }

    PsseCaseIdentification read1x(JsonNode node, Context context) throws IOException {
        context.setFileFormat(JSON);
        PsseCaseIdentification caseIdentification = readJson(node, context).get(0);
        context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
        return caseIdentification;
    }

    void write1(PssePowerFlowModel model, Context context, OutputStream outputStream) {
        // Adapt headers of case identification record
        // title1 and title2 go in separate lines in legacy text format
        String[] headers = ArrayUtils.removeElements(context.getFieldNames(recordGroup), "title1", "title2");
        writeLegacyText(PsseCaseIdentification.class, Collections.singletonList(model.getCaseIdentification()), headers, Util.intersection(quotedFields(), headers), context, outputStream);
        Util.writeString(model.getCaseIdentification().getTitle1(), outputStream);
        Util.writeString(model.getCaseIdentification().getTitle2(), outputStream);
    }

    void write1x(PssePowerFlowModel model, Context context, JsonGenerator generator) {
        String[] headers = context.getFieldNames(recordGroup);
        String record = buildRecords(PsseCaseIdentification.class, Collections.singletonList(model.getCaseIdentification()), headers, Util.intersection(quotedFields(), headers), context).get(0);
        writeJson(headers, Collections.singletonList(record), generator);
    }

    @Override
    public Class<PsseCaseIdentification> psseTypeClass() {
        return PsseCaseIdentification.class;
    }
}
