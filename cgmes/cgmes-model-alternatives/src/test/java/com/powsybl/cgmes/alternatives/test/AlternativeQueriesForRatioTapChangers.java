package com.powsybl.cgmes.alternatives.test;

/*
 * #%L
 * CGMES Model Alternatives
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.alternatives.test.AlternativeQueriesTester.Expected;
import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.cgmes_conformity.test.CgmesConformity1Catalog;
import com.powsybl.triplestore.PropertyBags;
import com.powsybl.triplestore.QueryCatalog;
import com.powsybl.triplestore.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AlternativeQueriesForRatioTapChangers {

    @BeforeClass
    public static void setUp() {
        TestGridModel model = new CgmesConformity1Catalog()
                .microGridBaseCaseBEModifiedNotAllTapChangersHaveControl();
        Expected expected = new Expected()
                .resultSize(3)
                .propertyCount("regulatingControlTargetValue", 2);
        int experiments = 10;
        boolean doAssert = true;
        Consumer<PropertyBags> consumer = rs -> {
            rs.stream()
                    .forEach(r -> LOG.info("    {} {} {} {} {} {} {} {}",
                            r.getId0("RatioTapChanger"),
                            r.getId0("TapChangerControl"),
                            r.getId0("TapChangerControlSSH"),
                            r.containsKey("regulatingControlTargetValue"),
                            r.asDouble("regulatingControlTargetValue"),
                            r.asDouble("regulatingControlTargetDeadband"),
                            r.asBoolean("regulatingControlEnabled", false),
                            r.get("RatioTapChanger")));
            LOG.info(rs.tabulateLocals());
        };
        tester = new AlternativeQueriesTester(
                TripleStoreFactory.allImplementations(),
                new QueryCatalog("ratioTapChangers.sparql"),
                model,
                expected,
                experiments,
                doAssert,
                consumer);
        tester.load();
        testerNestedGraph = new AlternativeQueriesTester(
                TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(),
                new QueryCatalog("ratioTapChangers.sparql"),
                model,
                expected,
                experiments,
                doAssert,
                consumer);
        testerNestedGraph.load();
    }

    @Test
    public void simple() {
        // All ratio tap changers should have the property lowStep
        long s = tester.expected().resultSize();
        Expected expected = new Expected()
                .resultSize(s)
                .propertyCount("lowStep", s);
        tester.test("simple", expected);
    }

    @Test
    public void noGraphClauses() {
        tester.test("noGraphClauses");
    }

    @Test
    public void nestedGraph() {
        testerNestedGraph.test("nestedGraph");
    }

    @Test
    public void optionalFilteredEquals() {
        tester.test("optionalFilteredEquals");
    }

    @Test
    public void optionalFilteredSameTerm() {
        tester.test("optionalFilteredSameTerm");
    }

    private static AlternativeQueriesTester tester;
    private static AlternativeQueriesTester testerNestedGraph;

    private static final Logger             LOG = LoggerFactory
            .getLogger(AlternativeQueriesForRatioTapChangers.class);
}
