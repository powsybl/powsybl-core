/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;


import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.IOException;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public class AsciiTableFormatterTest {


    @Test
    public void testConfig() throws IOException {

        AsciiTableFormatter formatter = new AsciiTableFormatter("",new Column("column1"));

    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(AsciiTableFormatterTest.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }
}
