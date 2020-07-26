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
import com.powsybl.psse.model.PsseArea;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class AreaInterchangeData extends BlockData {

    AreaInterchangeData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    AreaInterchangeData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseArea> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.AREA_INTERCHANGE_DATA, PsseVersion.VERSION_33);

        String[] headers = areaInterchangeDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);

        context.setAreaInterchangeDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseArea.class, headers);
    }

    List<PsseArea> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.AREA_INTERCHANGE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode areaInterchangeNode = networkNode.get("area");
        if (areaInterchangeNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(areaInterchangeNode);
        List<String> records = nodeRecords(areaInterchangeNode);

        context.setAreaInterchangeDataReadFields(headers);
        return parseRecordsHeader(records, PsseArea.class, headers);
    }

    private static String[] areaInterchangeDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"iarea", "isw", "pdes", "ptol", "arname"};
        } else {
            return new String[] {"i", "isw", "pdes", "ptol", "arname"};
        }
    }
}
