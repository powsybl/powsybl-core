/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.afs;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.dsl.ActionDslLoader;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ext.base.AbstractModificationScript;
import com.powsybl.afs.ext.base.ScriptType;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionScript extends AbstractModificationScript implements ContingenciesProvider {

    public static final String PSEUDO_CLASS = "actionScript";
    public static final int VERSION = 0;

    static final String SCRIPT_CONTENT = "scriptContent";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("lang.ActionScript");

    public ActionScript(ProjectFileCreationContext context) {
        super(context, VERSION, SCRIPT_CONTENT);
    }

    @Override
    public String getScriptLabel() {
        return RESOURCE_BUNDLE.getString("Actions");
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.GROOVY;
    }

    public ActionDb load(Network network) {
        Objects.requireNonNull(network);
        return new ActionDslLoader(readScript()).load(network);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return new ArrayList<>(load(network).getContingencies());
    }
}
