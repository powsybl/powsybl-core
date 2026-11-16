/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class FictitiousInjectionsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void testBb() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00+01:00"));
        network.getBusBreakerView().getBus("NGEN").setFictitiousP0(1.0).setFictitiousQ0(2.0);
        network.getBusBreakerView().getBus("NLOAD").setFictitiousP0(3.0);
        network.getBusBreakerView().getBus("NHV1").setFictitiousQ0(4.0);
        allFormatsRoundTripTest(network, "eurostag-fict-inj.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("eurostag-fict-inj.xml", IidmVersion.V_1_8);
    }

    @Test
    void testNb() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.getVoltageLevel("C").getNodeBreakerView()
                .setFictitiousP0(0, 1.0)
                .setFictitiousQ0(1, 2.0)
                .setFictitiousP0(2, 3.0)
                .setFictitiousQ0(2, 4.0);
        allFormatsRoundTripTest(network, "fictitiousSwitchRef-fict-inj.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("fictitiousSwitchRef-fict-inj.xml", IidmVersion.V_1_8);
    }
}
