/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.iidm.network.Network;

/**
 * Common interface for project files able to provide a Network.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ProjectCase {

    String queryNetwork(ScriptType scriptType, String scriptContent);

    Network getNetwork();

    void invalidateNetworkCache();

    void addListener(ProjectCaseListener l);

    void removeListener(ProjectCaseListener l);
}
