/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseNonTransformerBranch;
import com.powsybl.psse.model.PsseNonTransformerBranch35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class NonTransformerBranchData extends BlockData {

    NonTransformerBranchData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    NonTransformerBranchData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseNonTransformerBranch> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.NonTransformerBranchData, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = nonTransformerBranchDataHeaders(this.getPsseVersion());
        context.setNonTransformerBranchDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseNonTransformerBranch35> nonTransformerBranch35List = parseRecordsHeader(records, PsseNonTransformerBranch35.class, headers);
            return new ArrayList<>(nonTransformerBranch35List); // TODO improve
        } else { // version_33
            return parseRecordsHeader(records, PsseNonTransformerBranch.class, headers);
        }
    }

    List<PsseNonTransformerBranch> read(JsonNode networkNode, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.NonTransformerBranchData, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode nonTransformerBranchNode = networkNode.get("acline");
        if (nonTransformerBranchNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(nonTransformerBranchNode);
        List<String> records = nodeRecords(nonTransformerBranchNode);

        context.setNonTransformerBranchDataReadFields(headers);
        List<PsseNonTransformerBranch35> nonTransformerBranch35List = parseRecordsHeader(records, PsseNonTransformerBranch35.class, headers);
        return new ArrayList<>(nonTransformerBranch35List); // TODO improve
    }

    private static String[] nonTransformerBranchDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"i", "j", "ckt", "r", "x", "b", "name", "rate1", "rate2", "rate3", "rate4", "rate5",
                "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12", "gi", "bi", "gj", "bj",
                "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        } else { // Version 33
            return new String[] {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj",
                "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        }
    }
}
