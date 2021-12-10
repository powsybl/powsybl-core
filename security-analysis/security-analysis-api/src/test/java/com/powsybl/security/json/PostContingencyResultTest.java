/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResults;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PostContingencyResultTest extends AbstractConverterTest {

    @Test
    public void testGetters() {
        Contingency contingency = new Contingency("contingency");
        LimitViolation violation = new LimitViolation("violation", LimitViolationType.HIGH_VOLTAGE, 420, (float) 0.1, 500);
        LimitViolationsResult result = new LimitViolationsResult(true, Collections.singletonList(violation));
        Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults = new HashMap<>();
        threeWindingsTransformerResults.put("threeWindingsTransformerId", new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0));
        Map<String, BranchResult> branchResults = new HashMap<>();
        branchResults.put("branchId", new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0));
        Map<String, BusResults> busResults = new HashMap<>();
        busResults.put("busId", new BusResults("voltageLevelId", "busId", 400, 3.14));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, result, branchResults, busResults, threeWindingsTransformerResults);
        assertEquals(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getBranchResult("branchId"));
        assertEquals(new BusResults("voltageLevelId", "busId", 400, 3.14), postContingencyResult.getBusResult("busId"));
        assertEquals(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getThreeWindingsTransformerResult("threeWindingsTransformerId"));
    }

    @Test
    public void roundTrip() throws IOException {
        Contingency contingency = new Contingency("contingency");
        LimitViolation violation = new LimitViolation("violation", LimitViolationType.HIGH_VOLTAGE, 420, (float) 0.1, 500);
        LimitViolationsResult result = new LimitViolationsResult(true, Collections.singletonList(violation));
        Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults = new HashMap<>();
        threeWindingsTransformerResults.put("threeWindingsTransformerId", new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0));
        Map<String, BranchResult> branchResults = new HashMap<>();
        branchResults.put("branchId", new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0));
        Map<String, BusResults> busResults = new HashMap<>();
        busResults.put("busId", new BusResults("voltageLevelId", "busId", 400, 3.14));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, result, branchResults, busResults, threeWindingsTransformerResults);
        roundTripTest(postContingencyResult, this::write, this::read, "/PostContingencyResultTest.json");
    }

    public void write(PostContingencyResult postContingencyResult, Path jsonFile) {
        try {
            OutputStream out = Files.newOutputStream(jsonFile);
            JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .writer()
                .writeValue(out, postContingencyResult);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PostContingencyResult read(Path jsonFile) {
        try {
            return JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .readerFor(PostContingencyResult.class)
                .readValue(Files.newInputStream(jsonFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
