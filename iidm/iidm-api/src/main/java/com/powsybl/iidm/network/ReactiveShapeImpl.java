/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <code>Generator</code> reactive limits.
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
 * - P (MW): Active Power (Horizontal Axis)
 * - Q (MVaR): Reactive Power (Depth Axis)
 * - U (kV): Voltage (Vertical Axis)
 * The bounding box represents the operational limits—the single convex polyhedron defined by your constraints (the listOfPlanes).
 * The point (P, Q, U) represents a specific operating point that the isInside function is checking.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public class ReactiveShapeImpl implements ReactiveShape {



    /**
     * The volume
     */
    private final ReactiveShapePolyhedron polyhedron;

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.SHAPE;
    }

    @Override
    public double getMinQ(double p, double v) {
        return polyhedron.getMinQ(p,v);
    }

    @Override
    public double getMaxQ(double p, double v) {
        return polyhedron.getMaxQ(p,v);
    }

    @Override
    public double getMinQ(double p) {
        return polyhedron.getMinQ(p);
    }

    @Override
    public double getMaxQ(double p) {
        return polyhedron.getMaxQ(p);
    }

    public ReactiveShapeImpl(ReactiveShapePolyhedron polyhedron) {
        this.polyhedron = polyhedron;
    }


}
