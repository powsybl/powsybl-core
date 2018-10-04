package com.powsybl.cgmes.test;

/*
 * #%L
 * CGMES data model
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Paths;

import org.junit.Test;

import com.powsybl.cgmes.CgmesModelException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MissingCaseTest {
    @Test(expected = CgmesModelException.class)
    public void missing()  {
        TestGridModel missing = new TestGridModel(
                Paths.get("./thisTestCaseDoesNotExist"),
                "",
                null,
                false,
                false);
        new CgmesModelTester(missing).test();
    }
}
