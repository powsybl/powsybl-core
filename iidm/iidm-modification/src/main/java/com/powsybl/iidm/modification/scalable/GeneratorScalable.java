/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.modification.ConnectGenerator;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
class GeneratorScalable extends AbstractInjectionScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorScalable.class);

    GeneratorScalable(String id) {
        super(id);
    }

    GeneratorScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        Generator g = n.getGenerator(id);
        if (g != null) {
            g.setTargetP(0);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Default value is generator maximum power for GeneratorScalable
     */
    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Generator g = n.getGenerator(id);
        if (g != null) {
            return scalingConvention == GENERATOR ? maximumTargetP(g) : -minimumTargetP(g);
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Default value is generator minimum power for GeneratorScalable
     */
    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Generator g = n.getGenerator(id);
        if (g != null) {
            return scalingConvention == GENERATOR ? minimumTargetP(g) : -maximumTargetP(g);
        } else {
            return 0;
        }
    }

    private double minimumTargetP(Generator gen) {
        return Math.max(gen.getMinP(), minValue);
    }

    private double maximumTargetP(Generator gen) {
        return Math.min(gen.getMaxP(), maxValue);
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        Generator generator = n.getGenerator(id);
        if (generator != null) {
            injections.add(generator);
        } else if (notFoundInjections != null) {
            notFoundInjections.add(id);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <ul>
     * <li>If scalingConvention is GENERATOR, the generator active power increases for positive "asked" and decreases inversely.</li>
     * <li>If scalingConvention is LOAD, the generator active power decreases for positive "asked" and increases inversely.</li>
     * </ul>
     */
    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(parameters);

        Generator g = n.getGenerator(id);
        double done = 0;
        if (g == null) {
            LOGGER.warn("Generator {} not found", id);
            return done;
        }

        Terminal t = g.getTerminal();
        if (!t.isConnected()) {
            if (parameters.isReconnect()) {
                if (!connectGeneratorToMainComponent(n, g)) {
                    // If the generator is still disconnected from main component, we should not change the active power setPoint
                    LOGGER.info("Generator {} could not be connected, discarded from scaling", g.getId());
                    return 0.;
                }

            } else {
                LOGGER.info("Generator {} is not connected, discarded from scaling", g.getId());
                return 0.;
            }
        }

        double oldTargetP = g.getTargetP();
        double minimumTargetP = minimumTargetP(g);
        double maximumTargetP = maximumTargetP(g);

        if (!parameters.isAllowsGeneratorOutOfActivePowerLimits() && (oldTargetP < minimumTargetP || oldTargetP > maximumTargetP)) {
            LOGGER.error("Error scaling GeneratorScalable {}: Initial P is not in the range [Pmin, Pmax], skipped", id);
            return 0.;
        }

        // We use natural generator convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableUp = maximumTargetP - oldTargetP;
        double availableDown = oldTargetP - minimumTargetP;

        if (parameters.getScalingConvention() == GENERATOR) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            g.setTargetP(oldTargetP + done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            g.setTargetP(oldTargetP - done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} (pmax={})",
                    g.getId(), oldTargetP, g.getTargetP(), g.getMaxP());

        return done;
    }

    private static boolean connectGeneratorToMainComponent(Network n, Generator g) {
        LOGGER.info("Connecting {}", g.getId());
        new ConnectGenerator(g.getId()).apply(n);
        return g.getTerminal().isConnected() && g.getTerminal().getBusBreakerView().getConnectableBus().isInMainConnectedComponent();
    }

    /**
     * Compute the percentage of asked power available for the scale. It takes into account the scaling convention
     * specified by the user and the sign of the asked power.
     *
     * @param network Network on which the scaling is done
     * @param asked Asked power (can be positive or negative)
     * @param scalingPercentage Percentage of the asked power that shall be distributed to the current injection
     * @param scalingConvention Scaling convention (GENERATOR or LOAD)
     * @return the percentage of asked power available for the scale on the current injection
     */
    double availablePowerInPercentageOfAsked(Network network, double asked, double scalingPercentage, ScalingConvention scalingConvention) {
        var generator = network.getGenerator(id);

        // In LOAD convention, a positive scale will imply a decrease of generators target power
        var askedPower = asked * scalingPercentage / 100;
        if (scalingConvention == LOAD) {
            askedPower = -askedPower;
        }

        if (askedPower >= 0) {
            var availablePower = Math.min(generator.getMaxP(), maxValue) - generator.getTargetP();
            return askedPower > availablePower ? availablePower / askedPower : 100.0;
        } else {
            var availablePower = Math.max(generator.getMinP(), minValue) - generator.getTargetP();
            return askedPower < availablePower ? availablePower / askedPower : 100.0;
        }
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        Generator generator = network.getGenerator(id);
        if (generator == null) {
            LOGGER.warn("Generator {} not found", id);
            return 0.0;
        } else {
            return scalingConvention == GENERATOR ? generator.getTargetP() : -generator.getTargetP();
        }
    }
}
