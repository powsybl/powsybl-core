/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
class SectionModificationTest {

    private Network network;
    private ShuntCompensator shunt;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        assertTrue(network.getShuntCompensatorCount() > 0);
        shunt = network.getShuntCompensators().iterator().next();
        shunt.setSectionCount(0);
    }

    @Test
    void testConstructorCoherence() {
        String id = shunt.getId();
        assertThrows(PowsyblException.class, () -> new SectionModification(id, -3),
                "Negative value should not be accepted.");
        assertThrows(NullPointerException.class, () -> new SectionModification(null, 1),
                "Null id value should not be accepted.");
    }

    @Test
    void testApplyChecks() {
        SectionModification modif = new SectionModification(shunt.getId(), 1);
        assertDoesNotThrow(() -> modif.apply(network, true, Reporter.NO_OP));
        SectionModification modif1 = new SectionModification("UNKNOWN_ID", 1);
        assertThrows(PowsyblException.class, () -> modif1.apply(network, true, Reporter.NO_OP),
                "An invalid ID should fail to apply.");
        SectionModification modif2 = new SectionModification(shunt.getId(), shunt.getMaximumSectionCount() + 1);
        assertThrows(PowsyblException.class, () -> modif2.apply(network, true, Reporter.NO_OP),
                "Trying to set the number of section outside of range should not be accepted.");
        assertEquals(1, shunt.getSectionCount(), " Failed apply should not modify the value");
    }
}
