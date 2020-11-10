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
import com.powsybl.psse.model.PsseSwitchedShunt;
import com.powsybl.psse.model.PsseSwitchedShunt35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchedShuntData extends AbstractBlockData {

    SwitchedShuntData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    SwitchedShuntData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseSwitchedShunt> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.SWITCHED_SHUNT_DATA, PsseVersion.VERSION_33);

        String[] headers = switchedShuntDataDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);
        context.setSwitchedShuntDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseSwitchedShunt35> switchedShunt35List = parseRecordsHeader(records, PsseSwitchedShunt35.class, headers);
            return new ArrayList<>(switchedShunt35List);
        } else { // version_33
            return parseRecordsHeader(records, PsseSwitchedShunt.class, headers);
        }
    }

    List<PsseSwitchedShunt> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.SWITCHED_SHUNT_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode switchedShuntDataNode = networkNode.get("swshunt");
        if (switchedShuntDataNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(switchedShuntDataNode);
        List<String> records = nodeRecords(switchedShuntDataNode);

        context.setSwitchedShuntDataReadFields(headers);
        List<PsseSwitchedShunt35> switchedShunt35List = parseRecordsHeader(records, PsseSwitchedShunt35.class, headers);
        return new ArrayList<>(switchedShunt35List);
    }

    private static String[] switchedShuntDataDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit",
                "s1", "n1", "b1", "s2", "n2", "b2", "s3", "n3", "b3", "s4", "n4", "b4", "s5", "n5", "b5", "s6", "n6", "b6",
                "s7", "n7", "b7", "s8", "n8", "b8"};
        } else {
            return new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit",
                "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};
        }
    }
}
