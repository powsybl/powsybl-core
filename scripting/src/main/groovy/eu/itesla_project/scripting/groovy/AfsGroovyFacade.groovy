/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.scripting.groovy

import eu.itesla_project.afs.core.AppData
import eu.itesla_project.afs.core.Node

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class AfsGroovyFacade {

    private final AppData data

    AfsGroovyFacade(AppData data) {
        assert data
        this.data = data
    }

    Node getNode(String path) {
        data.getNode(path)
    }
}
