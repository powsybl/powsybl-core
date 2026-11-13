/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public interface ReactiveCapabilityShapeAdder extends PropertiesHolder {

    /**
     * Add the reactive capability shape to the element
     * @return the ReactiveCapabilityShape
     */
    ReactiveCapabilityShape add();

    /**
     * Add a reactive capability shape plane to the reactive capability shape
     *
     * <pre>
     * The inequality is of the form: coeffQ * Q + coeffU * U + coeffP * P  {≤, ≥}  rhs.
     * P = Active Power (MW)
     * Q = Reactive Power (MVAr)
     * U = Voltage (Volts)
     * </pre>
     * @param coeffU the coefficient for Voltage U in Volts
     * @param coeffP the coefficient for active power P in MW
     * @param coeffQ the coefficient for reactive power Q in MVAr
     * @param rhs the right hand side
     * @param isGreaterOrEqual true if the inequality is greater or equal, false if the inequality is less or equal
     * @return this
     */
    ReactiveCapabilityShapeAdder addPlane(double coeffU, double coeffP, double coeffQ, boolean isGreaterOrEqual, double rhs);

}
