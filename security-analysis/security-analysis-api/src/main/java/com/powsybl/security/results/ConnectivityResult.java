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

    private final double disconnectedLoadActivePower;

    private final double disconnectedGenerationActivePower;

    private final Set<String> disconnectedElements;

    public ConnectivityResult(int createdSynchronousComponentCount, int createdConnectedComponentCount, double disconnectedLoadActivePower,
                              double disconnectedGenerationActivePower, Set<String> lostElements) {
        this.createdSynchronousComponentCount = createdSynchronousComponentCount;
        this.createdConnectedComponentCount = createdConnectedComponentCount;
        this.disconnectedLoadActivePower = disconnectedLoadActivePower;
        this.disconnectedGenerationActivePower = disconnectedGenerationActivePower;
        this.disconnectedElements = lostElements;
    }

    public int getCreatedSynchronousComponentCount() {
        return createdSynchronousComponentCount;
    }

    public int getCreatedConnectedComponentCount() {
        return createdConnectedComponentCount;
    }

    public double getDisconnectedLoadActivePower() {
        return disconnectedLoadActivePower;
    }

    public double getDisconnectedGenerationActivePower() {
        return disconnectedGenerationActivePower;
    }

    public Set<String> getDisconnectedElements() {
        return disconnectedElements;
    }
}
