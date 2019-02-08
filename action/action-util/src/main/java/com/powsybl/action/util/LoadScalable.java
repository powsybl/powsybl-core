/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public class LoadScalable extends AbstractScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    private final String id;

    private double maxValue = Double.POSITIVE_INFINITY;

    public LoadScalable(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public LoadScalable(String id, double maxValue) {
        this.id = Objects.requireNonNull(id);
        this.maxValue = maxValue;
    }

    @Override
    public double initialValue(Network n) {
        Objects.requireNonNull(n);
        Load l = n.getLoad(id);
        return l != null && !Double.isNaN(l.getTerminal().getP()) ? l.getTerminal().getP() : 0;

    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        Load l = n.getLoad(id);
        if (l != null) {
            l.setP0(0);
        }
    }

    /**
     * Set a maximum value of active power for the LoadScalable
     * <p> by default Double.POSITIVE_INFINITY
     */
    @Override
    public double maximumValue(Network n) {
        return maxValue;
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        Load load = n.getLoad(id);
        if (load != null) {
            injections.add(load);
        } else {
            if (notFoundInjections != null) {
                notFoundInjections.add(id);
            }
        }
    }

    @Override
    public double scale(Network n, double asked) {
        //asked and done follow the Generator convention
        //if asked > 0, P0 decreases and conversely
        Objects.requireNonNull(n);

        Load l = n.getLoad(id);
        double done = 0;
        if (l != null) {
            Terminal t = l.getTerminal();
            if (!t.isConnected()) {
                t.connect();
                LOGGER.info("Connecting {}", l.getId());
            }
            done = Math.max(asked, l.getP0() - maximumValue(n));
            double oldP0 = l.getP0();
            l.setP0(l.getP0() - done);
            LOGGER.info("Change active power setpoint of {} from {} to {} ",
                    l.getId(), oldP0, l.getP0());
        } else {
            LOGGER.warn("Load {} not found", id);
        }
        return done;


    }


}
