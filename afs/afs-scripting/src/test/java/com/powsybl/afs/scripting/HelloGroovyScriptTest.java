/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.scripting;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HelloGroovyScriptTest extends AbstractGroovyScriptTest {

    @Override
    protected Reader getCodeReader() {
        return new StringReader("print 'hello'");
    }

    @Override
    protected String getExpectedOutput() {
        return "hello";
    }
}
