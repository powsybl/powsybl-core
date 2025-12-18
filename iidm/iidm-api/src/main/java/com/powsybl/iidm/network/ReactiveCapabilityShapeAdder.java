/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
public interface ReactiveCapabilityShapeAdder {

    /**
     * Add the reactive capability shape to the element
     * @return the ReactiveCapabilityShape
     */
    ReactiveCapabilityShape add();

    /**
     * Add a reactive capability shape plane to the reactive capability shape
     *
     * <pre>
     * The inequality is of the form: Q + alpha * U + beta * P  {≤, ≥}  gamma.
     * P = Active Power (MW)
     * Q = Reactive Power (MVaR)
     * U = Voltage (Volts)
     * </pre>
     * @param alpha the alpha coefficient for Voltage U in Volts
     * @param beta the beta coefficient for active power P in MW
     * @param gamma the gamma right hand side
     * @param isGreaterOrEqual true if the inequality is greater or equal, false if the inequality is less or equal
     * @return this
     */
    ReactiveCapabilityShapeAdder addPlane(double alpha, double beta, double gamma, boolean isGreaterOrEqual);

}
