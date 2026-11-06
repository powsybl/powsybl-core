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

    record ReactiveCapabilityShapeBound(double qBound, boolean isMin) { }

    /**
     * @return true if the hyperplan is of '≤' type
     */
    boolean isLessOrEqual();

    /**
     * @return true if the hyperplan is of '≥' type
     */
    boolean isGreaterOrEqual();

    /**
     * @return the alpha coefficient for the tension U
     */
    double getAlpha();

    /**
     * @return the beta coefficient for the active power P
     */
    double getBeta();

    /**
     * @return the delta coefficient for the reactive power Q
     */
    double getDelta();

    /**
     * @return the gamma Right hand side
     */
    double getGamma();

    /**
     * @return the minQ or maxQ corresponding to the bound defined by this hyperplane at the given line (P, U)
     * The object returned is a ReactiveCapabilityShapeBound holding the bound value and the information of being a minQ or maxQ
     * Return null if there is no bound with Q (if the line defined by (P, U) and the hyperplane are parallel)
     */
    ReactiveCapabilityShapeBound getBoundQ(double p, double u);

    /**
     * Set the hyperplane constraint to a less or equal ≤ inequality with gamma right hand side
     * @param gamma the gamma right hand side
     * @return this
     */
    ReactiveCapabilityShapePlane lessOrEqual(double gamma);

    /**
     * Set the hyperplane constraint to a greater or equal ≥ inequality with gamma right hand side
     * @param gamma the gamma right hand side
     * @return this
     */
    ReactiveCapabilityShapePlane greaterOrEqual(double gamma);

}
