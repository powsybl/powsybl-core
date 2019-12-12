/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.AbstractAppStorageTest;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.ForwardingAppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class ForwardingAppStorageTest extends AbstractAppStorageTest {

    @Override
    protected AppStorage createStorage() {
        return new ForwardingAppStorage(MapDbAppStorage.createMem("mem", new InMemoryEventsBus()));
    }
}
