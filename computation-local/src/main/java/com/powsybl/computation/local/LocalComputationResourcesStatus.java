/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import com.powsybl.computation.ComputationResourcesStatus;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LocalComputationResourcesStatus implements ComputationResourcesStatus {

    private final int availableCores;

    private ZonedDateTime date;

    private int busyCores = 0;

    LocalComputationResourcesStatus(int availableCores) {
        this.availableCores = availableCores;
    }

    @Override
    public synchronized ZonedDateTime getDate() {
        return date;
    }

    @Override
    public int getAvailableCores() {
        return availableCores;
    }

    @Override
    public synchronized int getBusyCores() {
        return busyCores;
    }

    @Override
    public synchronized Map<String, Integer> getBusyCoresPerApp() {
        return Collections.singletonMap("all", busyCores);
    }

    synchronized void incrementNumberOfBusyCores() {
        date = ZonedDateTime.now(ZoneOffset.UTC);
        busyCores++;
    }

    synchronized void decrementNumberOfBusyCores() {
        date = ZonedDateTime.now(ZoneOffset.UTC);
        busyCores--;
    }

}
