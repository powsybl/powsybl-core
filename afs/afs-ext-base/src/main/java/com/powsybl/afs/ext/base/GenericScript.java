/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFileCreationContext;

/**
 * Default general purpose Groovy script
 *
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class GenericScript extends AbstractScript<GenericScript> {
    public static final String PSEUDO_CLASS = "genericScript";
    public static final int VERSION = 0;
    private static final String SCRIPT_CONTENT = "scriptContent";

    public GenericScript(ProjectFileCreationContext context) {
        super(context, VERSION, SCRIPT_CONTENT);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.GROOVY;
    }
}
