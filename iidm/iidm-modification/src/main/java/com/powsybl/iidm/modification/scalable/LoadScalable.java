/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
class LoadScalable extends AbstractInjectionScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    LoadScalable(String id) {
        super(id, 0, Double.MAX_VALUE, LOAD);
    }

    LoadScalable(String id, double maxValue) {
        super(id, 0., Math.abs(maxValue), LOAD);
    }

    LoadScalable(String id, double minValue, double maxValue, ScalingConvention scalingConvention) {
        super(id, minValue, maxValue, scalingConvention);
    }

    /**
     * {@inheritDoc}
     *
     * Default value is Double.MAX_VALUE for LoadScalable
     */
    @Override
    public double getMaximumInjection(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Load l = n.getLoad(id);
        if (l != null) {
            return scalingConvention == GENERATOR ? maxInjection : -minInjection;
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
    public double getMinimumInjection(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Load l = n.getLoad(id);
        if (l != null) {
            return scalingConvention == GENERATOR ? minInjection : -maxInjection;
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

    private double scale(Network n, double asked, ScalingConvention scalingConvention, boolean constantPowerFactor) {
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
        double oldQ0 = l.getQ0();
        if (-oldP0 < minInjection || -oldP0 > maxInjection) {
            LOGGER.error("Error scaling LoadScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use generator convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableUp = maxInjection + oldP0;
        double availableDown = -oldP0 - minInjection;

        if (scalingConvention == GENERATOR) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            l.setP0(oldP0 - done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            l.setP0(oldP0 + done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                l.getId(), oldP0, l.getP0());

        if (constantPowerFactor) {
            l.setQ0(l.getP0() * oldQ0 / oldP0);
            LOGGER.info("Change reactive power setpoint of {} from {} to {} ",
                    l.getId(), oldQ0, l.getQ0());
        }

        return done;
    }

    /**
     * {@inheritDoc}
     *
     * If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely
     * If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely
     */
    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        return scale(n, asked, scalingConvention, false);
    }

    @Override
    public double scaleWithConstantPowerFactor(Network n, double asked, ScalingConvention scalingConvention) {
        return scale(n, asked, scalingConvention, true);
    }

    @Override
    public double getCurrentInjection(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);

        Injection injection = getInjectionOrNull(n);
        if (injection == null) {
            return 0;
        }
        if (injection instanceof Load) {
            double loadP = !Double.isNaN(((Load) injection).getP0()) ? ((Load) injection).getP0() : 0;
            return scalingConvention.equals(LOAD) ? loadP : -loadP;
        } else {
            throw new PowsyblException("Load scalable was not defined on a load.");
        }
    }
}
