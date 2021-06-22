/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractDiscreteMeasurementsTest {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Switch sw = network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR");
        sw.newExtension(DiscreteMeasurementsAdder.class).add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setType(DiscreteMeasurement.Type.SWITCH_POSITION)
                .setStringValue("CLOSED")
                .setIntValue(1)
                .setValid(false)
                .putProperty("source", "test")
                .putProperty("other", "test3")
                .add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("FICT")
                .setType(DiscreteMeasurement.Type.OTHER)
                .setStringValue("ADDITIONAL COMMENT")
                .setValid(true)
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.newExtension(DiscreteMeasurementsAdder.class).add();
        twt.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_TAP_POS")
                .setType(DiscreteMeasurement.Type.TAP_POSITION)
                .setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)
                .setIntValue(15)
                .putProperty("source", "test2")
                .add();

        DiscreteMeasurements<Switch> swDisMeasurements = sw.getExtension(DiscreteMeasurements.class);
        assertNotNull(swDisMeasurements);
        assertEquals(2, swDisMeasurements.getDiscreteMeasurements().size());
        for (DiscreteMeasurement meas : swDisMeasurements.getDiscreteMeasurements()) {
            if (meas.getId() == null) {
                assertEquals(DiscreteMeasurement.Type.SWITCH_POSITION, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals("CLOSED", meas.getValueAsString());
                assertEquals(1, meas.getValueAsInt());
                assertFalse(meas.isValid());
                assertEquals(2, meas.getPropertyNames().size());
                assertEquals("test", meas.getProperty("source"));
                assertEquals("test3", meas.getProperty("other"));
            } else {
                assertEquals("FICT", meas.getId());
                assertEquals(DiscreteMeasurement.Type.OTHER, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals("ADDITIONAL COMMENT", meas.getValueAsString());
                assertEquals(-1, meas.getValueAsInt());
                assertTrue(meas.isValid());
                assertTrue(meas.getPropertyNames().isEmpty());
                assertNull(meas.getProperty("source"));
                meas.putProperty("source", "test4");
                assertEquals(1, meas.getPropertyNames().size());
                assertEquals("test4", meas.getProperty("source"));
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
        assertEquals("15", ptcPos.getValueAsString());
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
