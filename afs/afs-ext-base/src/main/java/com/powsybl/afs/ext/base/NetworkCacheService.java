/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFile;
import com.powsybl.iidm.network.Network;

/**
 * Provides caching capabilities for loaded {@code Network} objects.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NetworkCacheService {

    <T extends ProjectFile & ProjectCase> Network getNetwork(T projectCase);

    <T extends ProjectFile & ProjectCase> String queryNetwork(T projectCase, ScriptType scriptType, String scriptContent);

    <T extends ProjectFile & ProjectCase> void invalidateCache(T projectCase);

    <T extends ProjectFile & ProjectCase> void addListener(T projectCase, ProjectCaseListener listener);

    <T extends ProjectFile & ProjectCase> void removeListener(T projectCase, ProjectCaseListener listener);
}
