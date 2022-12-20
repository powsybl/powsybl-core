/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import java.util.Set;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class ConnectivityResult {

    private final int createdSynchronousComponentCount;

    private final int createdConnectedComponentCount;

    private final double loadActivePowerLoss;

    private final double generationActivePowerLoss;

    private final Set<String> lostElements;

    public ConnectivityResult(int createdSynchronousComponentCount, int createdConnectedComponentCount, double loadActivePowerLoss,
                              double generationActivePowerLoss, Set<String> lostElements) {
        this.createdSynchronousComponentCount = createdSynchronousComponentCount;
        this.createdConnectedComponentCount = createdConnectedComponentCount;
        this.loadActivePowerLoss = loadActivePowerLoss;
        this.generationActivePowerLoss = generationActivePowerLoss;
        this.lostElements = lostElements;
    }

    public int getCreatedSynchronousComponentCount() {
        return createdSynchronousComponentCount;
    }

    public int getCreatedConnectedComponentCount() {
        return createdConnectedComponentCount;
    }

    public double getLoadActivePowerLoss() {
        return loadActivePowerLoss;
    }

    public double getGenerationActivePowerLoss() {
        return generationActivePowerLoss;
    }

    public Set<String> getLostElements() {
        return lostElements;
    }
}
