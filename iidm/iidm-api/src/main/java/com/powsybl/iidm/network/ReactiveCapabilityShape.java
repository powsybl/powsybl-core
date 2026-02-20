/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;

/**
 * Base interface for reactive capabilities shape limits.
 * <pre>
 *  Example of realisable point inside a cubic convex PQU enveloppe
 *           U (kV) ^
 *                     |
 *                     |      +------------------+
 *                     |     /|                 /|
 *                     |    / |                / |
 *                     *---*--+---------------* |
 *                     | / |  |               |  |
 *                     |/  |  . (P, Q, U)     |  |
 *                     +---|--|---------------|--+
 *                     |   |  +---------------|--+
 *                     |   | /                | /
 *                     |   |/                 |/
 *                     +---|------------------+
 *                     |
 * <-------------------*--------------------------------> P (MW)
 *                    /
 *                   /
 *                  / Q (MVaR)
 *
 * - P (MW): Active Power (Horizontal Axis)
 * - Q (MVaR): Reactive Power (Depth Axis)
 * - U (kV): Voltage (Vertical Axis)
 * </pre>
 * The bounding box represents the operational limits—the single convex polyhedron defined by your constraints (the listOfPlanes).
 * The point (P, Q, U) represents a specific operating point that the isInside function is checking.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public interface ReactiveCapabilityShape extends ReactiveLimits {

    /**
     * Get the reactive power minimum value at a given active power and voltage values.
     *
     * @param p the active power
     * @param u : the voltage
     */
    double getMinQ(double p, double u);

    /**
     * Get the reactive power maximum value at a given active power and voltage values.
     *
     * @param p the active power
     * @param u the voltage
     */
    double getMaxQ(double p, double u);

    /**
     * @return the list of planes of the convex polyhedron
     */
    Collection<ReactiveCapabilityShapePlane> getPlanes();

    /**
     * Checks if a point (P, Q, U) is inside the convex polyhedron.
     * @param p The Active Power (P in MW).
     * @param q The Reactive Power (Q in MVaR).
     * @param u The Voltage (U in KV).
     * @return true if the point satisfies ALL plane constraints, false otherwise.
     */
    boolean isInside(final double p, final double q, final double u);
}
