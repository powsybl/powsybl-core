/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
* @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
*/
public abstract class AbstractDroopCurveTest {

    private VoltageSourceConverter converter;

    @BeforeEach
    public void setUp() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        converter = network.getVoltageSourceConverter("VscFr");
    }

    @Test
    public void testAdder() {
        DroopCurve droopCurve = converter.newDroopCurve()
                    .beginSegment()
                        .setK(1.0)
                        .setMaxV(100.0)
                        .setMinV(0.0)
                    .endSegment()
                    .beginSegment()
                        .setK(2.0)
                        .setMaxV(400.0)
                        .setMinV(100.0)
                    .endSegment()
                    .beginSegment()
                        .setK(10.0)
                        .setMaxV(1000.0)
                        .setMinV(400.0)
                    .endSegment()
                .add();
        assertEquals(3, droopCurve.getSegments().size());
    }

    @Test
    public void invalidK() {
        ValidationException e = assertThrows(ValidationException.class, () -> converter.newDroopCurve()
                    .beginSegment()
                        .setK(Double.NaN)
                        .setMaxV(500.0)
                        .setMinV(100.0)
                    .endSegment()
                .add());
        assertTrue(e.getMessage().contains("k is not set"));
    }

    @Test
    public void invalidMaxV() {
        ValidationException e = assertThrows(ValidationException.class, () -> converter.newDroopCurve()
                    .beginSegment()
                        .setK(1.0)
                        .setMaxV(Double.NaN)
                        .setMinV(100.0)
                    .endSegment()
                .add());
        assertTrue(e.getMessage().contains("max V is not set"));
    }

    @Test
    public void invalidMinV() {
        ValidationException e = assertThrows(ValidationException.class, () -> converter.newDroopCurve()
                    .beginSegment()
                        .setK(1.0)
                        .setMaxV(500.0)
                        .setMinV(Double.NaN)
                    .endSegment()
                .add());
        assertTrue(e.getMessage().contains("min V is not set"));
    }

    @Test
    public void overlapping() {
        ValidationException e = assertThrows(ValidationException.class, () -> converter.newDroopCurve()
                .beginSegment()
                .setK(1.0)
                .setMaxV(500.0)
                .setMinV(0.0)
                .endSegment()
                .beginSegment()
                .setK(1.0)
                .setMaxV(100.0)
                .setMinV(-500.0)
                .endSegment()
                .add());
        assertTrue(e.getMessage().contains("Droop segments are overlapping"));
    }

    @Test
    public void discontinuous() {
        ValidationException e = assertThrows(ValidationException.class, () -> converter.newDroopCurve()
                .beginSegment()
                .setK(1.0)
                .setMaxV(500.0)
                .setMinV(0.0)
                .endSegment()
                .beginSegment()
                .setK(1.0)
                .setMaxV(-100.0)
                .setMinV(-500.0)
                .endSegment()
                .add());
        assertTrue(e.getMessage().contains("Droop curve is not continuous"));
    }
}
