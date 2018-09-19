/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface ReactiveLimitsHolder {

    /**
     * Get reactive limits of the generator.
     */
    ReactiveLimits getReactiveLimits();

    <L extends ReactiveLimits> L getReactiveLimits(Class<L> type);

    /**
     * Get a builder to create and associate a new reactive capability curve
     * to this generator.
     */
    ReactiveCapabilityCurveAdder newReactiveCapabilityCurve();

    /**
     * Get a builder to create and associate minimum and maximum reactive limits
     * to this generator.
     */
    MinMaxReactiveLimitsAdder newMinMaxReactiveLimits();
}
