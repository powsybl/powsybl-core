/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.events;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkEventTest {

    @Test
    void test() {
        assertEquals(List.of(new RemovalNetworkEvent("a", true)),
                     List.of(new RemovalNetworkEvent("a", true)));
        assertEquals(List.of(new UpdateNetworkEvent("a", "dd", null, "before", "after")),
                     List.of(new UpdateNetworkEvent("a", "dd", null, "before", "after")));
    }
}
