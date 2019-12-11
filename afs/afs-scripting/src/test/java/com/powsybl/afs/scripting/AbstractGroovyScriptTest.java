/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.scripting;

import com.powsybl.afs.*;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.computation.ComputationManager;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import com.powsybl.scripting.groovy.GroovyScripts;
import groovy.lang.Binding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractGroovyScriptTest {

    private class AfsGroovyScriptExtensionMock implements GroovyScriptExtension {

        @Override
        public void load(Binding binding, ComputationManager computationManager) {
            binding.setProperty("afs", new AfsGroovyFacade(data));
        }

        @Override
        public void unload() {
        }
    }

    protected AppData data;

    protected abstract Reader getCodeReader();

    protected abstract String getExpectedOutput();

    protected List<GroovyScriptExtension> getExtensions() {
        return Collections.singletonList(new AfsGroovyScriptExtensionMock());
    }

    protected AppStorage createStorage() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createRootNodeIfNotExists(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(invocationOnMock -> new NodeInfo("id",
                                                             (String) invocationOnMock.getArguments()[0],
                                                             (String) invocationOnMock.getArguments()[1],
                                                             "", 0L, 0L, 0, new NodeGenericMetadata()));
        return storage;
    }

    protected List<FileExtension> getFileExtensions() {
        return emptyList();
    }

    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return emptyList();
    }

    protected List<ServiceExtension> getServiceExtensions() {
        return emptyList();
    }

    @Before
    public void setUp() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        data = new AppData(computationManager, computationManager,
                singletonList(cm -> singletonList(new AppFileSystem("mem", false, createStorage()))),
                getFileExtensions(), getProjectFileExtensions(), getServiceExtensions());
    }

    @After
    public void tearDown() {
        data.close();
    }

    @Test
    public void test() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Reader codeReader = getCodeReader(); PrintStream ps = new PrintStream(out)) {
            GroovyScripts.run(codeReader, getExtensions(), ps);
        }
        assertEquals(getExpectedOutput(), out.toString());
    }
}
