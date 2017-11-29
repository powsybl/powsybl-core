/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FooFileExtension implements ProjectFileExtension<FooFile, FooFileBuilder> {
    @Override
    public Class<FooFile> getProjectFileClass() {
        return FooFile.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return "foo";
    }

    @Override
    public Class<FooFileBuilder> getProjectFileBuilderClass() {
        return FooFileBuilder.class;
    }

    @Override
    public FooFile createProjectFile(ProjectFileCreationContext context) {
        return new FooFile(context);
    }

    @Override
    public FooFileBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new FooFileBuilder(context);
    }
}
