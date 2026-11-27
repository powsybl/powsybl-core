/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Base class for scalables that consist in a unique injection with minimum and maximum
 * power value.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
abstract class AbstractInjectionScalable extends AbstractScalable {

    protected final String id;

    protected final double minValue;

    protected final double maxValue;

    AbstractInjectionScalable(String id) {
        this(id, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    AbstractInjectionScalable(String id, double minValue, double maxValue) {
        this.id = Objects.requireNonNull(id);
        if (maxValue < minValue) {
            throw new PowsyblException("Error creating Scalable " + id
                    + " : maxValue should be bigger than minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Returns the id of the injection scaled by this scalable.
     *
     * @return the id of the injection
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the minimum active power value allowed for the injection by this scalable, in MW.
     * <p>
     * This bound is expressed in the natural sign convention of the injection (e.g. load convention
     * for loads, generator convention for generators) and is applied in addition to the injection's own
     * limits. Defaults to {@code -Double.MAX_VALUE} when not specified.
     *
     * @return the minimum active power value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Returns the maximum active power value allowed for the injection by this scalable, in MW.
     * <p>
     * This bound is expressed in the natural sign convention of the injection (e.g. load convention
     * for loads, generator convention for generators) and is applied in addition to the injection's own
     * limits. Defaults to {@code Double.MAX_VALUE} when not specified.
     *
     * @return the maximum active power value
     */
    public double getMaxValue() {
        return maxValue;
    }

    private Injection getInjectionOrNull(Network n) {
        Identifiable identifiable = n.getIdentifiable(id);
        if (identifiable instanceof Injection<?> injection) {
            return injection;
        } else {
            return null;
        }
    }

    @Override
    public double initialValue(Network n) {
        Objects.requireNonNull(n);

        Injection injection = getInjectionOrNull(n);
        if (injection != null) {
            return !Double.isNaN(injection.getTerminal().getP()) ? injection.getTerminal().getP() : 0;
        } else {
            return 0;
        }
    }
}
