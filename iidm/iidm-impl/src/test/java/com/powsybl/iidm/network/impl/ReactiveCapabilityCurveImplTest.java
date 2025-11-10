/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurve.Point;
import com.powsybl.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ReactiveCapabilityCurveImplTest {

    private ReactiveCapabilityCurveImpl createCurve(Point... points) {
        TreeMap<Double, Point> map = new TreeMap<>();
        for (Point pt : points) {
            map.put(pt.getP(), pt);
        }
        return new ReactiveCapabilityCurveImpl(map, "ReactiveCapabilityCurve owner");
    }

    private static boolean checkReportNode(String expected, ReportNode reportNode) throws IOException {
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals(expected, sw.toString());
        return true;
    }

    @Test
    void testReactiveCapabilityCurve() {
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

    @Test
    void testReactiveCapabilityCurveRevertedMinQMaxQ() throws IOException {

        Network network = FictitiousSwitchFactory.create();

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("key1")
                .build();
        network.getReportNodeContext().pushReportNode(reportNode);

        Generator generator = network.getGenerator("CB");

        ReactiveCapabilityCurve reactiveCapabilityCurve1 = generator.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(1.0)
                .setMaxQ(5.0)
                .setMinQ(1.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMaxQ(2.0) // here minQ > maxQ : this is incorrect
                .setMinQ(10.0)
                .endPoint()
                .add();

        assertEquals(1.0, reactiveCapabilityCurve1.getMinQ(1.0), 0.0);
        assertEquals(5.0, reactiveCapabilityCurve1.getMaxQ(1.0), 0.0);
        // reversed minQ and maxQ remain unchanged :
        assertEquals(10.0, reactiveCapabilityCurve1.getMinQ(100.0), 0.0);
        assertEquals(2.0, reactiveCapabilityCurve1.getMaxQ(100.0), 0.0);

        network.setProperty("iidm.import.xml.check-minqmaxq-inversion", "true");

        ReactiveCapabilityCurve reactiveCapabilityCurve2 = generator.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(1.0)
                .setMaxQ(5.0)
                .setMinQ(1.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMaxQ(2.0) // here minQ > maxQ : this is incorrect
                .setMinQ(10.0)
                .endPoint()
                .add();

        assertEquals(1.0, reactiveCapabilityCurve2.getMinQ(1.0), 0.0);
        assertEquals(5.0, reactiveCapabilityCurve2.getMaxQ(1.0), 0.0);
        // reversed minQ and maxQ values have been put in the right order and this information is mentioned in the report node :
        assertEquals(2.0, reactiveCapabilityCurve2.getMinQ(100.0), 0.0);
        assertEquals(10.0, reactiveCapabilityCurve2.getMaxQ(100.0), 0.0);
        assertTrue(checkReportNode("+ name1" + System.lineSeparator() +
                "   Reactive capability curve for CB : reversed minQ > maxQ values have been put in the right order" + System.lineSeparator(), network.getReportNodeContext().getReportNode()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testReactiveCapabilityCurveWithReactiveLimitsExtrapolation(boolean extrapolate) {
        ReactiveCapabilityCurveImpl curve = createCurve(new PointImpl(100.0, 200.0, 300.0),
                new PointImpl(200.0, 300.0, 400.0),
                new PointImpl(300.0, 300.0, 400.0),
                new PointImpl(400.0, 310.0, 390.0));
        // bounds test
        assertEquals(200.0, curve.getMinQ(100.0, extrapolate), 0.0);
        assertEquals(300.0, curve.getMaxQ(100.0, extrapolate), 0.0);
        assertEquals(300.0, curve.getMinQ(200.0, extrapolate), 0.0);
        assertEquals(400.0, curve.getMaxQ(200.0, extrapolate), 0.0);

        // interpolation test
        assertEquals(250.0, curve.getMinQ(150.0, extrapolate), 0.0);
        assertEquals(350.0, curve.getMaxQ(150.0, extrapolate), 0.0);
        assertEquals(210.0, curve.getMinQ(110.0, extrapolate), 0.0);
        assertEquals(310.0, curve.getMaxQ(110.0, extrapolate), 0.0);

        // out of bounds test
        assertEquals(extrapolate ? 100.0 : 200.0, curve.getMinQ(0.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 200.0 : 300.0, curve.getMaxQ(0.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 320.0 : 310.0, curve.getMinQ(500.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 380.0 : 390.0, curve.getMaxQ(500.0, extrapolate), 0.0);

        // intersecting reactive limits test
        assertEquals(extrapolate ? 350.0 : 310.0, curve.getMinQ(1500.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 350.0 : 390.0, curve.getMaxQ(1500.0, extrapolate), 0.0);
    }

    @Test
    void testWithNegativeZeroValue() {
        ReactiveCapabilityCurveImpl curve = createCurve(new PointImpl(0.0, 200.0, 300.0),
                new PointImpl(200.0, 300.0, 400.0));
        // "-0.0 == 0.0" (JLS), but "Double.compareTo(-0.0, 0.0) = -1"
        // This test asserts that -0.0 is considered as equal to 0.0 by the reactive capability curve.
        assertEquals(200.0, curve.getMinQ(-0.0), 0.0);
    }
}
