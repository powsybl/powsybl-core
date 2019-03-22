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

import static com.powsybl.action.util.Scalable.ScalingConvention.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
class LoadScalable extends AbstractScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    private final String id;

    private final double minValue;

    private final double maxValue;

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
        }
        this.minValue = minValue;
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
     * Default value is Double.MAX_VALUE
     */
    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        return scalingConvention == LOAD ? maxValue : -minValue;
    }


    /**
     * Set a minimum value of active power for the LoadScalable
     * Default value is 0
     */
    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        return scalingConvention == LOAD ? minValue : -maxValue;

    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        Load load = n.getLoad(id);
        if (load != null) {
            injections.add(load);
        } else if (notFoundInjections != null) {
            notFoundInjections.add(id);
        }
    }


    /**
     * {@inheritDoc}
     *
     * If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely
     * If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely
     */
    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

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
        if (oldP0 < minimumValue(n, LOAD) || oldP0 > this.maximumValue(n, LOAD)) {
            throw new PowsyblException("Error scaling LoadScalable " + id +
                    " : Initial P is not in the range [Pmin, Pmax]");
        }

        // We use natural load convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableDown = oldP0 - minimumValue(n, LOAD);
        double availableUp = maximumValue(n, LOAD) - oldP0;

        if (scalingConvention == LOAD) {
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
