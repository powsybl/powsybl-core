/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.NetworkFactory;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractProjectFileTest {

    protected AppFileSystemStorage storage;

    protected AppFileSystem afs;

    protected AppData ad;

    protected Network network;

    protected ComponentDefaultConfig componentDefaultConfig;

    protected abstract AppFileSystemStorage createStorage();

    protected List<FileExtension> getFileExtensions() {
        return Collections.emptyList();
    }

    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return Collections.emptyList();
    }

    @Before
    public void setup() throws IOException {
        componentDefaultConfig = Mockito.mock(ComponentDefaultConfig.class);
        network = NetworkFactory.create("test", "test");
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        storage = createStorage();
        afs = new AppFileSystem("mem", false, storage);
        ad = new AppData(computationManager,
                         componentDefaultConfig,
                         Collections.singletonList(computationManager1 -> Collections.singletonList(afs)),
                         getFileExtensions(),
                         getProjectFileExtensions());
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
    }
}