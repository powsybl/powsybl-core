/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve.Point;
import com.powsybl.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImplTest {

    private ReactiveCapabilityCurveImpl createCurve(Point... points) {
        TreeMap<Double, Point> map = new TreeMap<>();
        for (Point pt : points) {
            map.put(pt.getP(), pt);
        }
        return new ReactiveCapabilityCurveImpl(map);
    }

    @Test
    public void testInterpolation() {
        ReactiveCapabilityCurveImpl curve = createCurve(new PointImpl(100.0, 200.0, 300.0),
                                                        new PointImpl(200.0, 300.0, 400.0));
        // bounds test
        assertEquals(200.0, curve.getMinQ(100.0), 0.0);
        assertEquals(300.0, curve.getMaxQ(100.0), 0.0);
        assertEquals(300.0, curve.getMinQ(200.0), 0.0);
        assertEquals(400.0, curve.getMaxQ(200.0), 0.0);

        // interpolation test
        assertEquals(250.0, curve.getMinQ(150.0), 0.0);
        assertEquals(350.0, curve.getMaxQ(150.0), 0.0);
        assertEquals(210.0, curve.getMinQ(110.0), 0.0);
        assertEquals(310.0, curve.getMaxQ(110.0), 0.0);

        // out of bounds test
        assertEquals(200.0, curve.getMinQ(0.0), 0.0);
        assertEquals(300.0, curve.getMaxQ(0.0), 0.0);
        assertEquals(300.0, curve.getMinQ(1000.0), 0.0);
        assertEquals(400.0, curve.getMaxQ(1000.0), 0.0);
    }

}
