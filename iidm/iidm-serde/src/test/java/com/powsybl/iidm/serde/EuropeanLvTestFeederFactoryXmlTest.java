/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.test.EuropeanLvTestFeederFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class EuropeanLvTestFeederFactoryXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        roundTripXmlTest(EuropeanLvTestFeederFactory.create(),
            (n, xmlFile) -> NetworkSerDe.writeAndValidate(n, new ExportOptions().setSorted(true), xmlFile),
            NetworkSerDe::read,
            getVersionedNetworkPath("europeanLvTestFeederRef.xml", CURRENT_IIDM_XML_VERSION));
    }
}
