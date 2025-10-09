/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.alternatives.test;

import java.io.IOException;
import java.util.function.Consumer;

import com.powsybl.commons.datasource.ResourceSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.alternatives.test.AlternativeQueriesTester.Expected;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class AlternativeQueriesForRatioTapChangersTest {

    @BeforeAll
    static void setUp() {
        GridModelReference model = new GridModelReferenceResources(
                "not_all_tap_changers_have_control",
                null,
                new ResourceSet("/sample_not_all_tap_changers_have_control", "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                                                                             "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"));
        Expected expected = new Expected()
                .resultSize(3)
                .propertyCount("regulatingControlTargetValue", 2);
        int experiments = 1;
        boolean doAssert = true;
        boolean cacheModels = false;
        Consumer<PropertyBags> consumer = rs -> {
            rs.forEach(r -> LOG.info("    {} {} {} {} {} {} {} {}",
                    r.getId0("RatioTapChanger"),
                    r.getId0("TapChangerControl"),
                    r.getId0("TapChangerControlSSH"),
                    r.containsKey("regulatingControlTargetValue"),
                    r.asDouble("regulatingControlTargetValue"),
                    r.asDouble("regulatingControlTargetDeadband"),
                    r.asBoolean("regulatingControlEnabled", false),
                    r.get("RatioTapChanger")));
            if (LOG.isInfoEnabled()) {
                LOG.info(rs.tabulateLocals());
            }
        };
        tester = new AlternativeQueriesTester(TripleStoreFactory.allImplementations(),
                new QueryCatalog("ratioTapChangers.sparql"), model, expected, experiments, doAssert, consumer,
                cacheModels);
        tester.load();
        testerNestedGraph = new AlternativeQueriesTester(
                TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(),
                new QueryCatalog("ratioTapChangers.sparql"), model, expected, experiments, doAssert, consumer,
                cacheModels);
        testerNestedGraph.load();
    }

    @Test
    void simple() throws IOException {
        // All ratio tap changers should have the property lowStep
        long s = tester.expected().resultSize();
        Expected expected = new Expected()
                .resultSize(s)
                .propertyCount("lowStep", s);
        tester.test("simple", expected);
    }

    @Test
    void noGraphClauses() throws IOException {
        tester.test("noGraphClauses");
    }

    @Test
    void nestedGraph() throws IOException {
        testerNestedGraph.test("nestedGraph");
    }

    @Test
    void optionalFilteredEquals() throws IOException {
        tester.test("optionalFilteredEquals");
    }

    @Test
    void optionalFilteredSameTerm() throws IOException {
        tester.test("optionalFilteredSameTerm");
    }

    private static AlternativeQueriesTester tester;
    private static AlternativeQueriesTester testerNestedGraph;

    private static final Logger LOG = LoggerFactory.getLogger(AlternativeQueriesForRatioTapChangersTest.class);
}
