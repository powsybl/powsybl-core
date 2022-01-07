/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import static org.junit.Assert.assertEquals;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TapChangerAttributeTest {

    @Test
    public void testTapChangerAttributeName() {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");

        // Check name for two winding transformers
        TwoWindingsTransformer twt2 = createTwoWindingsTransformer(substation);
        createPhaseTapChanger(twt2);
        createRatioTapChanger(twt2);
        assertEquals("phaseTapChanger", ((AbstractTapChanger) twt2.getPhaseTapChanger()).getTapChangerAttribute());
        assertEquals("ratioTapChanger", ((AbstractTapChanger) twt2.getRatioTapChanger()).getTapChangerAttribute());

        // Check name for three winding transformers
        ThreeWindingsTransformer twt3 = createThreeWindingsTransformer(substation);
        createRatioTapChanger(twt3.getLeg2());
        createRatioTapChanger(twt3.getLeg3());
        assertEquals("ratioTapChanger2",
            ((AbstractTapChanger) twt3.getLeg2().getRatioTapChanger()).getTapChangerAttribute());
        assertEquals("ratioTapChanger3",
            ((AbstractTapChanger) twt3.getLeg3().getRatioTapChanger()).getTapChangerAttribute());
    }

    private ThreeWindingsTransformer createThreeWindingsTransformer(Substation substation) {
        return substation.newThreeWindingsTransformer()
            .setId("twt3")
            .setName("twt3_name")
            .newLeg1()
            .setR(1.3)
            .setX(1.4)
            .setG(1.6)
            .setB(1.7)
            .setRatedU(1.1)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .setBus("busA")
            .add()
            .newLeg2()
            .setR(2.03)
            .setX(2.04)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(2.05)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(3.5)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .add();
    }

    private TwoWindingsTransformer createTwoWindingsTransformer(Substation substation) {
        return substation.newTwoWindingsTransformer()
            .setId("twt2")
            .setName("twt2_name")
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setRatedU1(5.0)
            .setRatedU2(6.0)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
    }

    private void createPhaseTapChanger(PhaseTapChangerHolder ptch) {
        ptch.newPhaseTapChanger()
            .setTapPosition(1)
            .setLowTapPosition(0)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
            .beginStep()
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setAlpha(5.0)
            .setRho(6.0)
            .endStep()
            .beginStep()
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setAlpha(5.0)
            .setRho(6.0)
            .endStep()
            .add();
    }

    private void createRatioTapChanger(RatioTapChangerHolder rtch) {
        rtch.newRatioTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(false)
            .beginStep()
            .setR(39.78473)
            .setX(39.784725)
            .setG(0.0)
            .setB(0.0)
            .setRho(1.0)
            .endStep()
            .beginStep()
            .setR(39.78474)
            .setX(39.784726)
            .setG(0.0)
            .setB(0.0)
            .setRho(1.0)
            .endStep()
            .beginStep()
            .setR(39.78475)
            .setX(39.784727)
            .setG(0.0)
            .setB(0.0)
            .setRho(1.0)
            .endStep()
            .add();
    }
}
