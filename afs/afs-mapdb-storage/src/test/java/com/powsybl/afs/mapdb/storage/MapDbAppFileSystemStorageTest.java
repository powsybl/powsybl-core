/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.AbstractAppFileSystemStorageTest;
import com.powsybl.afs.storage.AppFileSystemStorage;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemStorageTest extends AbstractAppFileSystemStorageTest {

    @Override
    protected AppFileSystemStorage createStorage() {
        return MapDbAppFileSystemStorage.createMem("mem");
    }
}
