/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.extensions.ConnectablePosition;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public final class TestUtils {

    private TestUtils() {
    }

    public static void assertConnectablePositionEquals(String referenceName, int referencePosition, ConnectablePosition.Direction referenceDirection, ConnectablePosition.Feeder actualFeeder) {
        assertNotNull(actualFeeder);
        assertEquals(referenceDirection, actualFeeder.getDirection());
        assertTrue(actualFeeder.getOrder().isPresent());
        assertEquals(referencePosition, actualFeeder.getOrder().get());
        assertTrue(actualFeeder.getName().isPresent());
        assertEquals(referenceName, actualFeeder.getName().get());
    }
}
