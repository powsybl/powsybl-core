package com.powsybl.cgmes_conformity.test;

/*
 * #%L
 * CGMES conformity
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.test.CgmesModelTester;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1Test {

    @BeforeClass
    public static void setUp() {
        catalog = new CgmesConformity1Catalog();
    }

    @Test
    public void microGridBaseCaseBE() {
        new CgmesModelTester(catalog.microGridBaseCaseBE()).test();
    }

    @Test
    public void miniNodeBreaker() {
        new CgmesModelTester(catalog.miniNodeBreaker()).test();
    }

    private static CgmesConformity1Catalog catalog;
}
