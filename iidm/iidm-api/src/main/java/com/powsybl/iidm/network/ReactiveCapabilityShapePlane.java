/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at rte-france.com>}
 */
public interface ReactiveCapabilityShapePlane {

    /**
     * @return true if the hyperplan is of '≤' type
     */
    boolean isLessOrEqual();

    /**
     * @return true if the hyperplan is of '≥' type
     */
    boolean isGreaterOrEqual();

    /**
     * @return the coefficient for the tension U
     */
    double getCoeffU();

    /**
     * @return the coefficient for the active power P
     */
    double getCoeffP();

    /**
     * @return the coefficient for the reactive power Q
     */
    double getCoeffQ();

    /**
     * @return the right hand side
     */
    double getRhs();

    /**
     * Set the hyperplane constraint to a less or equal ≤ inequality with the given right hand side
     * @param rhs the right hand side
     * @return this
     */
    ReactiveCapabilityShapePlane lessOrEqual(double rhs);

    /**
     * Set the hyperplane constraint to a greater or equal ≥ inequality with the given right hand side
     * @param rhs the right hand side
     * @return this
     */
    ReactiveCapabilityShapePlane greaterOrEqual(double rhs);

}
