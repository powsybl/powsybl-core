/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class PropertiesSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("eurostag-tutorial-example1-properties.xml");
    }
}
