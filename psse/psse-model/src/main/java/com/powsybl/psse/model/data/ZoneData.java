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
import com.powsybl.psse.model.PsseZone;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class ZoneData extends BlockData {

    ZoneData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    ZoneData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseZone> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.ZONE_DATA, PsseVersion.VERSION_33);

        String[] headers = zoneDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);

        context.setZoneDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseZone.class, headers);
    }

    List<PsseZone> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.ZONE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode zoneNode = networkNode.get("zone");
        if (zoneNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(zoneNode);
        List<String> records = nodeRecords(zoneNode);

        context.setZoneDataReadFields(headers);
        return parseRecordsHeader(records, PsseZone.class, headers);
    }

    private static String[] zoneDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"izone", "zoname"};
        } else {
            return new String[] {"i", "zoname"};
        }
    }
}
