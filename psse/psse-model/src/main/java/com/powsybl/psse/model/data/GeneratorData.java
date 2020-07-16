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
import com.powsybl.psse.model.PsseGenerator;
import com.powsybl.psse.model.PsseGenerator35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GeneratorData extends BlockData {

    GeneratorData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    GeneratorData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseGenerator> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.GeneratorData, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = generatorDataHeaders(this.getPsseVersion());
        context.setGeneratorDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseGenerator35> generator35List = parseRecordsHeader(records, PsseGenerator35.class, headers);
            return new ArrayList<>(generator35List); // TODO improve
        } else { // version_33
            return parseRecordsHeader(records, PsseGenerator.class, headers);
        }
    }

    List<PsseGenerator> read(JsonNode networkNode, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.GeneratorData, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode generatorNode = networkNode.get("generator");
        if (generatorNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(generatorNode);
        List<String> records = nodeRecords(generatorNode);

        context.setGeneratorDataReadFields(headers);
        List<PsseGenerator35> generator35List = parseRecordsHeader(records, PsseGenerator35.class, headers);
        return new ArrayList<>(generator35List); // TODO improve
    }

    private static String[] generatorDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg", "mbase", "zr", "zx", "rt",
                "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};

        } else { // Version 33
            return new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt",
                "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
        }
    }
}
