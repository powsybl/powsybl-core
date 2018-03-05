/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ProjectFileExtension;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class ActionScriptExtension implements ProjectFileExtension<ActionScript, ActionScriptBuilder> {

    @Override
    public Class<ActionScript> getProjectFileClass() {
        return ActionScript.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return ActionScript.PSEUDO_CLASS;
    }

    @Override
    public Class<ActionScriptBuilder> getProjectFileBuilderClass() {
        return ActionScriptBuilder.class;
    }

    @Override
    public ActionScript createProjectFile(ProjectFileCreationContext context) {
        return new ActionScript(context);
    }

    @Override
    public ActionScriptBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new ActionScriptBuilder(context);
    }
}
