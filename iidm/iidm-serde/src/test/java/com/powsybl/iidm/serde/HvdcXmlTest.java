/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class HvdcXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripLccTest() throws IOException {
        allFormatsRoundTripTest(HvdcTestNetwork.createLcc(), "LccRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("LccRoundTripRef.xml");
    }

    @Test
    void roundTripVscTest() throws IOException {
        allFormatsRoundTripTest(HvdcTestNetwork.createVsc(), "VscRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("VscRoundTripRef.xml");
    }
}
