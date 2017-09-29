/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.powsybl.computation.ComputationResourcesStatus;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LocalComputationResourcesStatus implements ComputationResourcesStatus {

    private final int availableCores;

    private DateTime date;

    private int busyCores = 0;

    LocalComputationResourcesStatus(int availableCores) {
        this.availableCores = availableCores;
    }

    @Override
    public synchronized DateTime getDate() {
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
        date = new DateTime();
        busyCores++;
    }

    synchronized void decrementNumberOfBusyCores() {
        date = new DateTime();
        busyCores--;
    }

}
