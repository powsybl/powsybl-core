/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppStorage;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFileContext {

    private final AppStorage storage;

    private final Project project;

    protected ProjectFileContext(AppStorage storage, Project project) {
        this.storage = Objects.requireNonNull(storage);
        this.project = Objects.requireNonNull(project);
    }

    public AppStorage getStorage() {
        return storage;
    }

    public Project getProject() {
        return project;
    }
}
