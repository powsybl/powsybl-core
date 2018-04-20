/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy;

import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AfsGroovyScriptTest extends AbstractGroovyScriptTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    @Override
    protected Reader getCodeReader() {
        return new StringReader(String.join(System.lineSeparator(),
                "println afs.fileSystemNames",
                "project = afs.getRootFolder('mem').createProject('test')",
                "print project.getName()"
        ));
    }

    @Override
    protected String getExpectedOutput() {
        return String.join(System.lineSeparator(), "[mem]", "test");
    }
}
