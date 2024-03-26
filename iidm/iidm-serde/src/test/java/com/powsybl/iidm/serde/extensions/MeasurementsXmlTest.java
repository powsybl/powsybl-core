/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class MeasurementsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        load.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_LOAD_P")
                .setType(Measurement.Type.ACTIVE_POWER)
                .setValue(580.0)
                .setStandardDeviation(5.0)
                .setValid(false)
                .putProperty("source", "test")
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        twt.newExtension(MeasurementsAdder.class).add();
        twt.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_TWT_Q_2")
                .setType(Measurement.Type.REACTIVE_POWER)
                .setSide(ThreeSides.TWO)
                .setValue(-600.07)
                .setStandardDeviation(10.2)
                .setValid(true)
                .add();
        twt.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_TWT_Q_1")
                .setType(Measurement.Type.REACTIVE_POWER)
                .setSide(ThreeSides.ONE)
                .setValue(605.2)
                .setStandardDeviation(9.7)
                .setValid(true)
                .putProperty("source", "test2")
                .add();

        allFormatsRoundTripTest(network, "measRef.xiidm", IidmSerDeConstants.CURRENT_IIDM_VERSION);
    }
}
