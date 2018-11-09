/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.test;

import org.junit.Test;

import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreClearTest {
    @Test
    public void testClearSubset() {
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml"};
        TripleStoreTester tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testClear("contexts:cgmes-rtcs/rtc-EQ.xml", "");
    }

    @Test
    public void testClearSubsetLocalName() {
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml"};
        TripleStoreTester tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testClear("cgmes-rtcs/rtc-EQ.xml", "contexts:");
    }
}
