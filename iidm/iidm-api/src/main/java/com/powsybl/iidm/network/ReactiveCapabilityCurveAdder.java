/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ReactiveCapabilityCurveAdder {

    interface PointAdder {

        PointAdder setP(double p);

        PointAdder setMinQ(double minQ);

        PointAdder setMaxQ(double maxQ);

        ReactiveCapabilityCurveAdder endPoint();
    }

    PointAdder beginPoint();

    ReactiveCapabilityCurve add();

    /**
     * Sets whether the validation for checking minQ and maxQ values should be performed when adding a reactive
     * capability curve.
     *
     * <p>This method is used only for retroactive compatibility of IIDM files for custom IIDM implementations.
     * It should not be called manually.</p>
     *
     * @param shouldCheckMinMaxValues a boolean indicating whether to check for minQ and maxQ values
     * @return the current instance of the {@code ReactiveCapabilityCurveAdder} for method chaining
     */
    ReactiveCapabilityCurveAdder setShouldCheckMinMaxValues(boolean shouldCheckMinMaxValues);
}
