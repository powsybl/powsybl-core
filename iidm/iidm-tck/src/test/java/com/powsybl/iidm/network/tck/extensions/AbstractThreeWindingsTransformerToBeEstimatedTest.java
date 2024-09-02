/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractThreeWindingsTransformerToBeEstimatedTest {

    @Test
    public void test() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformerToBeEstimated ext = twt.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChanger1Status(false)
                .withRatioTapChanger2Status(true)
                .withRatioTapChanger3Status(true)
                .withPhaseTapChanger1Status(false)
                .withPhaseTapChanger2Status(false)
                .withPhaseTapChanger3Status(false)
                .add();

        assertNotNull(ext);
        assertFalse(ext.shouldEstimateRatioTapChanger1());
        assertFalse(ext.shouldEstimateRatioTapChanger(ThreeSides.ONE));
        assertTrue(ext.shouldEstimateRatioTapChanger2());
        assertTrue(ext.shouldEstimateRatioTapChanger(ThreeSides.TWO));
        assertTrue(ext.shouldEstimateRatioTapChanger3());
        assertTrue(ext.shouldEstimateRatioTapChanger(ThreeSides.THREE));
        assertFalse(ext.shouldEstimatePhaseTapChanger1());
        assertFalse(ext.shouldEstimatePhaseTapChanger(ThreeSides.ONE));
        assertFalse(ext.shouldEstimatePhaseTapChanger2());
        assertFalse(ext.shouldEstimatePhaseTapChanger(ThreeSides.TWO));
        assertFalse(ext.shouldEstimatePhaseTapChanger3());
        assertFalse(ext.shouldEstimatePhaseTapChanger(ThreeSides.THREE));

        ext.shouldEstimatePhaseTapChanger1(true);
        assertTrue(ext.shouldEstimatePhaseTapChanger1());
        ext.shouldEstimatePhaseTapChanger(false, ThreeSides.ONE);
        assertFalse(ext.shouldEstimateRatioTapChanger1());

        ext.shouldEstimateRatioTapChanger2(false);
        assertFalse(ext.shouldEstimateRatioTapChanger2());
        ext.shouldEstimateRatioTapChanger(true, ThreeSides.TWO);
        assertTrue(ext.shouldEstimateRatioTapChanger2());
    }

    @ParameterizedTest
    @MethodSource("provideThreeSidesArguments")
    public void test2(ThreeSides ratioTapChangerThreeSides, ThreeSides phaseTapChangerThreeSides) {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformerToBeEstimated ext = twt.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChangerStatus(ratioTapChangerThreeSides, true)
                .withPhaseTapChangerStatus(phaseTapChangerThreeSides, true)
                .add();

        assertNotNull(ext);
        assertEquals(ratioTapChangerThreeSides == ThreeSides.ONE, ext.shouldEstimateRatioTapChanger1());
        assertEquals(ratioTapChangerThreeSides == ThreeSides.ONE, ext.shouldEstimateRatioTapChanger(ThreeSides.ONE));
        assertEquals(ratioTapChangerThreeSides == ThreeSides.TWO, ext.shouldEstimateRatioTapChanger2());
        assertEquals(ratioTapChangerThreeSides == ThreeSides.TWO, ext.shouldEstimateRatioTapChanger(ThreeSides.TWO));
        assertEquals(ratioTapChangerThreeSides == ThreeSides.THREE, ext.shouldEstimateRatioTapChanger3());
        assertEquals(ratioTapChangerThreeSides == ThreeSides.THREE, ext.shouldEstimateRatioTapChanger(ThreeSides.THREE));
        assertEquals(phaseTapChangerThreeSides == ThreeSides.ONE, ext.shouldEstimatePhaseTapChanger1());
        assertEquals(phaseTapChangerThreeSides == ThreeSides.ONE, ext.shouldEstimatePhaseTapChanger(ThreeSides.ONE));
        assertEquals(phaseTapChangerThreeSides == ThreeSides.TWO, ext.shouldEstimatePhaseTapChanger2());
        assertEquals(phaseTapChangerThreeSides == ThreeSides.TWO, ext.shouldEstimatePhaseTapChanger(ThreeSides.TWO));
        assertEquals(phaseTapChangerThreeSides == ThreeSides.THREE, ext.shouldEstimatePhaseTapChanger3());
        assertEquals(phaseTapChangerThreeSides == ThreeSides.THREE, ext.shouldEstimatePhaseTapChanger(ThreeSides.THREE));
    }

    private static Stream<Arguments> provideThreeSidesArguments() {
        return Stream.of(
            Arguments.of(ThreeSides.ONE, ThreeSides.TWO),
            Arguments.of(ThreeSides.TWO, ThreeSides.THREE),
            Arguments.of(ThreeSides.THREE, ThreeSides.ONE)
        );
    }
}
