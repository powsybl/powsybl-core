/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class OptionalLoadTypeBugTest extends AbstractIidmSerDeTest {

    @Test
    void shouldNotThrowNullPointerExceptionTest() {
        assertNotNull(NetworkSerDe.read(getVersionedNetworkAsStream("optionalLoadTypeBug.xml", IidmVersion.V_1_0)));
        assertNotNull(NetworkSerDe.read(getVersionedNetworkAsStream("optionalLoadTypeBug.xml", CURRENT_IIDM_VERSION)));
    }
}
