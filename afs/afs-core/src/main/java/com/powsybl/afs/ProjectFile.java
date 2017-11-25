/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFile extends ProjectNode {

    protected final FileIcon icon;

    protected ProjectFile(ProjectFileCreationContext context, FileIcon icon) {
        super(context, true);
        this.icon = Objects.requireNonNull(icon);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public FileIcon getIcon() {
        return icon;
    }

    public List<ProjectNode> getDependencies() {
        return storage.getDependenciesInfo(info.getId())
                .stream()
                .map(this::findProjectNode)
                .collect(Collectors.toList());
    }

    public void onDependencyChanged() {
        // method to override to be notified in case of dependency change
    }
}
