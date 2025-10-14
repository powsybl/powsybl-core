/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserve;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserveAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Jacques Borsenberger {literal <jacques.borsenberger at rte-france.com}
 */
class ManualFrequencyRestorationReserveXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2022-04-06T13:43:05.020+02:00"));

        // create substation and line position extensions
        network.getGenerator("GEN").newExtension(ManualFrequencyRestorationReserveAdder.class)
                .withParticipate(true)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/manualFrequencyRestorationReserve.xml", CURRENT_IIDM_VERSION);

        var tertiaryReserve = network2.getGenerator("GEN").getExtension(ManualFrequencyRestorationReserve.class);
        assertNotNull(tertiaryReserve);

    }

}
