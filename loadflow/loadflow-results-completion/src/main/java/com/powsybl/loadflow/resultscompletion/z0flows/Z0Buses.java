/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.List;

import com.powsybl.iidm.network.Bus;

public class Z0Buses {

    private List<Z0Bus> z0Buses;

    public Z0Buses() {
        z0Buses = new ArrayList<Z0Bus>();
    }

    public boolean busInZ0Buses(Bus bus) {
        for (Z0Bus z0Bus : z0Buses) {
            if (z0Bus.busInZ0Bus(bus)) {
                return true;
            }
        }

        return false;
    }

    public void addZ0Bus(Z0Bus z0Bus) {
        z0Buses.add(z0Bus);
    }

    public Z0Bus getZ0Bus(int pos) {
        return z0Buses.get(pos);
    }

    public void print() {
        z0Buses.forEach(z0Bus -> {
            z0Bus.print();
        });
    }

    public int getSize() {
        return z0Buses.size();
    }
}
