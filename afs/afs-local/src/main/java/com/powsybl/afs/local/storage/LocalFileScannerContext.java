/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.computation.ComputationManager;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalFileScannerContext {

    private final ComputationManager computationManager;

    public LocalFileScannerContext(ComputationManager computationManager) {
        this.computationManager = computationManager;
    }

    public ComputationManager getComputationManager() {
        return computationManager;
    }
}
