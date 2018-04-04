/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy;

import com.powsybl.afs.*;
import com.powsybl.afs.storage.*;
import com.powsybl.afs.storage.events.AppStorageListener;
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

    private static class ListenableAppStorageMock extends ForwardingAppStorage implements ListenableAppStorage {

        public ListenableAppStorageMock(AppStorage storage) {
            super(storage);
        }

        @Override
        public void addListener(AppStorageListener l) {
        }

        @Override
        public void removeListener(AppStorageListener l) {
        }

        @Override
        public void removeListeners() {
        }
    }

    protected AppData data;

    protected abstract Reader getCodeReader();

    protected abstract String getExpectedOutput();

    protected List<GroovyScriptExtension> getExtensions() {
        return emptyList();
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
    public void setUp() throws Exception {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        data = new AppData(computationManager, computationManager,
                singletonList(cm -> singletonList(new AppFileSystem("mem", false, new ListenableAppStorageMock(createStorage())))),
                getFileExtensions(), getProjectFileExtensions(), getServiceExtensions());
    }

    @After
    public void tearDown() {
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
