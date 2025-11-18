/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.List;

/**
 * A droop curve to define droop function of a <code>AcDcConverter</code>
 * in <code>P_PCC_DROOP</code> mode.
 * <p>
 * This curve is made of <code>Segment</code> and each segment is defined by its minimum
 * and maximum voltage associated with a droop coefficient.
 * <p>
 * The following graph shows a 3 segments droop curve. On the X-axis
 * is the <code>AcDcConverter</code> DC voltage, on the Y-axis the droop coefficient.
 *<pre>
 * k
 * ^
 * |
 * |
 * |------
 * |      --------
 * +---------------------> V (kV)
 * |              --------
 * |
 * |
 * |
 *</pre>
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public interface DroopCurve {

    DroopCurve EMPTY = new DroopCurve() {

        @Override
        public Collection<Segment> getSegments() {
            return List.of();
        }

        @Override
        public double getK(double v) {
            return 0;
        }
    };

    /**
     * A segment of the droop curve, for a given voltage range, the droop coefficient associated.
     */
    interface Segment {

        double getK();

        double getMinV();

        double getMaxV();
    }

    /**
     * Get the curve segments.
     */
    Collection<Segment> getSegments();

    /**
     * Get the coefficient k associated to this voltage v.
     */
    double getK(double v);
}
