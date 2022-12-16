/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.GENERATOR;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class DanglingLineScalable extends AbstractInjectionScalable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DanglingLineScalable.class);

    DanglingLineScalable(String id) {
        this(id, -Double.MAX_VALUE, Double.MAX_VALUE, GENERATOR);
    }

    DanglingLineScalable(String id, double minInjection, double maxInjection, ScalingConvention scalingConvention) {
        super(id, minInjection, maxInjection, scalingConvention);
    }

    /**
     * {@inheritDoc}
     * <p>
     * There is no default value for the maximum value.
     */
    @Override
    public double getMaximumInjection(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == GENERATOR ? maxInjection : -minInjection;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * There is no default value for the minimum value.
     */
    @Override
    public double getMinimumInjection(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == GENERATOR ? minInjection : -maxInjection;
        } else {
            return 0;
        }
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            injections.add(dl);
        } else if (notFoundInjections != null) {
            notFoundInjections.add(id);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely
     * If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely
     */
    @Override
    public double scale(Network n, double asked, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);

        double done = 0;
        if (dl == null) {
            LOGGER.warn("Dangling line {} not found", id);
            return done;
        }

        Terminal t = dl.getTerminal();
        if (!t.isConnected()) {
            t.connect();
            LOGGER.info("Connecting {}", dl.getId());
        }

        double oldP0 = dl.getP0();
        if (oldP0 < minInjection || oldP0 > maxInjection) {
            LOGGER.error("Error scaling DanglingLineScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use natural generator convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableUp = maxInjection - oldP0;
        double availableDown = oldP0 - minInjection;

        if (scalingConvention == GENERATOR) {
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            dl.setP0(oldP0 + done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            dl.setP0(oldP0 - done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                dl.getId(), oldP0, dl.getP0());

        return done;
    }

    @Override
    public double getCurrentInjection(Network n, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);

        Injection injection = getInjectionOrNull(n);
        if (injection == null) {
            return 0;
        }
        if (injection instanceof DanglingLine) {
            double danglingLineP0 = !Double.isNaN(((DanglingLine) injection).getP0()) ? ((DanglingLine) injection).getP0() : 0;
            return scalingConvention.equals(GENERATOR) ? danglingLineP0 : -danglingLineP0;
        } else {
            throw new PowsyblException("Dangling line scalable was not defined on a dangling line.");
        }
    }
}
