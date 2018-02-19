/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.*;
import com.powsybl.iidm.network.Network;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCase extends ProjectFile implements ProjectCase, RunnableScript, DependencyListener {

    public static final String PSEUDO_CLASS = "virtualCase";
    public static final int VERSION = 0;

    private static final FileIcon VIRTUAL_CASE_ICON = new FileIcon("virtualCase", VirtualCase.class.getResourceAsStream("/icons/virtualCase16x16.png"));

    static final String CASE_DEPENDENCY_NAME = "case";
    static final String SCRIPT_DEPENDENCY_NAME = "script";

    private final DependencyCache<ProjectCase> projectCaseDependency = new DependencyCache<>(this, CASE_DEPENDENCY_NAME, ProjectCase.class);

    private final DependencyCache<ModificationScript> modificationScriptDependency = new DependencyCache<>(this, SCRIPT_DEPENDENCY_NAME, ModificationScript.class);

    public VirtualCase(ProjectFileCreationContext context) {
        super(context, VERSION, VIRTUAL_CASE_ICON);
        addDependencyListener(this, this);
    }

    public Optional<ProjectCase> getCase() {
        return projectCaseDependency.getFirst();
    }

    public Optional<ModificationScript> getScript() {
        return modificationScriptDependency.getFirst();
    }

    @Override
    public String queryNetwork(String groovyScript) {
        return findService(NetworkService.class).queryNetwork(this, groovyScript);
    }

    @Override
    public Network getNetwork() {
        return findService(NetworkService.class).getNetwork(this);
    }

    @Override
    public ScriptError getScriptError() {
        return findService(NetworkService.class).getScriptError(this);
    }

    @Override
    public String getScriptOutput() {
        return findService(NetworkService.class).getScriptOutput(this);
    }

    static AfsException createScriptLinkIsDeadException() {
        return new AfsException("Script link is dead");
    }

    @Override
    public ScriptType getScriptType() {
        return getScript().orElseThrow(VirtualCase::createScriptLinkIsDeadException)
                          .getScriptType();
    }

    @Override
    public String readScript() {
        return getScript().orElseThrow(VirtualCase::createScriptLinkIsDeadException)
                          .readScript();
    }

    @Override
    public void writeScript(String content) {
        getScript().orElseThrow(VirtualCase::createScriptLinkIsDeadException)
                   .writeScript(content);
    }

    @Override
    public void addListener(ScriptListener listener) {
        getScript().orElseThrow(VirtualCase::createScriptLinkIsDeadException)
                   .addListener(listener);
    }

    @Override
    public void removeListener(ScriptListener listener) {
        getScript().orElseThrow(VirtualCase::createScriptLinkIsDeadException)
                   .removeListener(listener);
    }

    private void invalidateNetworkCache() {
        findService(NetworkService.class).invalidateCache(this);
    }

    @Override
    public void dependencyChanged() {
        invalidateNetworkCache();
    }

    @Override
    public void delete() {
        super.delete();

        // also clean cache
        invalidateNetworkCache();
    }
}
