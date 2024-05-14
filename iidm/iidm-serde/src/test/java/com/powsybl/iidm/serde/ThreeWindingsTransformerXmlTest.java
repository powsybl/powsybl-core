/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */
class ThreeWindingsTransformerXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("threeWindingsTransformerRoundTripRef.xml");

        allFormatsRoundTripTest(ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits(), "threeWindingsTransformerRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void completeTwtTest() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        twt.getLeg1().setRatedS(1.0);

        twt.getLeg1().newRatioTapChanger()
                .setRegulating(false)
                .setLoadTapChangingCapabilities(false)
                .setTapPosition(0)
                .beginStep()
                .setRho(1.0)
                .setR(1.089)
                .setX(0.1089)
                .setG(0.09182736455463728)
                .setB(0.009182736455463728)
                .endStep()
                .add();

        createPtc(twt.getLeg1().newPhaseTapChanger());
        createPtc(twt.getLeg2().newPhaseTapChanger());
        createPtc(twt.getLeg3().newPhaseTapChanger());

        allFormatsRoundTripTest(network, "completeThreeWindingsTransformerRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    private void createPtc(PhaseTapChangerAdder adder) {
        adder.setTapPosition(2)
                .setLowTapPosition(1)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .beginStep().setRho(1.f).setAlpha(-50f).setR(0.1f).setX(0.1f).setG(0.1f).setB(0.1f).endStep()
                .beginStep().setRho(1.f).setAlpha(-25f).setR(0.1f).setX(0.1f).setG(0.1f).setB(0.1f).endStep()
                .add();
    }
}
