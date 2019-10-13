/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void basicTest() {
        ReactiveCapabilityCurveImpl curve = ReactiveCapabilityCurveImpl.of(new PointImpl(1, 2, 3),
                                                                           new PointImpl(2, 3, 4));
        assertEquals(ReactiveLimitsKind.CURVE, curve.getKind());
        assertEquals(2, curve.getPointCount());
        assertEquals(1, curve.getMinP(), 0);
        assertEquals(2, curve.getMaxP(), 0);
    }

    @Test
    public void testInterpolation() {
        ReactiveCapabilityCurveImpl curve = ReactiveCapabilityCurveImpl.of(new PointImpl(100.0, 200.0, 300.0),
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

    @Test
    public void equalsTest() {
        new EqualsTester()
                .addEqualityGroup(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3), new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3))
                .addEqualityGroup(new ReactiveCapabilityCurveImpl.PointImpl(2, -1, 4), new ReactiveCapabilityCurveImpl.PointImpl(2, -1, 4))
                .testEquals();
        new EqualsTester()
                .addEqualityGroup(ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3), new ReactiveCapabilityCurveImpl.PointImpl(4, -4, 1)),
                                  ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3), new ReactiveCapabilityCurveImpl.PointImpl(4, -4, 1)))
                .addEqualityGroup(ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(2, -1, 4), new ReactiveCapabilityCurveImpl.PointImpl(4, -4, 1)),
                                  ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(2, -1, 4), new ReactiveCapabilityCurveImpl.PointImpl(4, -4, 1)))
                .testEquals();
    }

    @Test
    public void onePointError() {
        thrown.expectMessage("A reactive capability curve is expected to have at least 2 points");
        thrown.expect(IllegalArgumentException.class);
        ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3));
    }

    @Test
    public void sameActivePowerPointError() {
        thrown.expectMessage("Duplicate point at p=1.0");
        thrown.expect(IllegalArgumentException.class);
        ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3),
                                       new ReactiveCapabilityCurveImpl.PointImpl(1, -1, 1));
    }

    @Test
    public void addTest() {
        ReactiveCapabilityCurve curve1 = ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(1, -2, 3),
                                                                        new ReactiveCapabilityCurveImpl.PointImpl(2, -1, 4),
                                                                        new ReactiveCapabilityCurveImpl.PointImpl(4, -4, 1));

        ReactiveCapabilityCurve curve2 = ReactiveCapabilityCurveImpl.of(new ReactiveCapabilityCurveImpl.PointImpl(2, -2, 2),
                                                                        new ReactiveCapabilityCurveImpl.PointImpl(3, 0, 6),
                                                                        new ReactiveCapabilityCurveImpl.PointImpl(5, -1, 1));

        ReactiveCapabilityCurveImpl sum = ReactiveCapabilityCurveImpl.add(curve1, curve2);
        assertEquals(ReactiveCapabilityCurveImpl.of(new PointImpl(1.0, -2.0, 3.0),
                                                    new PointImpl(2.0, -2.0, 4.0),
                                                    new PointImpl(3.0, -4.0, 6.0),
                                                    new PointImpl(4.0, -4.0, 9.0),
                                                    new PointImpl(5.0, -1.0, 10.0),
                                                    new PointImpl(6.0, -6.0, 4.0),
                                                    new PointImpl(7.0, -4.0, 7.0),
                                                    new PointImpl(9.0, -5.0, 2.0)),
                     sum);

        ReactiveCapabilityCurve sum2 = ReactiveCapabilityCurveImpl.add(curve1, new MinMaxReactiveLimitsImpl(-3, 2));
        assertEquals(ReactiveCapabilityCurveImpl.of(new PointImpl(1.0, -3.0, 3.0),
                                                    new PointImpl(2.0, -3.0, 4.0),
                                                    new PointImpl(4.0, -4.0, 2.0)),
                     sum2);
    }
}
