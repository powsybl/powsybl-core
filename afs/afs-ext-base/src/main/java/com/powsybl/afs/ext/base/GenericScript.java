package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFileCreationContext;

/**
 * Default general purpose Groovy script
 */
public class GenericScript extends AbstractScript<GenericScript> {
    private static final String SCRIPT_CONTENT = "scriptContent";
    public static final String PSEUDO_CLASS = "modificationScript";
    public static final int VERSION = 0;

    public GenericScript(ProjectFileCreationContext context) {
        super(context, VERSION, SCRIPT_CONTENT);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.GROOVY;
    }
}
