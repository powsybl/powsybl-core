/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

import static com.powsybl.action.util.Scalable.ScalingConvention.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class DanglingLineScalable extends AbstractInjectionScalable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DanglingLineScalable.class);

    DanglingLineScalable(String id) {
        super(id, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    DanglingLineScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            dl.setP0(0);
        }
    }

    /**
     * {@inheritDoc}
     *
     * There is no default value for the maximum value.
     */
    @Override
    public double maximumValue(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == LOAD ? maxValue : -minValue;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     *
     * There is no default value for the minimum value.
     */
    @Override
    public double minimumValue(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == LOAD ? minValue : -maxValue;
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
     *
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
        if (oldP0 < minValue || oldP0 > maxValue) {
            LOGGER.error("Error scaling DanglingLineScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use natural load convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableDown = oldP0 - minValue;
        double availableUp = maxValue - oldP0;

        if (scalingConvention == LOAD) {
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
}
