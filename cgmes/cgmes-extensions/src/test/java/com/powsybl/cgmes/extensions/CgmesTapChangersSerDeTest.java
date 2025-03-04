/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CgmesTapChangersSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWith3wTransformer();
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T12:01:34.831Z"));
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer("NGEN_NHV1");
        CgmesTapChangers<TwoWindingsTransformer> ctc2wt = ((CgmesTapChangersAdder<TwoWindingsTransformer>) twoWT.newExtension(CgmesTapChangersAdder.class)).add();
        ctc2wt.newTapChanger()
                .setId("tc1")
                .setControlId("control1")
                .setStep(1)
                .setType("type1")
                .setHiddenStatus(false)
                .add();
        ThreeWindingsTransformer threeWT = network.getThreeWindingsTransformer("NGEN_V2_NHV1");
        CgmesTapChangers<ThreeWindingsTransformer> ctc3wt = ((CgmesTapChangersAdder<ThreeWindingsTransformer>) threeWT.newExtension(CgmesTapChangersAdder.class)).add();
        ctc3wt.newTapChanger()
                .setId("tc2")
                .setCombinedTapChangerId("ctc2")
                .setStep(1)
                .setControlId("control2")
                .setType("type2")
                .setHiddenStatus(true)
                .add();
        allFormatsRoundTripTest(network, "/eurostag_cgmes_tap_changers.xml");
    }
}
