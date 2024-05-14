/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesControlAreasSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2021-02-02T09:27:39.856+01:00"));
        network.getVoltageLevel("VLGEN")
                .getBusBreakerView()
                .newBus()
                .setId("NDL")
                .add();
        network.getVoltageLevel("VLGEN")
                .newDanglingLine()
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setB(0.0)
                .setG(0.0)
                .setId("DL")
                .setConnectableBus("NDL")
                .setBus("NDL")
                .add();
        network.newExtension(CgmesControlAreasAdder.class).add();
        network.getExtension(CgmesControlAreas.class).newCgmesControlArea()
                .setId("cgmesControlAreaId")
                .setName("cgmesControlAreaName")
                .setEnergyIdentificationCodeEic("energyIdentCodeEic")
                .setNetInterchange(100.0)
                .add()
                .add(network.getLine("NHV1_NHV2_1").getTerminal1());
        network.getExtension(CgmesControlAreas.class).getCgmesControlArea("cgmesControlAreaId").add(network.getDanglingLine("DL").getBoundary());

        allFormatsRoundTripTest(network, "/eurostag_cgmes_control_area.xml");
    }
}
