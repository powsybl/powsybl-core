/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectDependency<T extends ProjectNode> {

    private final String name;

    private final T projectNode;

    public ProjectDependency(String name, T projectNode) {
        this.name = Objects.requireNonNull(name);
        this.projectNode = Objects.requireNonNull(projectNode);
    }

    public String getName() {
        return name;
    }

    public T getProjectNode() {
        return projectNode;
    }
}
