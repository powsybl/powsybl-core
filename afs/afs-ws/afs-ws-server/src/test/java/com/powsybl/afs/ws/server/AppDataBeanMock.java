/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.AppData;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.afs.ws.server.utils.AppDataBean;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Specializes;
import javax.inject.Singleton;
import java.util.Collections;

import static org.mockito.Matchers.any;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Specializes
@Singleton
public class AppDataBeanMock extends AppDataBean {

    static final String TEST_FS_NAME = "mem";

    @PostConstruct
    @Override
    public void init() {
        appData = Mockito.mock(AppData.class);
        AppStorage storage = MapDbAppStorage.createMem(TEST_FS_NAME, new InMemoryEventsBus());
        Mockito.when(appData.getRemotelyAccessibleStorage(TEST_FS_NAME))
                .thenReturn(storage);
        Mockito.when(appData.getRemotelyAccessibleFileSystemNames())
                .thenReturn(Collections.singletonList(TEST_FS_NAME));
        AppFileSystem appFileSystem = new AppFileSystem(TEST_FS_NAME, true, storage);
        Mockito.when(appData.getFileSystem(any())).thenReturn(appFileSystem);
    }
}
