/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
public class LoadScalable extends AbstractInjectionScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadScalable.class);

    protected LoadScalable(String id) {
        super(id, 0., Double.MAX_VALUE);
    }

    protected LoadScalable(String id, double maxValue) {
        super(id, 0., maxValue);
    }

    protected LoadScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        Load l = n.getLoad(id);
        if (l != null) {
            setP0(l, 0);
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

        if (parameters.getIgnoredInjectionIds().contains(id)) {
            LOGGER.info("Scaling parameters' injections to be ignored contains load {}, discarded from scaling", id);
            return 0;
        }

        Load l = n.getLoad(id);

        if (l == null) {
            LOGGER.warn("Load {} not found", id);
            return 0;
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

        return shiftLoad(asked, parameters, l);
    }

    private double shiftLoad(double asked, ScalingParameters parameters, Load l) {
        double oldP0 = getP0(l);
        double oldQ0 = getQ0(l);
        if (oldP0 < minValue || oldP0 > maxValue) {
            LOGGER.error("Error scaling LoadScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use natural load convention to compute the limits.
        // The actual convention is taken into account afterward.
        double availableDown = oldP0 - minValue;
        double availableUp = maxValue - oldP0;

        double done;
        if (parameters.getScalingConvention() == LOAD) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            setP0(l, oldP0 + done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            setP0(l, oldP0 - done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                l.getId(), oldP0, getP0(l));

        if (parameters.isConstantPowerFactor() && oldP0 != 0) {
            scaleReactivePower(parameters, l, oldQ0, oldP0, getP0(l));
        }

        return done;
    }

    private void scaleReactivePower(ScalingParameters parameters, Load l, double oldQ, double oldP, double newP) {
        double limitedQ = applyPowerFactorLimit(parameters, oldQ, oldP, newP);
        limitedQ = applyRelativeQRateLimits(parameters, oldQ, limitedQ);
        setQ0(l, limitedQ);
        LOGGER.info("Change reactive power setpoint of {} from {} to {} ", l.getId(), oldQ, limitedQ);
    }

    /**
     * Scales reactive power proportionally to the active power change, subject to a minimum power factor constraint.
     * <p>
     * If the absolute value of the initial power factor is below {@code loadMinPowerFactor}, the reactive power
     * is recalculated from {@code newP} and the minimum power factor instead of scaling proportionally.
     *
     * @param parameters scaling parameters
     * @param oldQ       initial reactive power
     * @param oldP       initial active power
     * @param newP       scaled active power
     * @return the new reactive power, respecting the minimum power factor constraint
     */
    private static double applyPowerFactorLimit(ScalingParameters parameters, double oldQ, double oldP, double newP) {
        double newQ = newP * oldQ / oldP;
        double minPowerFactor = parameters.getLoadMinPowerFactor();
        if (minPowerFactor == 0 || minPowerFactor < Math.cos(Math.atan(oldQ / oldP))) {
            return newQ;
        }
        return Math.copySign(Math.tan(Math.acos(minPowerFactor)) * newP, newQ);
    }

    /**
     * Limits scaled Q to avoid deviating too much from its initial value
     * If Q0 was positive, Q_scaled should be in [Q_initial * loadMinQRate, Q_initial * loadMaxQRate].
     * Otherwise, it should be in [Q_initial * loadMaxQRate, Q_initial * loadMinQRate].
     *
     * @param parameters scaling parameters
     * @param oldQ       initial reactive power
     * @param newQ       reactive power after power-factor limiting
     * @return the reactive power limited to the configured rate limits
     */
    private static double applyRelativeQRateLimits(ScalingParameters parameters, double oldQ, double newQ) {
        double limitedQ = newQ;
        if (parameters.getLoadMinQRate().isPresent()) {
            double minQ = oldQ * parameters.getLoadMinQRate().getAsDouble();
            limitedQ = oldQ >= 0 ? Math.max(limitedQ, minQ) : Math.min(limitedQ, minQ);
        }
        if (parameters.getLoadMaxQRate().isPresent()) {
            double maxQ = oldQ * parameters.getLoadMaxQRate().getAsDouble();
            limitedQ = oldQ >= 0 ? Math.min(limitedQ, maxQ) : Math.max(limitedQ, maxQ);
        }
        return limitedQ;
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        Load load = network.getLoad(id);
        if (load == null) {
            LOGGER.warn("Load {} not found", id);
            return 0.0;
        } else {
            return scalingConvention == LOAD ? getP0(load) : -getP0(load);
        }
    }

    protected void setP0(Load l, double value) {
        l.setP0(value);
    }

    protected double getP0(Load l) {
        return l.getP0();
    }

    protected void setQ0(Load l, double value) {
        l.setQ0(value);
    }

    protected double getQ0(Load l) {
        return l.getQ0();
    }
}
