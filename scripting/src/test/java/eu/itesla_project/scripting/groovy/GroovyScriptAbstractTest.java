/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.scripting.groovy;

import eu.itesla_project.afs.core.AppData;
import eu.itesla_project.afs.core.AppFileSystem;
import eu.itesla_project.afs.core.FileExtension;
import eu.itesla_project.afs.core.ProjectFileExtension;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.computation.ComputationManager;
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
public abstract class GroovyScriptAbstractTest {

    protected ComponentDefaultConfig componentDefaultConfig;

    protected AppData data;

    protected abstract Reader getCodeReader();

    protected abstract String getExpectedOutput();

    protected List<GroovyScriptExtension> getExtensions() {
        return emptyList();
    }

    protected AppFileSystemStorage createStorage() {
        return Mockito.mock(AppFileSystemStorage.class);
    }

    protected List<FileExtension> getFileExtensions() {
        return emptyList();
    }

    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return emptyList();
    }

    @Before
    public void setUp() throws Exception {
        componentDefaultConfig = Mockito.mock(ComponentDefaultConfig.class);
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        data = new AppData(computationManager, componentDefaultConfig,
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
