/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractThreeWindingsTransformerToBeEstimatedTest {

    @Test
    public void test() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        twt.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2)
                .withTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_3)
                .add();

        ThreeWindingsTransformerToBeEstimated ext = twt.getExtension(ThreeWindingsTransformerToBeEstimated.class);
        assertNotNull(ext);
        assertEquals(2, ext.getTapChangers().size());
        assertTrue(ext.getTapChangers().contains(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2));
        assertTrue(ext.getTapChangers().contains(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_3));
        assertFalse(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_1));
        assertFalse(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_1));
        assertTrue(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2));
        assertFalse(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_2));
        assertTrue(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_3));
        assertFalse(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_3));

        ext.addTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_1);
        assertEquals(3, ext.getTapChangers().size());
        assertTrue(ext.getTapChangers().contains(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_1));
        assertTrue(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_1));

        ext.removeTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2);
        assertEquals(2, ext.getTapChangers().size());
        assertFalse(ext.getTapChangers().contains(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2));
        assertFalse(ext.toBeEstimated(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_2));

        ext.cleanIfEmpty();
        assertNotNull(twt.getExtension(ThreeWindingsTransformerToBeEstimated.class));

        ext.removeTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER_1)
                .removeTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER_3);
        ext.cleanIfEmpty();
        assertNull(twt.getExtension(ThreeWindingsTransformerToBeEstimated.class));
    }
}
