package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubstationsTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getLine("NHV1_NHV2_1").remove();
        network.getLine("NHV1_NHV2_2").remove();
        assertDoesNotThrow(() -> Substations.checkRemovability(network.getSubstation("P1")));
    }

    @Test
    void failCheckRemovabilityBecauseOfBranches() {
        Network network = EurostagTutorialExample1Factory.create();
        assertFail(() -> Substations.checkRemovability(network.getSubstation("P1")));
        assertFail(() -> Substations.checkRemovability(network.getSubstation("P2")));
    }

    @Test
    void failCheckRemovabilityBecauseOfHvdcLines() {
        Network network = HvdcTestNetwork.createLcc();
        assertFail(() -> Substations.checkRemovability(network.getSubstation("S1")));
        assertFail(() -> Substations.checkRemovability(network.getSubstation("S2")));
    }

    private void assertFail(Executable executable) {
        PowsyblException e = Assertions.assertThrows(PowsyblException.class, executable);
        assertTrue(e.getMessage().contains("is still connected to another substation"));
    }

}
