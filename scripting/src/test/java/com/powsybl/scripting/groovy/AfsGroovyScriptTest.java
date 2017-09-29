/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.scripting.groovy;

import eu.itesla_project.afs.mapdb.storage.MapDbAppFileSystemStorage;
import eu.itesla_project.afs.storage.AppFileSystemStorage;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AfsGroovyScriptTest extends AbstractGroovyScriptTest {

    @Override
    protected AppFileSystemStorage createStorage() {
        return MapDbAppFileSystemStorage.createHeap("mem");
    }

    @Override
    protected Reader getCodeReader() {
        return new StringReader(String.join(System.lineSeparator(),
                "project = afs.getNode('mem:/').createProject('test', '')",
                "print project.getName()"
        ));
    }

    @Override
    protected String getExpectedOutput() {
        return "test";
    }
}
