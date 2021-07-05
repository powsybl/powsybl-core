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
                .withRatioTapChanger1Status(false)
                .withRatioTapChanger2Status(true)
                .withRatioTapChanger3Status(true)
                .withPhaseTapChanger1Status(false)
                .withPhaseTapChanger2Status(false)
                .withPhaseTapChanger3Status(false)
                .add();

        ThreeWindingsTransformerToBeEstimated ext = twt.getExtension(ThreeWindingsTransformerToBeEstimated.class);
        assertNotNull(ext);
        assertFalse(ext.containsRatioTapChanger1());
        assertTrue(ext.containsRatioTapChanger2());
        assertTrue(ext.containsRatioTapChanger3());
        assertFalse(ext.containsPhaseTapChanger1());
        assertFalse(ext.containsPhaseTapChanger2());
        assertFalse(ext.containsPhaseTapChanger3());

        ext.setPhaseTapChanger1Status(true);
        assertTrue(ext.containsPhaseTapChanger1());

        ext.setRatioTapChanger2Status(false);
        assertFalse(ext.containsRatioTapChanger2());
    }
}
