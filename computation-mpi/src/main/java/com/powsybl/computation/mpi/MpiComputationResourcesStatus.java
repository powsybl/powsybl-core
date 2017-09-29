/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.google.common.collect.ImmutableMap;
import com.powsybl.computation.ComputationResourcesStatus;
import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MpiComputationResourcesStatus implements ComputationResourcesStatus {

    private final DateTime date;

    private int availableCores = 0;

    private int busyCores = 0;

    MpiComputationResourcesStatus(MpiResources resources) {
        date = new DateTime();
        availableCores = resources.getAvailableCores();
        busyCores = resources.getBusyCores();
    }

    @Override
    public DateTime getDate() {
        return date;
    }

    @Override
    public int getAvailableCores() {
        return availableCores;
    }

    @Override
    public int getBusyCores() {
        return busyCores;
    }

    @Override
    public Map<String, Integer> getBusyCoresPerApp() {
        return ImmutableMap.of("all", busyCores);
    }

}
