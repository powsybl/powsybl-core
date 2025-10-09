/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.extensions.DiscreteMeasurement.ValueType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractDiscreteMeasurementsTest {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Switch sw = network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR");
        sw.newExtension(DiscreteMeasurementsAdder.class).add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setType(DiscreteMeasurement.Type.SWITCH_POSITION)
                .setId("IS_FICT")
                .setValue("CLOSED")
                .setValid(false)
                .putProperty("source", "test")
                .putProperty("other", "test3")
                .add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("IS_FICT")
                .setEnsureIdUnicity(true)
                .setType(DiscreteMeasurement.Type.OTHER)
                .setValue(false)
                .setValid(true)
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.newExtension(DiscreteMeasurementsAdder.class).add();
        twt.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_TAP_POS")
                .setType(DiscreteMeasurement.Type.TAP_POSITION)
                .setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)
                .setValue(15)
                .putProperty("source", "test2")
                .add();

        DiscreteMeasurements<Switch> swDisMeasurements = sw.getExtension(DiscreteMeasurements.class);
        assertNotNull(swDisMeasurements);
        assertEquals(2, swDisMeasurements.getDiscreteMeasurements().size());
        for (DiscreteMeasurement meas : swDisMeasurements.getDiscreteMeasurements()) {
            if ("IS_FICT".equals(meas.getId())) {
                assertEquals(DiscreteMeasurement.Type.SWITCH_POSITION, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals(STRING, meas.getValueType());
                assertEquals("CLOSED", meas.getValueAsString());
                assertFalse(meas.isValid());
                assertEquals(2, meas.getPropertyNames().size());
                assertEquals("test", meas.getProperty("source"));
                assertEquals("test3", meas.getProperty("other"));
            } else {
                assertEquals("IS_FICT#0", meas.getId());
                assertEquals(DiscreteMeasurement.Type.OTHER, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals(BOOLEAN, meas.getValueType());
                assertFalse(meas.getValueAsBoolean());
                assertTrue(meas.isValid());
                assertTrue(meas.getPropertyNames().isEmpty());
                assertNull(meas.getProperty("source"));
                meas.putProperty("source", "test4");
                assertEquals(1, meas.getPropertyNames().size());
                assertEquals("test4", meas.getProperty("source"));

                meas.setValue("CHANGED VALUE");
                assertEquals(STRING, meas.getValueType());
                assertEquals("CHANGED VALUE", meas.getValueAsString());
            }
        }

        swDisMeasurements.cleanIfEmpty();
        assertNotNull(sw.getExtension(DiscreteMeasurements.class));

        DiscreteMeasurements<TwoWindingsTransformer> twtDisMeasurements = twt.getExtension(DiscreteMeasurements.class);
        assertNotNull(twtDisMeasurements);
        assertEquals(1, twtDisMeasurements.getDiscreteMeasurements().size());
        DiscreteMeasurement ptcPos = twtDisMeasurements.getDiscreteMeasurement("DIS_MEAS_TAP_POS");
        assertNotNull(ptcPos);
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, ptcPos.getType());
        assertEquals(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER, ptcPos.getTapChanger());
        assertEquals(INT, ptcPos.getValueType());
        assertEquals(15, ptcPos.getValueAsInt());
        assertTrue(ptcPos.isValid());
        assertEquals(1, ptcPos.getPropertyNames().size());
        assertEquals("test2", ptcPos.getProperty("source"));

        ptcPos.remove();
        assertTrue(twtDisMeasurements.getDiscreteMeasurements().isEmpty());
        twtDisMeasurements.cleanIfEmpty();
        assertNull(twt.getExtension(DiscreteMeasurements.class));
    }
}
