package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFileCreationContext;

public abstract class AbstractModificationScript extends AbstractScript {

    public AbstractModificationScript(ProjectFileCreationContext context, int codeVersion, String scriptContentName) {
        super(context, codeVersion, scriptContentName);
    }

}
