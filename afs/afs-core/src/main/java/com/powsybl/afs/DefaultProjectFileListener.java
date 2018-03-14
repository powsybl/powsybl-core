/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultProjectFileListener implements ProjectFileListener {

    @Override
    public void dependencyChanged(String name) {
        // to implement
    }

    @Override
    public void backwardDependencyChanged(String name) {
        // to implement
    }

}
