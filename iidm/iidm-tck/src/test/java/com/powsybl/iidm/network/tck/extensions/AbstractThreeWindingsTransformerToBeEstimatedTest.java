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
import org.junit.jupiter.api.Test;

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
}
