/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppFileSystem implements AutoCloseable {

    public static final String FS_SEPARATOR = ":";
    public static final String PATH_SEPARATOR = "/";

    private final String name;

    private final boolean remotelyAccessible;

    private final AppFileSystemStorage storage;

    private AppData data;

    public AppFileSystem(String name, boolean remotelyAccessible, AppFileSystemStorage storage) {
        this.name = Objects.requireNonNull(name);
        this.remotelyAccessible = remotelyAccessible;
        this.storage = Objects.requireNonNull(storage);
    }

    public String getName() {
        return name;
    }

    public boolean isRemotelyAccessible() {
        return remotelyAccessible;
    }

    AppFileSystemStorage getStorage() {
        return storage;
    }

    public Folder getRootFolder() {
        return new Folder(storage.getRootNode(), storage, this);
    }

    public AppData getData() {
        return data;
    }

    void setData(AppData data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public void close() {
        storage.close();
    }
}
