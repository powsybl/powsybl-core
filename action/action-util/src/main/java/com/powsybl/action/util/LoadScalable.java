/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
class LoadScalable extends AbstractScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    private final String id;

    private final double maxValue;

    private final double minValue;


    LoadScalable(String id) {
        this(id, 0., Double.MAX_VALUE);

    }

    LoadScalable(String id, double maxValue) {
        this(id, 0., maxValue);
    }

    LoadScalable(String id, double minValue, double maxValue) {
        this.id = Objects.requireNonNull(id);
        if (maxValue < minValue) {
            throw new PowsyblException("Error creating LoadScalable " + id
                    + " : maxValue should be bigger than minValue");
        } else {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

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
     * <p> by default Double.MAX_VALUE
     */
    @Override
    public double maximumValue(Network n, ScalingPowerConvention scalingConvention) {
        return ScalingPowerConvention.LOAD.equals(scalingConvention) ? maxValue : -minValue;
    }


    /**
     * Set a minimum value of active power for the LoadScalable
     * <p> by default zero
     */
    @Override
    public double minimumValue(Network n, ScalingPowerConvention scalingConvention) {
        return ScalingPowerConvention.LOAD.equals(scalingConvention) ? minValue : -maxValue;

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


    /**
     * @param n                 network
     * @param asked             value asked to adjust the scalable active power
     * @param scalingConvention
     * If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely
     * If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely
     * @return actual value of adjusted active power in accordance with the scaling convention
     */
    @Override
    public double scale(Network n, double asked, ScalingPowerConvention scalingConvention) {

        Objects.requireNonNull(n);
        Load l = n.getLoad(id);

        double done = 0;
        if (l == null) {
            LOGGER.warn("Load {} not found", id);
            return done;
        }

        Terminal t = l.getTerminal();
        if (!t.isConnected()) {
            t.connect();
            LOGGER.info("Connecting {}", l.getId());
        }

        double oldP0 = l.getP0();
        if (oldP0 < this.minimumValue(n, ScalingPowerConvention.LOAD) || oldP0 > this.maximumValue(n, ScalingPowerConvention.LOAD)) {
            throw new PowsyblException("Error scaling LoadScalable " + id +
                    " : Initial P is not in the range [Pmin, Pmax]");
        }

        double availableDown = oldP0 - minimumValue(n, ScalingPowerConvention.LOAD);
        double availableUp = maximumValue(n, ScalingPowerConvention.LOAD) - oldP0;

        if (ScalingPowerConvention.LOAD.equals(scalingConvention)) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            l.setP0(oldP0 + done);
        } else {

            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            l.setP0(oldP0 - done);

        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                l.getId(), oldP0, l.getP0());

        return done;
    }
}
