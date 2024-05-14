/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class FictitiousSwitchXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripTest(FictitiousSwitchFactory.create(), "fictitiousSwitchRef.xml", CURRENT_IIDM_VERSION);

        //backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("fictitiousSwitchRef.xml");
    }

}
