/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve.Point;
import com.powsybl.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import java.util.TreeMap;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(curve.getMinQ(100.0) == 200.0);
        Assert.assertTrue(curve.getMaxQ(100.0) == 300.0);
        Assert.assertTrue(curve.getMinQ(200.0) == 300.0);
        Assert.assertTrue(curve.getMaxQ(200.0) == 400.0);

        // interpolation test
        Assert.assertTrue(curve.getMinQ(150.0) == 250.0);
        Assert.assertTrue(curve.getMaxQ(150.0) == 350.0);
        Assert.assertTrue(curve.getMinQ(110.0) == 210.0);
        Assert.assertTrue(curve.getMaxQ(110.0) == 310.0);

        // out of bounds test
        Assert.assertTrue(curve.getMinQ(0.0) == 200.0);
        Assert.assertTrue(curve.getMaxQ(0.0) == 300.0);
        Assert.assertTrue(curve.getMinQ(1000.0) == 300.0);
        Assert.assertTrue(curve.getMaxQ(1000.0) == 400.0);
    }

}
