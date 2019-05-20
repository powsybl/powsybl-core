/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.NodeGenericMetadata;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class FooFileBuilder implements ProjectFileBuilder<FooFile> {

    private final ProjectFileBuildContext context;

    private String name;

    private String clazz;

    FooFileBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public FooFileBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public FooFileBuilder withClass(String clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public FooFile build() {
        if (name == null) {
            throw new IllegalStateException("name is not set");
        }
        String pseudoClass = "foo";
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, pseudoClass, "", 0, new NodeGenericMetadata());
        context.getStorage().setConsistent(info.getId());
        return new FooFile(new ProjectFileCreationContext(info,
                                                          context.getStorage(),
                                                          context.getProject()));
    }
}
