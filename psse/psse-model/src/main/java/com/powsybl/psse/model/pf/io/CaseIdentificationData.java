/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CaseIdentificationData extends AbstractRecordGroup<PsseCaseIdentification> {
    private static final String STR_TITLE_1 = "title1";
    private static final String STR_TITLE_2 = "title2";

    CaseIdentificationData() {
        super(PowerFlowRecordGroup.CASE_IDENTIFICATION, "ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", STR_TITLE_1, STR_TITLE_2);
        withQuotedFields(STR_TITLE_1, STR_TITLE_2);
        withIO(LEGACY_TEXT, new CaseIdentificationLegacyText(this));
        withIO(JSON, new CaseIdentificationJson(this));
    }

    @Override
    protected Class<PsseCaseIdentification> psseTypeClass() {
        return PsseCaseIdentification.class;
    }

    private static class CaseIdentificationLegacyText extends RecordGroupIOLegacyText<PsseCaseIdentification> {
        protected CaseIdentificationLegacyText(AbstractRecordGroup<PsseCaseIdentification> recordGroup) {
            super(recordGroup);
        }

        @Override
        public PsseCaseIdentification readHead(LegacyTextReader reader, Context context) throws IOException {
            String line = reader.readRecordLine();
            context.detectDelimiter(line);

            String[] headers = recordGroup.fieldNames(context.getVersion());
            PsseCaseIdentification caseIdentification = recordGroup.parseSingleRecord(line, headers, context);
            caseIdentification.setTitle1(reader.readLine());
            caseIdentification.setTitle2(reader.readLine());

            context.setFieldNames(recordGroup.getIdentification(), headers);
            context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
            return caseIdentification;
        }

        @Override
        public void writeHead(PsseCaseIdentification caseIdentification, Context context, OutputStream outputStream) {
            // Adapt headers of case identification record
            // title1 and title2 go in separate lines in legacy text format
            String[] headers = ArrayUtils.removeElements(context.getFieldNames(recordGroup.getIdentification()), STR_TITLE_1, STR_TITLE_2);
            String[] quotedFields = recordGroup.quotedFields();
            write(Collections.singletonList(caseIdentification), headers, Util.retainAll(quotedFields, headers), context, outputStream);
            writeLine(caseIdentification.getTitle1(), outputStream);
            writeLine(caseIdentification.getTitle2(), outputStream);
        }

        private static void writeLine(String s, OutputStream outputStream) {
            try {
                outputStream.write(s.getBytes());
                outputStream.write(System.lineSeparator().getBytes());
            } catch (IOException e) {
                throw new PsseException("Writing head record", e);
            }
        }

        @Override
        public List<PsseCaseIdentification> read(LegacyTextReader reader, Context context) throws IOException {
            throw new PsseException("Case Identification can not be read as a record group, it was be read as head record");
        }

        @Override
        public void write(List<PsseCaseIdentification> psseObjects, Context context, OutputStream outputStream) {
            throw new PsseException("Case Identification can not be written as a record group, it was be written as head record");
        }
    }

    private static class CaseIdentificationJson extends RecordGroupIOJson<PsseCaseIdentification> {
        public CaseIdentificationJson(AbstractRecordGroup<PsseCaseIdentification> recordGroup) {
            super(recordGroup);
        }

        @Override
        public PsseCaseIdentification readHead(LegacyTextReader reader, Context context) throws IOException {
            PsseCaseIdentification caseIdentification = read(reader, context).get(0);
            context.setVersion(PsseVersion.fromRevision(caseIdentification.getRev()));
            return caseIdentification;
        }

        @Override
        public void writeHead(PsseCaseIdentification caseIdentification, Context context, OutputStream outputStream) {
            write(Collections.singletonList(caseIdentification), context, outputStream);
        }
    }

}
