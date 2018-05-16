package com.powsybl.afs.ws.server;

import com.powsybl.afs.AppData;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.DefaultListenableAppStorage;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.ws.server.utils.AppDataBean;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Specializes;
import javax.inject.Singleton;
import java.util.Collections;

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
        ListenableAppStorage storage = new DefaultListenableAppStorage(MapDbAppStorage.createHeap(TEST_FS_NAME));
        Mockito.when(appData.getRemotelyAccessibleStorage(TEST_FS_NAME))
                .thenReturn(storage);
        Mockito.when(appData.getRemotelyAccessibleFileSystemNames())
                .thenReturn(Collections.singletonList(TEST_FS_NAME));
    }
}
