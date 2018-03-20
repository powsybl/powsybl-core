/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AfsExtensionErrorGroovyScriptTest extends AbstractGroovyScriptTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    @Override
    protected Reader getCodeReader() {
        return new StringReader(String.join(System.lineSeparator(),
                "project = afs.getRootFolder('mem').createProject('test')",
                "bar = project.getRootFolder().barBuilder().build()"
        ));
    }

    @Override
    protected String getExpectedOutput() {
        return "";
    }

    @Override
    @Test(expected = AfsException.class)
    public void test() throws IOException {
        super.test();
    }
}
