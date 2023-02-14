/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteMeasurementsXmlTest extends AbstractXmlConverterTest {

    @Test
    void test() throws IOException {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Switch sw = network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR");
        sw.newExtension(DiscreteMeasurementsAdder.class).add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_SW_POS")
                .setType(DiscreteMeasurement.Type.SWITCH_POSITION)
                .setValue("CLOSED")
                .setValid(false)
                .putProperty("source", "test")
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.newExtension(DiscreteMeasurementsAdder.class).add();
        twt.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_TAP_POS")
                .setType(DiscreteMeasurement.Type.TAP_POSITION)
                .setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)
                .setValue(15)
                .setValid(true)
                .putProperty("source", "test2")
                .add();

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "disMeasRef.xiidm");
        roundTripVersionedXmlFromMinToCurrentVersionTest("disMeasRef.xiidm", IidmXmlVersion.V_1_5);
    }
}
