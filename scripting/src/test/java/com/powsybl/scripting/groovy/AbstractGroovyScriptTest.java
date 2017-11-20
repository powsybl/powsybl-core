/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy;

import com.powsybl.afs.AppData;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.FileExtension;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.computation.ComputationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractGroovyScriptTest {

    protected AppData data;

    protected abstract Reader getCodeReader();

    protected abstract String getExpectedOutput();

    protected List<GroovyScriptExtension> getExtensions() {
        return emptyList();
    }

    protected AppStorage createStorage() {
        return Mockito.mock(AppStorage.class);
    }

    protected List<FileExtension> getFileExtensions() {
        return emptyList();
    }

    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return emptyList();
    }

    @Before
    public void setUp() throws Exception {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        data = new AppData(computationManager,
                singletonList(cm -> singletonList(new AppFileSystem("mem", false, createStorage()))),
                getFileExtensions(), getProjectFileExtensions());
    }

    @After
    public void tearDown() throws Exception {
        data.close();
    }

    @Test
    public void test() throws IOException {
        StringWriter out = new StringWriter();
        try (Reader codeReader = getCodeReader()) {
            GroovyScripts.run(codeReader, data, getExtensions(), out);
        } finally {
            out.close();
        }
        assertEquals(out.toString(), getExpectedOutput());
    }
}
