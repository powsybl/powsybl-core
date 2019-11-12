/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.test;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreClearTest {

    @BeforeClass
    public static void setUp() {
        queries = new QueryCatalog("cgmes-rtcs/cgmes-rtcs.sparql");
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml"};
        tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();

        expectedTapChangerControls = new Expected().expect("TapChangerControl",
            "_97110e84-7da6-479c-846c-696fdaa83d56", "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b");
        expectedEmpty = new Expected();
    }

    @Test
    public void testClearSubset() {
        tester.load();
        tester.testQuery(queries.get("tapChangerControls"), expectedTapChangerControls);
        tester.clear("contexts:cgmes-rtcs/rtc-EQ.xml", "");
        tester.testQuery(queries.get("tapChangerControls"), expectedEmpty);
    }

    @Test
    public void testClearSubsetLocalName() {
        tester.load();
        tester.testQuery(queries.get("tapChangerControls"), expectedTapChangerControls);
        tester.clear("cgmes-rtcs/rtc-EQ.xml", "contexts:");
        tester.testQuery(queries.get("tapChangerControls"), expectedEmpty);
    }

    private static TripleStoreTester tester;
    private static QueryCatalog queries;
    private static Expected expectedTapChangerControls;
    private static Expected expectedEmpty;
}
