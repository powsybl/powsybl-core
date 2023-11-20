/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.test.AbstractSerializerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class XMLReaderContextTest extends AbstractSerializerTest {

    @Test
    void xmlReaderContextExceptionTest() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new NetworkSerializerReaderContext(null, null));
    }
}
