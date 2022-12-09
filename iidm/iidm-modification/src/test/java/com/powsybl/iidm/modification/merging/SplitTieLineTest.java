/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.merging;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class SplitTieLineTest extends AbstractConverterTest {

    @Test
    public void defaultTest() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWithTieLines();
        SplitTieLine split = new SplitTieLineBuilder()
                .withTieLineId("NHV1_NHV2_1")
                .build();
        split.apply(n);
        roundTripXmlTest(n, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/default-split-tl.xiidm");
    }

    @Test
    public void completeTest() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWithTieLines();
        TieLine tl = (TieLine) n.getLine("NHV1_NHV2_1");
        tl.addAlias("test1");
        tl.addAlias("test2", "type1");
        tl.addAlias("test3", "type2");
        tl.addAlias("test4", "type3");
        tl.addAlias("test5", "type4");
        tl.setProperty("property", "value");
        SplitTieLine split = new SplitTieLineBuilder()
                .withTieLineId(tl.getId())
                .withAliasTypes1("type1")
                .withAliasTypes2("type2", "type3")
                .withDefaultSideForAliases(Branch.Side.ONE)
                .build();
        split.apply(n);
        roundTripXmlTest(n, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/complete-split-tl.xiidm");
    }
}
