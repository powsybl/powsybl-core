/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class PhaseShifterTest {
    private Network network;
    private TwoWindingsTransformer twoWindingsTransformer;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        twoWindingsTransformer = network.getTwoWindingsTransformerStream().findAny().orElseThrow();
        assertTrue(twoWindingsTransformer.hasPhaseTapChanger());
        assertTrue(twoWindingsTransformer.hasRatioTapChanger());
        twoWindingsTransformer.getPhaseTapChanger()
            .setTapPosition(twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition());
        twoWindingsTransformer.getRatioTapChanger()
            .setTapPosition(twoWindingsTransformer.getRatioTapChanger().getHighTapPosition());
    }

    @Test
    void testHasImpactSetAsFixedTap() {
        NetworkModification modification1 = new PhaseShifterSetAsFixedTap("UNKNOWN_ID", 1);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new PhaseShifterSetAsFixedTap("TWT", 32);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new PhaseShifterSetAsFixedTap("TWT", 12);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        twoWindingsTransformer.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        NetworkModification modification4 =  new PhaseShifterSetAsFixedTap("TWT", 32);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        twoWindingsTransformer.getRatioTapChanger().setRegulating(false);
        twoWindingsTransformer.getPhaseTapChanger().setRegulationValue(225);
        twoWindingsTransformer.getPhaseTapChanger().setTargetDeadband(300);
        twoWindingsTransformer.getPhaseTapChanger().setRegulating(true);
        NetworkModification modification5 =  new PhaseShifterSetAsFixedTap("TWT", 32);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));
    }

    @Test
    void testHasImpactShiftTap() {
        NetworkModification modification1 = new PhaseShifterShiftTap("UNKNOWN_ID", 1);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new PhaseShifterShiftTap("TWT", 0);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new PhaseShifterShiftTap("TWT", -1);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        twoWindingsTransformer.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        NetworkModification modification4 =  new PhaseShifterShiftTap("TWT", 1);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        twoWindingsTransformer.getRatioTapChanger().setRegulating(false);
        twoWindingsTransformer.getPhaseTapChanger().setRegulationValue(225);
        twoWindingsTransformer.getPhaseTapChanger().setTargetDeadband(300);
        twoWindingsTransformer.getPhaseTapChanger().setRegulating(true);
        NetworkModification modification5 =  new PhaseShifterShiftTap("TWT", 1);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));
    }

}
