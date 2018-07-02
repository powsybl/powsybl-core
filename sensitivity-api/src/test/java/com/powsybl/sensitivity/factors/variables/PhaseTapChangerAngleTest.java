/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class PhaseTapChangerAngleTest {

    private static final String VARIABLE_ID = "Variable ID";
    private static final String VARIABLE_NAME = "Variable name";
    private static final String PHASE_TAP_CHANGER_HOLDER_ID = "Phase tap changer holder ID";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullPhaseTapChangerHolderId() {
        exception.expect(NullPointerException.class);
        new PhaseTapChangerAngle(VARIABLE_ID, VARIABLE_NAME, null);
    }

    @Test
    public void getName() {
        PhaseTapChangerAngle phaseTapChangerAngle = new PhaseTapChangerAngle(VARIABLE_ID, VARIABLE_NAME, PHASE_TAP_CHANGER_HOLDER_ID);
        assertEquals(VARIABLE_NAME, phaseTapChangerAngle.getName());
    }

    @Test
    public void getId() {
        PhaseTapChangerAngle phaseTapChangerAngle = new PhaseTapChangerAngle(VARIABLE_ID, VARIABLE_NAME, PHASE_TAP_CHANGER_HOLDER_ID);
        assertEquals(VARIABLE_ID, phaseTapChangerAngle.getId());
    }

    @Test
    public void getPhaseTapChanger() {
        PhaseTapChangerAngle phaseTapChangerAngle = new PhaseTapChangerAngle(VARIABLE_ID, VARIABLE_NAME, PHASE_TAP_CHANGER_HOLDER_ID);
        assertEquals(PHASE_TAP_CHANGER_HOLDER_ID, phaseTapChangerAngle.getPhaseTapChangerHolderId());
    }
}
