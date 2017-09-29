/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.mapdb;

import eu.itesla_project.afs.AppFileSystem;
import eu.itesla_project.afs.mapdb.storage.MapDbAppFileSystemStorage;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystem extends AppFileSystem {

    public MapDbAppFileSystem(String driveName, boolean remotelyAccessible, MapDbAppFileSystemStorage storage) {
        super(driveName, remotelyAccessible, storage);
    }
}
