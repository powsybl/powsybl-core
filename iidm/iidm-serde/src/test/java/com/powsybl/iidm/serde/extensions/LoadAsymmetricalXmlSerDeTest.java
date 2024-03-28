/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadAsymmetricalXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        var load = network.getLoad("LOAD");
        assertNotNull(load);
        LoadAsymmetrical asym = load.newExtension(LoadAsymmetricalAdder.class)
                .withConnectionType(LoadConnectionType.DELTA)
                .withDeltaPa(-1)
                .withDeltaQa(1)
                .withDeltaPc(-2)
                .withDeltaQc(2)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/asymmetrical/loadAsymmetricalRef.xml");

        var load2 = network2.getLoad("LOAD");
        assertNotNull(load2);
        LoadAsymmetrical asym2 = load2.getExtension(LoadAsymmetrical.class);
        assertNotNull(asym2);

        assertSame(LoadConnectionType.DELTA, asym2.getConnectionType());
        assertEquals(asym.getDeltaPa(), asym2.getDeltaPa(), 0);
        assertEquals(asym.getDeltaQa(), asym2.getDeltaQa(), 0);
        assertEquals(asym.getDeltaPb(), asym2.getDeltaPb(), 0);
        assertEquals(asym.getDeltaQb(), asym2.getDeltaQb(), 0);
        assertEquals(asym.getDeltaPc(), asym2.getDeltaPc(), 0);
        assertEquals(asym.getDeltaQc(), asym2.getDeltaQc(), 0);
    }
}
