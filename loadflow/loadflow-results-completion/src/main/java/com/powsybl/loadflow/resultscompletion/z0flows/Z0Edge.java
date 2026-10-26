/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.*;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class Z0Edge extends DefaultWeightedEdge {

    private static final double UNDEFINED_LIMIT = 1.0e6;
    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double ASSUMED_POWER_FACTOR = 0.95;

    public Z0Edge(Line line) {
        this.line = Objects.requireNonNull(line);
    }

    public Line getLine() {
        return line;
    }

    @Override
    protected double getWeight() {
        return getMinimumLineLoadingLimit(line);
    }

    // No serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    private static double getMinimumLineLoadingLimit(Line line) {
        double limit = Math.min(
                getLoadingLimitForLineSide(line, TwoSides.ONE),
                getLoadingLimitForLineSide(line, TwoSides.TWO));
        return Double.isFinite(limit) ? limit : UNDEFINED_LIMIT;
    }

    private static double getLoadingLimitForLineSide(Line line, TwoSides side) {

        double loadingLimit = line.getApparentPowerLimits(side)
                .map(LoadingLimits::getPermanentLimit)
                .filter(Double::isFinite)
                .orElse(UNDEFINED_LIMIT);

        Optional<CurrentLimits> currentLimits = line.getCurrentLimits(side);
        if (currentLimits.isPresent()) {
            double current = currentLimits.get().getPermanentLimit();
            if (Double.isFinite(current)) {
                double nominalV = line.getTerminal(side).getVoltageLevel().getNominalV();
                loadingLimit = Math.min(loadingLimit, SQRT_3 * nominalV * current / 1000.0);
            }
        }

        Optional<ActivePowerLimits> activePowerLimits = line.getActivePowerLimits(side);
        if (activePowerLimits.isPresent()) {
            double activePower = activePowerLimits.get().getPermanentLimit();
            if (Double.isFinite(activePower)) {
                loadingLimit = Math.min(loadingLimit, activePower / ASSUMED_POWER_FACTOR);
            }
        }
        return loadingLimit;
    }

    private final transient Line line;
}
