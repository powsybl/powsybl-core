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
import com.powsybl.psse.model.PsseFixedShunt;
import com.powsybl.psse.model.data.BlockData.PsseVersion;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class FixedBusShuntData extends BlockData {

    public FixedBusShuntData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    FixedBusShuntData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseFixedShunt> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.FixedBusShuntData, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = fixedBusShuntDataHeaders(this.getPsseVersion());
        context.setFixedBusShuntDataReadFields(readFields(records, headers, context.getDelimiter()));

        return parseRecordsHeader(records, PsseFixedShunt.class, headers);
    }

    List<PsseFixedShunt> read(JsonNode networkNode, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.FixedBusShuntData, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode fixedShuntNode = networkNode.get("fixshunt");
        if (fixedShuntNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(fixedShuntNode);
        List<String> records = nodeRecords(fixedShuntNode);

        context.setFixedBusShuntDataReadFields(headers);
        return parseRecordsHeader(records, PsseFixedShunt.class, headers);
    }

    static String[] fixedBusShuntDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "shntid", "stat", "gl", "bl"};
        } else {
            return new String[] {"i", "id", "status", "gl", "bl"};
        }
    }
}
