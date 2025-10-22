/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * Base interface for <code>Generator</code> reactive capabilities shape limits.
 * <pre>
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
     */
    double getMinQ(double p, double v);

    /**
     * Get the reactive power maximum value at a given active power  and voltage values.
     *
     * @param p the active power
     */
    double getMaxQ(double p, double v);

    /**
     * @return the reactive capability shape polyhedron
     */
    ReactiveCapabilityShapePolyhedron getPolyhedron();
}
