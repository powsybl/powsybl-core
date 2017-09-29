/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractAppFileSystemConfig<T extends AbstractAppFileSystemConfig<T>> {

    protected static final boolean DEFAULT_REMOTELY_ACCESSIBLE = false;

    protected String driveName;

    protected boolean remotelyAccessible;

    public AbstractAppFileSystemConfig(String driveName, boolean remotelyAccessible) {
        this.driveName = Objects.requireNonNull(driveName);
        this.remotelyAccessible = remotelyAccessible;
    }

    public String getDriveName() {
        return driveName;
    }

    public T setDriveName(String driveName) {
        this.driveName = Objects.requireNonNull(driveName);
        return (T) this;
    }

    public boolean isRemotelyAccessible() {
        return remotelyAccessible;
    }

    public T setRemotelyAccessible(boolean remotelyAccessible) {
        this.remotelyAccessible = remotelyAccessible;
        return (T) this;
    }
}
