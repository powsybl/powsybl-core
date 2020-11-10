/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CaseIdentificationData extends AbstractBlockData {

    CaseIdentificationData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    CaseIdentificationData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    PsseCaseIdentification read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.CASE_IDENTIFICATION_DATA, PsseVersion.VERSION_33);

        String line = readLineAndRemoveComment(reader);
        context.setDelimiter(detectDelimiter(line));

        String[] headers = caseIdentificationDataHeaders(line.split(context.getDelimiter()).length);
        PsseCaseIdentification caseIdentification = parseRecordHeader(line, PsseCaseIdentification.class, headers);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setCaseIdentificationDataReadFields(headers);
        return caseIdentification;
    }

    PsseCaseIdentification read(BufferedReader reader) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.CASE_IDENTIFICATION_DATA, PsseVersion.VERSION_33);

        String line = readLineAndRemoveComment(reader);

        String[] headers = caseIdentificationDataHeaders();
        PsseCaseIdentification caseIdentification = parseRecordHeader(line, PsseCaseIdentification.class, headers);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        return caseIdentification;
    }

    PsseCaseIdentification readx(JsonNode networkNode) {
        assertMinimumExpectedVersion(PsseBlockData.CASE_IDENTIFICATION_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode caseIdentificationNode = networkNode.get("caseid");
        if (caseIdentificationNode == null) {
            throw new PsseException("Psse: CaseIdentificationBlock does not exist");
        }

        String[] headers = nodeFields(caseIdentificationNode);
        List<String> records = nodeRecords(caseIdentificationNode);
        List<PsseCaseIdentification> caseIdentificationList = parseRecordsHeader(records, PsseCaseIdentification.class, headers);
        if (caseIdentificationList.size() != 1) {
            throw new PsseException("Psse: CaseIdentificationBlock, unexpected size " + caseIdentificationList.size());
        }

        return caseIdentificationList.get(0);
    }

    PsseCaseIdentification readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.CASE_IDENTIFICATION_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        context.setDelimiter(",");

        JsonNode caseIdentificationNode = networkNode.get("caseid");
        if (caseIdentificationNode == null) {
            throw new PsseException("Psse: CaseIdentificationBlock does not exist");
        }

        String[] headers = nodeFields(caseIdentificationNode);
        List<String> records = nodeRecords(caseIdentificationNode);
        List<PsseCaseIdentification> caseIdentificationList = parseRecordsHeader(records, PsseCaseIdentification.class, headers);
        if (caseIdentificationList.size() != 1) {
            throw new PsseException("Psse: CaseIdentificationBlock, unexpected size " + caseIdentificationList.size());
        }

        context.setCaseIdentificationDataReadFields(headers);
        return caseIdentificationList.get(0);
    }

    private static String[] caseIdentificationDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq"};
        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), "title1", "title2");
    }

    private static String[] caseIdentificationDataHeaders() {
        return new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
    }
}
