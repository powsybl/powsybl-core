/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ReactiveCapabilityCurve.Point;
import eu.itesla_project.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import java.util.TreeMap;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImplTest {

    private ReactiveCapabilityCurveImpl createCurve(Point... points) {
        TreeMap<Float, Point> map = new TreeMap<>();
        for (Point pt : points) {
            map.put(pt.getP(), pt);
        }
        return new ReactiveCapabilityCurveImpl(map);
    }

    @Test
    public void testInterpolation() {
        ReactiveCapabilityCurveImpl curve = createCurve(new PointImpl(100, 200, 300),
                                                        new PointImpl(200, 300, 400));
        // bounds test
        Assert.assertTrue(curve.getMinQ(100) == 200f);
        Assert.assertTrue(curve.getMaxQ(100) == 300f);
        Assert.assertTrue(curve.getMinQ(200) == 300f);
        Assert.assertTrue(curve.getMaxQ(200) == 400f);

        // interpolation test
        Assert.assertTrue(curve.getMinQ(150) == 250f);
        Assert.assertTrue(curve.getMaxQ(150) == 350f);
        Assert.assertTrue(curve.getMinQ(110) == 210f);
        Assert.assertTrue(curve.getMaxQ(110) == 310f);

        // out of bounds test
        Assert.assertTrue(curve.getMinQ(0) == 200f);
        Assert.assertTrue(curve.getMaxQ(0) == 300f);
        Assert.assertTrue(curve.getMinQ(1000) == 300f);
        Assert.assertTrue(curve.getMaxQ(1000) == 400f);
    }

}
