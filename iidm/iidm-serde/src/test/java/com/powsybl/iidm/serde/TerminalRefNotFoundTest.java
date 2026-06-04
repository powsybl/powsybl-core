/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TerminalRefNotFoundTest extends AbstractIidmSerDeTest {

    @Test
    void test() {
        String expectedError = "Terminal reference identifiable not found: '????'";
        IidmVersion minVersion = IidmVersion.V_1_5;
        Stream.of(IidmVersion.values())
            .filter(v -> v.compareTo(minVersion) >= 0)
            .forEach(v -> {
                PowsyblException exception = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(getVersionedNetworkAsStream("terminalRefNotFound.xiidm", v)));
                assertEquals(expectedError, exception.getMessage());
            });
    }
}
