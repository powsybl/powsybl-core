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
import com.powsybl.psse.model.PsseException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CaseIdentificationData extends AbstractDataBlock<PsseCaseIdentification> {

    CaseIdentificationData() {
        super(PsseDataBlock.CASE_IDENTIFICATION_DATA);
    }

    public PsseCaseIdentification read1(BufferedReader reader, PsseContext context) throws IOException {
        String line = Util.readLineAndRemoveComment(reader);
        context.setDelimiter(Util.detectDelimiter(line));

        String[] headers = caseIdentificationDataHeaders(line.split(context.getDelimiter()).length);
        PsseCaseIdentification caseIdentification = parseRecordHeader(line, PsseCaseIdentification.class, headers);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setFieldNames(getDataBlock(), headers);
        context.setVersion(PsseVersion.fromNumber(caseIdentification.getRev()));
        return caseIdentification;
    }

    PsseCaseIdentification readx1(JsonNode networkNode, PsseContext context) {
        context.setDelimiter(",");

        JsonNode caseIdentificationNode = networkNode.get("caseid");
        if (caseIdentificationNode == null) {
            throw new PsseException("Psse: CaseIdentificationBlock does not exist");
        }

        String[] headers = Util.nodeFieldNames(caseIdentificationNode);
        List<String> records = Util.nodeRecords(caseIdentificationNode);
        List<PsseCaseIdentification> caseIdentificationList = parseRecords(records, PsseCaseIdentification.class, headers);
        if (caseIdentificationList.size() != 1) {
            throw new PsseException("Psse: CaseIdentificationBlock, unexpected size " + caseIdentificationList.size());
        }

        context.setFieldNames(getDataBlock(), headers);
        return caseIdentificationList.get(0);
    }

    private static String[] caseIdentificationDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq"};
        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), "title1", "title2");
    }

    private static String[] caseIdentificationDataHeaders() {
        return new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        throw new PsseException("XXX(Luma) fieldNames for CaseIdentification");
    }

    @Override
    public Class<? extends PsseCaseIdentification> psseTypeClass(PsseVersion version) {
        throw new PsseException("XXX(Luma) typeClass for CaseIdentification");
    }
}
