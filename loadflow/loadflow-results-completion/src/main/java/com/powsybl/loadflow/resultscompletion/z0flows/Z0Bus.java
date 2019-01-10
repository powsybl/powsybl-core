/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;

public class Z0Bus {

    private List<Bus> z0Bus;

    public Z0Bus() {
        z0Bus = new ArrayList<Bus>();
    }

    public boolean busInZ0Bus(Bus bus) {
        return z0Bus.contains(bus);
    }

    public void addBus(Bus bus) {
        if (!z0Bus.contains(bus)) {
            z0Bus.add(bus);
        }
    }

    public int size() {
        return z0Bus.size();
    }

    public int indexOf(Bus bus) {
        return z0Bus.indexOf(bus);
    }

    public Bus getBus(int pos) {
        return z0Bus.get(pos);
    }

    public void print() {
        LOG.info(z0Bus.stream().map(b -> b.getId()).collect(Collectors.joining(",")));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Z0Bus.class);
}
