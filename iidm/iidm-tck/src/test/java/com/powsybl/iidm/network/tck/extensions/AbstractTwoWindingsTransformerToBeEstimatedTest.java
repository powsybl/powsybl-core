/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractTwoWindingsTransformerToBeEstimatedTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        TwoWindingsTransformerToBeEstimated ext = twt.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChangerStatus(true)
                .withPhaseTapChangerStatus(false)
                .add();

        assertNotNull(ext);
        assertTrue(ext.shouldEstimateRatioTapChanger());
        assertFalse(ext.shouldEstimatePhaseTapChanger());

        ext.shouldEstimatePhaseTapChanger(true);
        assertTrue(ext.shouldEstimateRatioTapChanger());
        assertTrue(ext.shouldEstimatePhaseTapChanger());

        ext.shouldEstimateRatioTapChanger(false);
        assertFalse(ext.shouldEstimateRatioTapChanger());
        assertTrue(ext.shouldEstimatePhaseTapChanger());
    }
}
