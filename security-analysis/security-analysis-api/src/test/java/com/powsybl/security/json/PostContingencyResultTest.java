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
import com.powsybl.security.*;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResult;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PostContingencyResultTest extends AbstractConverterTest {

    @Test
    public void testGetters() {
        Contingency contingency = new Contingency("contingency");
        LimitViolation violation = new LimitViolation("violation", LimitViolationType.HIGH_VOLTAGE, 420, (float) 0.1, 500);
        LimitViolationsResult result = new LimitViolationsResult(Collections.singletonList(violation));
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = new ArrayList<>();
        threeWindingsTransformerResults.add(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0));
        List<BranchResult> branchResults = new ArrayList<>();
        branchResults.add(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0));
        List<BusResult> busResults = new ArrayList<>();
        busResults.add(new BusResult("voltageLevelId", "busId", 400, 3.14));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, PostContingencyComputationStatus.CONVERGED, result, branchResults, busResults, threeWindingsTransformerResults);
        assertEquals(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getNetworkResult().getBranchResult("branchId"));
        assertEquals(new BusResult("voltageLevelId", "busId", 400, 3.14), postContingencyResult.getNetworkResult().getBusResult("busId"));
        assertEquals(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getNetworkResult().getThreeWindingsTransformerResult("threeWindingsTransformerId"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, postContingencyResult.getStatus());
    }

    @Test
    public void roundTrip() throws IOException {
        Contingency contingency = new Contingency("contingency");
        LimitViolation violation = new LimitViolation("violation", LimitViolationType.HIGH_VOLTAGE, 420, (float) 0.1, 500);
        LimitViolationsResult result = new LimitViolationsResult(Collections.singletonList(violation));
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = new ArrayList<>();
        threeWindingsTransformerResults.add(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0));
        List<BranchResult> branchResults = new ArrayList<>();
        branchResults.add(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0));
        List<BusResult> busResults = new ArrayList<>();
        busResults.add(new BusResult("voltageLevelId", "busId", 400, 3.14));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, PostContingencyComputationStatus.CONVERGED, result, branchResults, busResults, threeWindingsTransformerResults);
        roundTripTest(postContingencyResult, this::write, this::read, "/PostContingencyResultTest.json");
    }

    public void write(PostContingencyResult postContingencyResult, Path jsonFile) {
        try {
            OutputStream out = Files.newOutputStream(jsonFile);
            JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .writerWithDefaultPrettyPrinter()
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
