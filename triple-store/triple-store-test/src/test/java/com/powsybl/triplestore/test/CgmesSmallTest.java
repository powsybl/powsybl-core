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

import com.powsybl.triplestore.TripleStoreException;
import com.powsybl.triplestore.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesSmallTest {

    @BeforeClass
    public static void setUp() throws TripleStoreException {
        Path folder = Paths.get("../../data/cim14/smallcase1");
        Path input1 = folder.resolve("case1_EQ.xml");
        Path input2 = folder.resolve("case1_TP.xml");
        Path input3 = folder.resolve("case1_SV.xml");
        String base = folder.toUri().normalize().toString();
        Path workingDir = folder;
        boolean doAsserts = true;
        tester = new TripleStoreTester(
                doAsserts,
                TripleStoreFactory.allImplementations(),
                base,
                workingDir,
                input1, input2, input3);
        tester.load();
    }

    @Test
    public void testWrite() throws Exception {
        tester.testWrite();
    }

    @Test
    public void testAllTypedSubjects() throws Exception {
        Expected expected = new Expected()
                .expect(
                        "s",
                        "_AC_PATL_OLT", "_BV_0", "_BV_1", "_BV_2", "_CA_1",
                        "_GEN_____-GRID____-1_PT", "_GEN_____-GRID____-1_TW_EX",
                        "_GEN_____-GRID____-1_TW_EX_CL", "_GEN_____-GRID____-1_TW_EX_OLS",
                        "_GEN_____-GRID____-1_TW_EX_TE", "_GEN_____-GRID____-1_TW_EX_TE",
                        "_GEN_____-GRID____-1_TW_OR", "_GEN_____-GRID____-1_TW_OR_CL",
                        "_GEN_____-GRID____-1_TW_OR_OLS", "_GEN_____-GRID____-1_TW_OR_TE",
                        "_GEN_____-GRID____-1_TW_OR_TE", "_GEN______GU", "_GEN______SM",
                        "_GEN______SM_RC", "_GEN______SM_TE", "_GEN______SM_TE", "_GEN______SS",
                        "_GEN______TN", "_GEN______VL", "_GRID____-INF_____-1_AC",
                        "_GRID____-INF_____-1_AC_CL", "_GRID____-INF_____-1_AC_OLS",
                        "_GRID____-INF_____-1_AC_TE_EX", "_GRID____-INF_____-1_AC_TE_EX",
                        "_GRID____-INF_____-1_AC_TE_OR", "_GRID____-INF_____-1_AC_TE_OR",
                        "_GRID_____TN", "_GRID_____VL", "_GR_1_", "_INF______GU", "_INF______SM",
                        "_INF______SM_RC", "_INF______SM_TE", "_INF______SM_TE", "_INF______SS",
                        "_INF______TN", "_INF______VL", "_SGR_1_", "_SVPF_0", "_SVPF_1", "_SV_0",
                        "_SV_1", "_SV_2", "_T3_PATL_OLT", "_TI_1", "_TW_PATL_OLT", "version");
        tester.testQuery("SELECT * { GRAPH ?g { ?s a ?type }}", expected);
    }

    private static TripleStoreTester tester;
}
