/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class SecurityAnalysisResultJsonTest extends AbstractConverterTest {

    private static SecurityAnalysisResult create() {
        // Create a LimitViolation(CURRENT) to ensure backward compatibility works
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "limit", 100f, 0.95f, 110f, Branch.Side.ONE);
        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "20'", 100f, 1.0f, 110f, Branch.Side.TWO);
        LimitViolation violation3 = new LimitViolation("GEN", LimitViolationType.HIGH_VOLTAGE, 100f, 0.9f, 110f);
        LimitViolation violation4 = new LimitViolation("GEN2", LimitViolationType.LOW_VOLTAGE, 100f, 0.7f, 115f);

        List<ContingencyElement> elements = Arrays.asList(
                new BranchContingency("NHV1_NHV2_2", "VLNHV1"),
                new BranchContingency("NHV1_NHV2_1"),
                new GeneratorContingency("GEN"),
                new BusbarSectionContingency("BBS1")
        );
        Contingency contingency = new ContingencyImpl("contingency", elements);

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, true, Arrays.asList(violation2, violation3, violation4), Arrays.asList("action1", "action2"));

        return new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), SecurityAnalysisResultSerializer::write, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");
    }
}
