/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb;

import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystem extends AppFileSystem {

    public MapDbAppFileSystem(String driveName, boolean remotelyAccessible, MapDbAppStorage storage) {
        super(driveName, remotelyAccessible, storage);
    }
}
