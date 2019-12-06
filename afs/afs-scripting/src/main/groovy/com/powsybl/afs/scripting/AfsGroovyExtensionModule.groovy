/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.scripting

import com.powsybl.afs.AfsException
import com.powsybl.afs.FileExtension
import com.powsybl.afs.Folder
import com.powsybl.afs.ProjectFileExtension
import com.powsybl.afs.ProjectFolder

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class AfsGroovyExtensionModule {

    static findGetChildPseudoClass(String name, args) {
        if (name.startsWith("get") && name.size() > 3 && args.length > 0 && !args.find { !(it instanceof String) }) {
            name.substring(3).uncapitalize()
        }
    }

    private static String findBuilderPseudoClass(String name, args) {
        if (name.endsWith("Builder") && name.size() > 7 && args.length == 0) {
            name.substring(0, name.length() - 7)
        }
    }

    private static String findGroovyBuilderPseudoClass(String name, args) {
        if (name.startsWith("build") && name.size() > 5 && args.length == 1 && args[0] instanceof Closure) {
            name.substring(5).uncapitalize()
        }
    }

    private static createBuilder(projectFilePseudoClass, delegate) {
        ProjectFileExtension extension = delegate.getFileSystem().getData()
                .getProjectFileExtensionByPseudoClass(projectFilePseudoClass)
        if (!extension) {
            throw new AfsException("No extension found for project file pseudo class '"
                    + projectFilePseudoClass + "'");
        }
        delegate.fileBuilder(extension.getProjectFileBuilderClass())
    }

    static Object methodMissing(ProjectFolder self, String name, Object args) {
        String projectFilePseudoClass;
        if ((projectFilePseudoClass = findBuilderPseudoClass(name, args))) {
            createBuilder(projectFilePseudoClass, self)
        } else if ((projectFilePseudoClass = findGroovyBuilderPseudoClass(name, args))) {
            def closure = args[0]

            def builder = createBuilder(projectFilePseudoClass, self)

            def cloned = closure.clone()
            BuilderSpec spec = new BuilderSpec(builder)
            cloned.delegate = spec
            cloned()

            builder.build();
        } else if ((projectFilePseudoClass = findGetChildPseudoClass(name, args))) {
            ProjectFileExtension extension = self.getProject().getFileSystem().getData()
                    .getProjectFileExtensionByPseudoClass(projectFilePseudoClass)
            if (extension) {
                self.invokeMethod("getChild", args)
            }
        }
    }

    static Object methodMissing(Folder self, String name, Object args) {
        String filePseudoClass = findGetChildPseudoClass(name, args)
        if (filePseudoClass) {
            FileExtension extension = self.getFileSystem().getData().getFileExtensionByPseudoClass(filePseudoClass)
            if (extension) {
                self.invokeMethod("getChild", args)
            }
        }
    }
}
