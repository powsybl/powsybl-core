/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;

/**
 * A reactive capability curve to define reactive limits of a <code>Generator<code>
 * that depends of the active power.
 * <p>
 * This curve is made of <code>Point</code> and each point defines the minimum
 * and maximum reactive limit for a given active power of the generator.
 * <p>
 * The following graph shows a 4 points reactive capability curve. On the X-axis
 * is the active power in MW, on the Y-axis the minimal and maximal reactive
 * values in MVAR.
 *<pre>
 * Q (MVAR)
 * ^
 * |
 * |     *     *
 * |
 * | *             *
 * +---------------------> P (MW)
 * | *             *
 * |
 * |     *     *
 * |
 *</pre>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see Generator
 */
public interface ReactiveCapabilityCurve extends ReactiveLimits {

    /**
     * A point of the reactive capability curve, for a given active power the
     * minimal and the maximal value for the reactive power.
     */
    public interface Point {

        double getP();

        double getMinQ();

        double getMaxQ();
    }

    /**
     * Get the curve points.
     */
    Collection<Point> getPoints();

    /**
     * Get the curve point count.
     */
    int getPointCount();

    /**
     * Get the active power minimum value of the curve.
     */
    double getMinP();

    /**
     * Get the active power maximim value of the curve.
     */
    double getMaxP();

}
