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

    public static final double DEFAULT_MIN_Q_VALUE = -Double.MAX_VALUE;
    public static final double DEFAULT_MAX_Q_VALUE = Double.MAX_VALUE;

    private double minQValue = DEFAULT_MIN_Q_VALUE;
    private double maxQValue = DEFAULT_MAX_Q_VALUE;

    protected LoadScalable(String id) {
        super(id, 0., Double.MAX_VALUE);
    }

    protected LoadScalable(String id, double maxValue) {
        super(id, 0., maxValue);
    }

    protected LoadScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
    }

    protected LoadScalable(String id, double minPValue, double maxPValue, double minQValue, double maxQValue) {
        super(id, minPValue, maxPValue);
        this.minQValue = minQValue;
        this.maxQValue = maxQValue;
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
     * <p>
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
     * <p>
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
            double newP = getP0(l);

            // Step 1: proportional scaling (constant power factor)
            double newQ = newP * oldQ0 / oldP0;

            // Step 2: apply loadMinPowerFactor
            // This caps |newQ| at |newP| * tan(acos(minPF)), same as, |newP| * sqrt(1/minPF² - 1)
            // When the initial PF is already >= minPF, the proportional newQ is within the cap and no clamping occurs
            // When the initial PF < minPF, newQ is clamped so the resulting PF never drops below minPF and the sign of Q is preserved
            double minPowerFactor = parameters.getLoadMinPowerFactor();
            if (minPowerFactor > 0.0 && newP != 0.0) {
                double maxAbsQ = Math.abs(newP) * Math.sqrt(1.0 / (minPowerFactor * minPowerFactor) - 1.0);
                if (Math.abs(newQ) > maxAbsQ) {
                    newQ = Math.signum(newQ) * maxAbsQ;
                }
            }

            // Step 3: apply rate limits relative to the initial Q (from ScalingParameters)
            // Q_scaled must stay in [Q_initial * minQRate, Q_initial * maxQRate]
            if (oldQ0 != 0) {
                newQ = getNewQClamped(parameters, newQ, oldQ0);
            }

            // Step 4: apply absolute MVAr limits (per-load, set on LoadScalable directly)
            if (minQValue != DEFAULT_MIN_Q_VALUE) {
                newQ = Math.max(newQ, minQValue);
            }
            if (maxQValue != DEFAULT_MAX_Q_VALUE) {
                newQ = Math.min(newQ, maxQValue);
            }

            setQ0(l, newQ);
            LOGGER.info("Change reactive power setpoint of {} from {} to {} ",
                    l.getId(), oldQ0, newQ);
        }

        return done;
    }

    private static double getNewQClamped(ScalingParameters parameters, double newQ, double oldQ0) {
        double newQClamped = newQ;

        if (parameters.getLoadMinQRate() != ScalingParameters.DEFAULT_LOAD_MIN_Q_RATE) {
            double minBound = oldQ0 * parameters.getLoadMinQRate();
            if (oldQ0 > 0) {
                newQClamped = Math.max(newQClamped, minBound);
            } else {
                newQClamped = Math.min(newQClamped, minBound);
            }
        }

        if (parameters.getLoadMaxQRate() != ScalingParameters.DEFAULT_LOAD_MAX_Q_RATE) {
            double maxBound = oldQ0 * parameters.getLoadMaxQRate();
            if (oldQ0 > 0) {
                newQClamped = Math.min(newQClamped, maxBound);
            } else {
                newQClamped = Math.max(newQClamped, maxBound);
            }
        }
        return newQClamped;
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

    /**
     * Returns the absolute minimum reactive power limit (MVAr) applied to this load during scaling
     * <p>
     * This is the per-load floor applied in Q scaling, after all rate-based and
     * power-factor constraints have been evaluated. It is independent of {@link ScalingParameters}
     *
     * @return the minimum Q value in MVAr (default: {@link #DEFAULT_MIN_Q_VALUE})
     */
    public double getMinQ() {
        return minQValue;
    }

    /**
     * Sets the absolute minimum reactive power limit (MVAr) for this load during scaling
     *
     * @param minQValue the minimum Q value in MVAr
     */
    public LoadScalable setMinQ(double minQValue) {
        this.minQValue = minQValue;
        return this;
    }

    /**
     * Returns the absolute maximum reactive power limit (MVAr) applied to this load during scaling
     * <p>
     * This is the per-load ceiling applied in Q scaling, after all rate-based and
     * power-factor constraints have been evaluated. It is independent of {@link ScalingParameters}.
     *
     * @return the maximum Q value in MVAr (default: {@link #DEFAULT_MAX_Q_VALUE})
     */
    public double getMaxQ() {
        return maxQValue;
    }

    /**
     * Sets the absolute maximum reactive power limit (MVAr) for this load during scaling
     *
     * @param maxQValue the maximum Q value in MVAr
     */
    public LoadScalable setMaxQ(double maxQValue) {
        this.maxQValue = maxQValue;
        return this;
    }
}
