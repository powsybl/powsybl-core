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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Ameni Walha <ameni.walha at rte-france.com>
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
            return scalingConvention == GENERATOR ? Math.min(g.getMaxP(), maxValue) : -Math.max(g.getMinP(), minValue);
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
            return scalingConvention == GENERATOR ? Math.max(g.getMinP(), minValue) : -Math.min(g.getMaxP(), maxValue);
        } else {
            return 0;
        }
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
     * If scalingConvention is GENERATOR, the generator active power increases for positive "asked" and decreases inversely
     * If scalingConvention is LOAD, the generator active power decreases for positive "asked" and increases inversely
     */
    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        Generator g = n.getGenerator(id);
        double done = 0;
        if (g == null) {
            LOGGER.warn("Generator {} not found", id);
            return done;
        }

        Terminal t = g.getTerminal();
        if (!t.isConnected()) {
            GeneratorUtil.connectGenerator(g);
            LOGGER.info("Connecting {}", g.getId());
        }

        double oldTargetP = g.getTargetP();
        if (oldTargetP < this.minimumValue(n) || oldTargetP > this.maximumValue(n)) {
            throw new PowsyblException("Error scaling GeneratorScalable " + id +
                    " : Initial P is not in the range [Pmin, Pmax]");
        }

        // We use natural generator convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableUp = maximumValue(n, GENERATOR) - oldTargetP;
        double availableDown = oldTargetP - minimumValue(n, GENERATOR);

        if (scalingConvention == GENERATOR) {
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
}
