/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class KinsolStatusTest {

    @Test
    void test() {
        assertSame(KinsolStatus.KIN_INITIAL_GUESS_OK, KinsolStatus.fromValue(1));
        KinsolException e = assertThrows(KinsolException.class, () -> KinsolStatus.fromValue(1000));
        assertEquals("Unkown solver status value: 1000", e.getMessage());
    }
}
