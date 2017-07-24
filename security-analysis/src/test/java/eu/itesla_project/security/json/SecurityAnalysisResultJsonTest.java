/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security.json;

import eu.itesla_project.commons.ConverterBaseTest;
import eu.itesla_project.contingency.*;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.security.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class SecurityAnalysisResultJsonTest extends ConverterBaseTest {

    private static SecurityAnalysisResult create() {
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, 100f, "limit", .95f, 110f, Country.FR, 380f);
        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, 100f, null, 110f);

        List<ContingencyElement> elements = Arrays.asList(
                new BranchContingency("NHV1_NHV2_2", "VLNHV1"),
                new BranchContingency("NHV1_NHV2_1"),
                new GeneratorContingency("GEN"),
                new BusbarSectionContingency("BBS1")
        );
        Contingency contingency = new ContingencyImpl("contingency", elements);

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, true, Arrays.asList(violation1, violation2), Arrays.asList("action1", "action2"));

        return new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), SecurityAnalysisResultSerializer::write, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");
    }
}
