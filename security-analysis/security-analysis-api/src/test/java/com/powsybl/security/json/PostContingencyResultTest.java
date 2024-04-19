/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.*;
import com.powsybl.security.results.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class PostContingencyResultTest extends AbstractSerDeTest {

    @Test
    void testGetters() {
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
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, PostContingencyComputationStatus.CONVERGED, result, branchResults, busResults, threeWindingsTransformerResults,
                new ConnectivityResult(1, 2, 5.0, 10.0, Set.of("Id1", "Id2")));
        assertEquals(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getNetworkResult().getBranchResult("branchId"));
        assertEquals(new BusResult("voltageLevelId", "busId", 400, 3.14), postContingencyResult.getNetworkResult().getBusResult("busId"));
        assertEquals(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0), postContingencyResult.getNetworkResult().getThreeWindingsTransformerResult("threeWindingsTransformerId"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, postContingencyResult.getStatus());
        assertEquals(1, postContingencyResult.getConnectivityResult().getCreatedSynchronousComponentCount());
        assertEquals(2, postContingencyResult.getConnectivityResult().getCreatedConnectedComponentCount());
        assertEquals(5.0, postContingencyResult.getConnectivityResult().getDisconnectedLoadActivePower());
        assertEquals(10.0, postContingencyResult.getConnectivityResult().getDisconnectedGenerationActivePower());
        assertEquals(Set.of("Id1", "Id2"), postContingencyResult.getConnectivityResult().getDisconnectedElements());
    }

    @Test
    void roundTrip() throws IOException {
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
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, PostContingencyComputationStatus.CONVERGED, result, branchResults, busResults, threeWindingsTransformerResults,
                new ConnectivityResult(1, 1, 5.0, 10.0, Collections.emptySet()));
        roundTripTest(postContingencyResult, this::write, this::read, "/PostContingencyResultTest.json");
    }

    void write(PostContingencyResult postContingencyResult, Path jsonFile) {
        try {
            OutputStream out = Files.newOutputStream(jsonFile);
            JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .registerModule(new ActionJsonModule())
                .writerWithDefaultPrettyPrinter()
                .writeValue(out, postContingencyResult);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    PostContingencyResult read(Path jsonFile) {
        try {
            return JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .registerModule(new ActionJsonModule())
                .readerFor(PostContingencyResult.class)
                .readValue(Files.newInputStream(jsonFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
