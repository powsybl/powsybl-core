/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ConnectableOrderingXmlTest extends AbstractIidmSerDeTest {

    @Test
    void testRoundTrip() throws IOException {
        ExportOptions exportOptions = new ExportOptions();
        roundTripTest(Network.read("/twtOrdering.xiidm", getNetworkAsStream("/twtOrdering.xiidm")),
                (n, p) -> NetworkSerDe.write(n, exportOptions, p),
                NetworkSerDe::validateAndRead,
                "/twtOrdering.xiidm");
    }

    @Test
    void testCopy() throws IOException {
        Network network = Network.read("/twtOrdering.xiidm", getNetworkAsStream("/twtOrdering.xiidm"));
        Network exportNetwork = NetworkSerDe.copy(network);
        assertEquals(exportNetwork.getTwoWindingsTransformers().toString(), network.getTwoWindingsTransformers().toString());
    }
}
