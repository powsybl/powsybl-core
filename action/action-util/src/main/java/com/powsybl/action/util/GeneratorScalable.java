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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
class GeneratorScalable extends AbstractScalable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorScalable.class);

    private final String id;

    private final double maxValue;

    private final double minValue;


    GeneratorScalable(String id) {
        this(id, -Double.MAX_VALUE, Double.MAX_VALUE);
    }


    GeneratorScalable(String id, double minValue, double maxValue) {
        this.id = Objects.requireNonNull(id);
        if (maxValue < minValue) {
            throw new PowsyblException("Error creating GeneratorScalable " + id
                    + " : maxValue should be bigger than minValue");
        } else {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

    }


    @Override
    public double initialValue(Network n) {
        Objects.requireNonNull(n);

        Generator g = n.getGenerator(id);
        return g != null && !Double.isNaN(g.getTerminal().getP()) ? g.getTerminal().getP() : 0;
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        Generator g = n.getGenerator(id);
        if (g != null) {
            g.setTargetP(0);
        }
    }


    @Override
    public double maximumValue(Network n, ScalingPowerConvention scalingPowerConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingPowerConvention);

        Generator g = n.getGenerator(id);
        if (g != null) {
            return ScalingPowerConvention.GENERATOR.equals(scalingPowerConvention) ? Math.min(g.getMaxP(), maxValue) : -Math.max(g.getMinP(), minValue);
        } else {
            return 0;
        }

    }

    @Override
    public double minimumValue(Network n, ScalingPowerConvention scalingPowerConvention) {
        Objects.requireNonNull(n);

        Generator g = n.getGenerator(id);
        if (g != null) {
            return ScalingPowerConvention.GENERATOR.equals(scalingPowerConvention) ? Math.max(g.getMinP(), minValue) : -Math.min(g.getMaxP(), maxValue);
        } else {
            return 0;
        }

    }

    @Override
    public  void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        Generator generator = n.getGenerator(id);
        if (generator != null) {
            injections.add(generator);
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
     * If scalingConvention is GENERATOR, the generator active power increases for positive "asked" and decreases inversely
     * If scalingConvention is LOAD, the generator active power decreases for positive "asked" and increases inversely
     * @return actual value of adjusted active power in accordance with the scaling convention
     */
    @Override
    public double scale(Network n, double asked, ScalingPowerConvention scalingConvention) {
        Objects.requireNonNull(n);

        Generator g = n.getGenerator(id);
        double done = 0;
        if (g == null) {
            LOGGER.warn("Generator {} not found", id);
            return done;
        }

        Terminal t = g.getTerminal();
        if (!t.isConnected()) {
            connectGenerator(g);
        }

        double oldTargetP = g.getTargetP();
        if (oldTargetP < this.minimumValue(n) || oldTargetP > this.maximumValue(n)) {
            throw new PowsyblException("Error scaling GeneratorScalable " + id +
                    " : Initial P is not in the range [Pmin, Pmax]");
        }

        double availableUp = maximumValue(n, ScalingPowerConvention.GENERATOR) - oldTargetP;
        double availableDown = oldTargetP - minimumValue(n, ScalingPowerConvention.GENERATOR);


        if (ScalingPowerConvention.GENERATOR.equals(scalingConvention)) {

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

    private static void connectGenerator(Generator g) {
        Terminal t = g.getTerminal();
        t.connect();
        if (g.isVoltageRegulatorOn()) {
            Bus bus = t.getBusView().getBus();
            if (bus != null) {
                // set voltage setpoint to the same as other generators connected to the bus
                double targetV = bus.getGeneratorStream().findFirst().map(Generator::getTargetV).orElse(Double.NaN);
                // if no other generator connected to the bus, set voltage setpoint to network voltage
                if (Double.isNaN(targetV) && !Double.isNaN(bus.getV())) {
                    g.setTargetV(bus.getV());
                }
            }
        }
        LOGGER.info("Connecting {}", g.getId());
    }
}
