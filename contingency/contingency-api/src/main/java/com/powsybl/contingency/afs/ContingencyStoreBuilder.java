/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.afs;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyStoreBuilder implements ProjectFileBuilder<ContingencyStore> {

    private final ProjectFileBuildContext context;

    private String name;

    public ContingencyStoreBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public ContingencyStoreBuilder withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    @Override
    public ContingencyStore build() {
        if (name == null) {
            throw new AfsException("Name is not set");
        }

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name).isPresent()) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, ContingencyStore.PSEUDO_CLASS,
                "", ContingencyStore.VERSION, new NodeGenericMetadata());

        context.getStorage().flush();

        return new ContingencyStore(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));
    }
}
