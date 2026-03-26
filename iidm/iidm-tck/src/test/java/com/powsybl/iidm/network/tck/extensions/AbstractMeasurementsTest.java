/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractMeasurementsTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        load.getExtension(Measurements.class)
                .newMeasurement()
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
                .putProperty("source", "test2")
                .add();
        twt.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_TWT_Q_2")
                .setEnsureIdUnicity(true)
                .setType(Measurement.Type.REACTIVE_POWER)
                .setSide(ThreeSides.ONE)
                .setValue(605.2)
                .setValid(true)
                .putProperty("source", "test3")
                .putProperty("other", "test4")
                .add();

        Measurements<Load> loadMeasurements = load.getExtension(Measurements.class);
        assertNotNull(loadMeasurements);
        assertNull(loadMeasurements.getMeasurement("id"));
        assertEquals(1, loadMeasurements.getMeasurements().size());
        Measurement loadMeas = loadMeasurements.getMeasurements().iterator().next();
        assertNull(loadMeas.getId());
        assertEquals(Measurement.Type.ACTIVE_POWER, loadMeas.getType());
        assertNull(loadMeas.getSide());
        assertEquals(580.0, loadMeas.getValue(), 0.0);
        assertEquals(5.0, loadMeas.getStandardDeviation(), 0.0);
        assertFalse(loadMeas.isValid());
        assertEquals(1, loadMeas.getPropertyNames().size());
        assertEquals("test", loadMeas.getProperty("source"));
        assertNull(loadMeas.getProperty("other"));

        loadMeas.remove();
        assertTrue(loadMeasurements.getMeasurements().isEmpty());
        loadMeasurements.cleanIfEmpty();
        assertNull(load.getExtension(Measurements.class));

        Measurements<TwoWindingsTransformer> twtMeasurements = twt.getExtension(Measurements.class);
        assertNotNull(twtMeasurements);
        assertEquals(2, twtMeasurements.getMeasurements().size());
        Measurement twtQ1 = twtMeasurements.getMeasurement("MEAS_TWT_Q_2#0");
        assertNotNull(twtQ1);
        assertEquals("MEAS_TWT_Q_2#0", twtQ1.getId());
        assertEquals(Measurement.Type.REACTIVE_POWER, twtQ1.getType());
        assertEquals(ThreeSides.ONE, twtQ1.getSide());
        assertEquals(605.2, twtQ1.getValue(), 0.0);
        assertTrue(Double.isNaN(twtQ1.getStandardDeviation()));
        assertTrue(twtQ1.isValid());
        assertEquals(2, twtQ1.getPropertyNames().size());
        assertEquals("test3", twtQ1.getProperty("source"));
        assertEquals("test4", twtQ1.getProperty("other"));
        Measurement twtQ2 = twtMeasurements.getMeasurement("MEAS_TWT_Q_2");
        assertNotNull(twtQ2);
        assertEquals("MEAS_TWT_Q_2", twtQ2.getId());
        assertEquals(Measurement.Type.REACTIVE_POWER, twtQ2.getType());
        assertEquals(ThreeSides.TWO, twtQ2.getSide());
        assertEquals(-600.07, twtQ2.getValue(), 0.0);
        assertEquals(10.2, twtQ2.getStandardDeviation(), 0.0);
        assertTrue(twtQ2.isValid());
        assertEquals(1, twtQ2.getPropertyNames().size());
        assertEquals("test2", twtQ2.getProperty("source"));
        assertNull(twtQ2.getProperty("other"));
        twtQ2.putProperty("other", "test5");
        assertEquals(2, twtQ2.getPropertyNames().size());
        assertEquals("test5", twtQ2.getProperty("other"));

        twtMeasurements.cleanIfEmpty();
        assertNotNull(twt.getExtension(Measurements.class));
    }

    @Test
    void setValueAndValidity() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        Measurement measurement = load.getExtension(Measurements.class)
                .newMeasurement()
                .setType(Measurement.Type.OTHER)
                .setValue(Double.NaN)
                .setStandardDeviation(1.0)
                .setValid(false)
                .add();

        measurement.setValueAndValidity(200.0d, true);
        assertEquals(200.0d, measurement.getValue());
        assertTrue(measurement.isValid());

        measurement.setValueAndValidity(Double.NaN, false);
        assertFalse(measurement.isValid());

        assertThrows(PowsyblException.class, () -> measurement.setValueAndValidity(Double.NaN, true));
    }
}
