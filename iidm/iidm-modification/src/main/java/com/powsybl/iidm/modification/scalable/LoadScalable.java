/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.*;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
class LoadScalable extends AbstractInjectionScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    LoadScalable(String id) {
        super(id, 0., Double.MAX_VALUE);
    }

    LoadScalable(String id, double maxValue) {
        super(id, 0., maxValue);
    }

    LoadScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
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
     * {@inheritDoc}
     *
     * Default value is Double.MAX_VALUE for LoadScalable
     */
    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Load l = n.getLoad(id);
        if (l != null) {
            return scalingConvention == LOAD ? maxValue : -minValue;
        } else {
            return 0;
        }
    }


    /**
     * {@inheritDoc}
     *
     * Default value is 0 for LoadScalable
     */
    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Load l = n.getLoad(id);
        if (l != null) {
            return scalingConvention == LOAD ? minValue : -maxValue;
        } else {
            return 0;
        }

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
     * <ul>
     * <li>If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely.</li>
     * <li>If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely.</li>
     * </ul>
     */
    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(parameters);

        Load l = n.getLoad(id);

        double done = 0;
        if (l == null) {
            LOGGER.warn("Load {} not found", id);
            return done;
        }

        Terminal t = l.getTerminal();
        if (!t.isConnected()) {
            if (parameters.isReconnect()) {
                t.connect();
                LOGGER.info("Connecting {}", l.getId());
            } else {
                LOGGER.info("Load {} is not connected, discarded from scaling", l.getId());
                return 0.;
            }
        }

        double oldP0 = l.getP0();
        double oldQ0 = l.getQ0();
        if (oldP0 < minValue || oldP0 > maxValue) {
            LOGGER.error("Error scaling LoadScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use natural load convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableDown = oldP0 - minValue;
        double availableUp = maxValue - oldP0;

        if (parameters.getScalingConvention() == LOAD) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            l.setP0(oldP0 + done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            l.setP0(oldP0 - done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                l.getId(), oldP0, l.getP0());

        if (parameters.isConstantPowerFactor() && oldP0 != 0) {
            l.setQ0(l.getP0() * oldQ0 / oldP0);
            LOGGER.info("Change reactive power setpoint of {} from {} to {} ",
                    l.getId(), oldQ0, l.getQ0());
        }

        return done;
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        Load load = network.getLoad(id);
        if (load == null) {
            LOGGER.warn("Load {} not found", id);
            return 0.0;
        } else {
            return scalingConvention == LOAD ? load.getP0() : -load.getP0();
        }
    }
}
