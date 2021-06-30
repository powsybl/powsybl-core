/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractTwoWindingsTransformerToBeEstimatedTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        twt.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class)
                .withTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER)
                .add();

        TwoWindingsTransformerToBeEstimated ext = twt.getExtension(TwoWindingsTransformerToBeEstimated.class);
        assertNotNull(ext);
        assertEquals(1, ext.getTapChangers().size());
        assertTrue(ext.getTapChangers().contains(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER));
        assertTrue(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER));
        assertFalse(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER));

        ext.addTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER);
        assertEquals(2, ext.getTapChangers().size());
        assertTrue(ext.getTapChangers().contains(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER));
        assertTrue(ext.getTapChangers().contains(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER));
        assertTrue(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER));
        assertTrue(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER));

        ext.removeTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER);
        assertNotNull(ext);
        assertEquals(1, ext.getTapChangers().size());
        assertTrue(ext.getTapChangers().contains(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER));
        assertTrue(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER));
        assertFalse(ext.tobeEstimated(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER));

        ext.cleanIfEmpty();
        assertNotNull(twt.getExtension(TwoWindingsTransformerToBeEstimated.class));

        ext.removeTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.PHASE_TAP_CHANGER);
        ext.cleanIfEmpty();
        assertNull(twt.getExtension(TwoWindingsTransformerToBeEstimated.class));
    }
}
