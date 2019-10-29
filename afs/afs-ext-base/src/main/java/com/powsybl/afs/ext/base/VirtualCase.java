/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.DependencyCache;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCase extends ProjectFile implements ProjectCase {

    public static final String PSEUDO_CLASS = "virtualCase";
    public static final int VERSION = 0;

    static final String CASE_DEPENDENCY_NAME = "case";
    static final String SCRIPT_DEPENDENCY_NAME = "script";

    private final DependencyCache<ProjectFile> projectCaseDependency = new DependencyCache<>(this, CASE_DEPENDENCY_NAME, ProjectFile.class);

    private final DependencyCache<ModificationScript> modificationScriptDependency = new DependencyCache<>(this, SCRIPT_DEPENDENCY_NAME, ModificationScript.class);

    public VirtualCase(ProjectFileCreationContext context) {
        super(context, VERSION);
    }

    public Optional<ProjectFile> getCase() {
        return projectCaseDependency.getFirst();
    }

    public void setCase(ProjectFile aCase) {
        Objects.requireNonNull(aCase);
        setDependencies(CASE_DEPENDENCY_NAME, Collections.singletonList(aCase));
        projectCaseDependency.invalidate();
    }

    public Optional<ModificationScript> getScript() {
        return modificationScriptDependency.getFirst();
    }

    public void setScript(ModificationScript aScript) {
        Objects.requireNonNull(aScript);
        setDependencies(SCRIPT_DEPENDENCY_NAME, Collections.singletonList(aScript));
        modificationScriptDependency.invalidate();
    }

    @Override
    public String queryNetwork(ScriptType scriptType, String scriptContent) {
        Objects.requireNonNull(scriptType);
        Objects.requireNonNull(scriptContent);
        return findService(NetworkCacheService.class).queryNetwork(this, scriptType, scriptContent);
    }

    @Override
    public Network getNetwork() {
        return findService(NetworkCacheService.class).getNetwork(this);
    }

    @Override
    public void invalidateNetworkCache() {
        findService(NetworkCacheService.class).invalidateCache(this);
    }

    static AfsException createScriptLinkIsDeadException() {
        return new AfsException("Script link is dead");
    }

    @Override
    public void addListener(ProjectCaseListener l) {
        findService(NetworkCacheService.class).addListener(this, l);
    }

    @Override
    public void removeListener(ProjectCaseListener l) {
        findService(NetworkCacheService.class).removeListener(this, l);
    }

    @Override
    protected void invalidate() {
        // invalidate network cache
        findService(NetworkCacheService.class).invalidateCache(this);

        super.invalidate();
    }

    @Override
    public boolean mandatoryDependenciesAreMissing() {
        return !getCase().isPresent() || !getScript().isPresent();
    }
}
