/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.mmap;

import eu.itesla_project.offline.db.AbstractOfflineDbTest;
import org.junit.Test;

import java.io.IOException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MMapOfflineDbTest extends AbstractOfflineDbTest {

    @Test
    public void testMMapImplChunkSample10() throws IOException {
        try (MMapOfflineDb offlineDb = new MMapOfflineDb(new MMapOfflineDbConfig(tmpDir, 10, 5, 70), DB_NAME, file -> new MemoryMapFileTestImpl())) {
            test(offlineDb);
        }
    }

    @Test
    public void testMMapImplChunkSample1() throws IOException {
        try (MMapOfflineDb offlineDb = new MMapOfflineDb(new MMapOfflineDbConfig(tmpDir, 1, 5, 70), DB_NAME, file -> new MemoryMapFileTestImpl())) {
            test(offlineDb);
        }
    }

}
