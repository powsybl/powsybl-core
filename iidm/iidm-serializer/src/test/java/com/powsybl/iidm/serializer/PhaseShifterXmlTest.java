/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseShifterXmlTest extends AbstractIidmSerializerTest {
    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("phaseShifterRoundTripRef.xml");

        roundTripXmlTest(PhaseShifterTestCaseFactory.createWithTargetDeadband(),
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("phaseShifterRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));
    }
}
