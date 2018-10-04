package com.powsybl.triplestore.test;

/*
 * #%L
 * Triple stores for CGMES models
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.QueryCatalog;
import com.powsybl.triplestore.TripleStoreException;
import com.powsybl.triplestore.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesRatioTapChangerTest {

    @BeforeClass
    public static void setUp() throws TripleStoreException {
        Path folder = Paths.get("../../data/triple-store/rtc");
        Path input1 = folder.resolve("rtc-EQ.xml");
        Path input2 = folder.resolve("rtc-SSH.xml");
        String base = folder.toUri().normalize().toString();
        Path workingDir = folder;
        boolean doAsserts = true;
        tester = new TripleStoreTester(
                doAsserts,
                TripleStoreFactory.allImplementations(),
                base,
                workingDir,
                input1, input2);
        tester.load();
        queries = new QueryCatalog("cgmes-rtcs.sparql");
        queries.load(ClassLoader.getSystemResourceAsStream(queries.resource()));
    }

    @Test
    public void testTapChangerControls() throws Exception {
        Expected expected = new Expected().expect("TapChangerControl",
                "_97110e84-7da6-479c-846c-696fdaa83d56", "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b");
        tester.testQuery(queries.get("tapChangerControls"), expected);
    }

    @Test
    public void testTapChangerControlsOptionalSsh() throws Exception {
        Expected expected = new Expected().expect("TapChangerControl",
                "_97110e84-7da6-479c-846c-696fdaa83d56", "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b");
        tester.testQuery(queries.get("tapChangerControlsOptionalSSH"), expected);
    }

    @Test
    public void testTapChangerControlsOnlySshData() throws Exception {
        Expected expected = new Expected().expect("TapChangerControl",
                "_38f972bc-b7fd-4e75-8c24-379a86fbb506", "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b");
        tester.testQuery(queries.get("tapChangerControlsOnlySSHData"), expected);
    }

    @Test
    public void testTapChangerOptionalControlOptionalSsh() throws Exception {
        Expected expected = new Expected().expect(
                "RatioTapChanger",
                "_11111111-4a10-4031-b008-60c0dc340a07",
                "_83cc66dd-8d93-4a2c-8103-f1f5a9cf7e2e",
                "_955d9cd0-4a10-4031-b008-60c0dc340a07",
                "_fe25f43a-7341-446e-a71a-8ab7119ba806")
                .expect(
                        "TapChangerControl",
                        "_97110e84-7da6-479c-846c-696fdaa83d56",
                        "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b",
                        null, null);
        tester.testQuery(queries.get("tapChangerOptionalControlOptionalSSH"), expected);
    }

    private static TripleStoreTester tester;
    private static QueryCatalog      queries;
}
