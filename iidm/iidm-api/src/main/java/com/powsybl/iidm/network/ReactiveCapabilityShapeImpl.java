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
 * The bounding box represents the operational limits—the single convex
 * polyhedron defined by your constraints(the listOfPlanes).
 * The point (P, Q, U) represents a specific operating point that the
 * isInside function is checking.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class ReactiveCapabilityShapeImpl implements ReactiveCapabilityShape {

    /**
     * The convex polyhedron
     */
    private final ReactiveCapabilityShapePolyhedron polyhedron;

    /**
     * @return the kind of reactive limit (SHAPE)
     */
    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.SHAPE;
    }

    /**
     * Return the minimum reactive power q for a specific active power p and a specific voltage v
     * @param p the active power
     * @return he minimum reactive power q for a specific active power p and a specific voltage v
     */
    @Override
    public double getMinQ(final double p, final double v) {
        return polyhedron.getMinQ(p, v);
    }

    /**
     * Return the maximum reactive power q for a specific active power p and a specific voltage v
     * @param p the active power
     * @return the maximum reactive power q for a specific active power p and a specific voltage v
     */
    @Override
    public double getMaxQ(final double p, final double v) {
        return polyhedron.getMaxQ(p, v);
    }

    /**
     * Return the minimum reactive power q for a specific active power p
     * @param p the active power
     * @return the minimum reactive power q for a specific active power p
     */
    @Override
    public double getMinQ(final double p) {
        return polyhedron.getMinQ(p);
    }

    /**
     * Return the maximum reactive power q for a specific active power p
     * @param p the active power
     * @return the maximum reactive power q for a specific active power p
     */
    @Override
    public double getMaxQ(final double p) {
        return polyhedron.getMaxQ(p);
    }

    /**
     * Constructor
     * @param polyhedron the reactive capacility shape polyhedron
     */
    private ReactiveCapabilityShapeImpl(final ReactiveCapabilityShapePolyhedron polyhedron) {
        this.polyhedron = polyhedron;
    }

    /**
     * Builder
     * @param polyhedron the reactive capacility shape polyhedron
     */
    public static ReactiveCapabilityShapeImpl build(final ReactiveCapabilityShapePolyhedron polyhedron) {
        return new ReactiveCapabilityShapeImpl(polyhedron);
    }

}
