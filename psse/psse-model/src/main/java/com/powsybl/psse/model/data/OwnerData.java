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
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseOwner;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class OwnerData extends BlockData {

    OwnerData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    OwnerData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseOwner> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.OwnerData, PsseVersion.VERSION_33);

        String[] headers = ownerDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);

        context.setOwnerDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    List<PsseOwner> read(JsonNode networkNode, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.OwnerData, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode ownerNode = networkNode.get("owner");
        if (ownerNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(ownerNode);
        List<String> records = nodeRecords(ownerNode);

        context.setOwnerDataReadFields(headers);
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    private static String[] ownerDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"iowner", "owname"};
        } else {
            return new String[] {"i", "owname"};
        }
    }
}
