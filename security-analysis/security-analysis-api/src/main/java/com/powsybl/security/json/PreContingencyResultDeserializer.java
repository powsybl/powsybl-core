/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResults;
import com.powsybl.security.results.PreContingencyResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class PreContingencyResultDeserializer extends StdDeserializer<PreContingencyResult> {

    PreContingencyResultDeserializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public PreContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        LimitViolationsResult preContingencyResult = null;
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResults> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {

                case "limitViolationsResult":
                    parser.nextToken();
                    preContingencyResult = parser.readValueAs(LimitViolationsResult.class);
                    break;
                case "branchResults":
                    parser.nextToken();
                    branchResults = parser.readValueAs(new TypeReference<List<BranchResult>>() {
                    });
                    break;
                case "busResults":
                    parser.nextToken();
                    busResults = parser.readValueAs(new TypeReference<List<BusResults>>() {
                    });
                    break;
                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    threeWindingsTransformerResults = parser.readValueAs(new TypeReference<List<ThreeWindingsTransformerResult>>() {
                    });
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new PreContingencyResult(preContingencyResult,
            branchResults,
            busResults,
            threeWindingsTransformerResults);
    }
}
