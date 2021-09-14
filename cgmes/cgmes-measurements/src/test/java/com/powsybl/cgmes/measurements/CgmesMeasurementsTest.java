/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesMeasurementsTest {

    @Test
    public void testBusBranch() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);

        Measurements<Line> measExt = network.getLine("_b58bf21a-096a-4dae-9a01-3f03b60c24c7").getExtension(Measurements.class);
        assertNotNull(measExt);
        assertEquals(2, measExt.getMeasurements().size());

        Measurement meas1 = measExt.getMeasurement("test_analog_1");
        assertNotNull(meas1);
        assertEquals(Measurement.Side.TWO, meas1.getSide());
        assertTrue(Double.isNaN(meas1.getValue()));
        assertTrue(Double.isNaN(meas1.getStandardDeviation()));
        assertFalse(meas1.isValid());
        assertEquals(Measurement.Type.OTHER, meas1.getType());
        assertEquals(1, meas1.getPropertyNames().size());
        String property = meas1.getProperty("type");
        assertNotNull(property);
        assertEquals("LineCurrent", property);
        Measurement meas2 = measExt.getMeasurement("test_analog_2");
        assertNotNull(meas2);
        assertEquals(Measurement.Side.TWO, meas2.getSide());
        assertTrue(Double.isNaN(meas2.getValue()));
        assertTrue(Double.isNaN(meas2.getStandardDeviation()));
        assertFalse(meas2.isValid());
        assertEquals(Measurement.Type.ANGLE, meas2.getType());
        assertTrue(meas2.getPropertyNames().isEmpty());
        assertTrue(measExt.getMeasurements(Measurement.Type.ANGLE).contains(meas2));
        assertEquals(1, measExt.getMeasurements(Measurement.Type.ANGLE).size());

        Measurements<Generator> measExt2 = network.getGenerator("_3a3b27be-b18b-4385-b557-6735d733baf0").getExtension(Measurements.class);
        assertNotNull(measExt2);
        assertEquals(1, measExt2.getMeasurements().size());
        Measurement meas3 = measExt2.getMeasurement("test_analog_3");
        assertNotNull(meas3);
        assertEquals(Measurement.Type.ANGLE, meas3.getType());
        assertNull(meas3.getSide());
        assertTrue(Double.isNaN(meas3.getValue()));
        assertTrue(Double.isNaN(meas3.getStandardDeviation()));
        assertFalse(meas3.isValid());
        assertTrue(meas3.getPropertyNames().isEmpty());

        DiscreteMeasurements<TwoWindingsTransformer> discMeasExt = network.getTwoWindingsTransformer("_b94318f6-6d24-4f56-96b9-df2531ad6543").getExtension(DiscreteMeasurements.class);
        assertNotNull(discMeasExt);
        assertEquals(2, discMeasExt.getDiscreteMeasurements().size());

        DiscreteMeasurement discrMeas1 = discMeasExt.getDiscreteMeasurement("test_discrete_1");
        assertNotNull(discrMeas1);
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, discrMeas1.getType());
        assertEquals(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER, discrMeas1.getTapChanger());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas1.getValueType());
        assertNull(discrMeas1.getValueAsString());
        try {
            discrMeas1.getValueAsBoolean();
            fail();
        } catch (PowsyblException e) {
            // Ignore
        }
        try {
            discrMeas1.getValueAsInt();
            fail();
        } catch (PowsyblException e) {
            // Ignore
        }
        assertFalse(discrMeas1.isValid());
        assertTrue(discrMeas1.getPropertyNames().isEmpty());

        DiscreteMeasurement discrMeas2 = discMeasExt.getDiscreteMeasurement("test_discrete_2");
        assertNotNull(discrMeas2);
        assertEquals(DiscreteMeasurement.Type.OTHER, discrMeas2.getType());
        assertNull(discrMeas2.getTapChanger());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas2.getValueType());
        assertNull(discrMeas2.getValueAsString());
        assertFalse(discrMeas2.isValid());
        assertEquals(1, discrMeas2.getPropertyNames().size());
        String property2 = discrMeas2.getProperty("type");
        assertNotNull(property2);
        assertEquals("TestType", property2);

        DiscreteMeasurements<TwoWindingsTransformer> discMeasExt2 = network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getExtension(DiscreteMeasurements.class);
        assertNotNull(discMeasExt2);
        assertEquals(1, discMeasExt2.getDiscreteMeasurements().size());

        DiscreteMeasurement discrMeas3 = discMeasExt2.getDiscreteMeasurement("test_discrete_3");
        assertNotNull(discrMeas3);
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, discrMeas3.getType());
        assertEquals(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER, discrMeas3.getTapChanger());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas3.getValueType());
        assertNull(discrMeas3.getValueAsString());
        assertFalse(discrMeas3.isValid());
        assertTrue(discrMeas3.getPropertyNames().isEmpty());
    }

    @Test
    public void testNodeBreaker() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);

        VoltageLevel voltageLevel = network.getVoltageLevel("_a43d15db-44a6-4fda-a525-2402ff43226f");
        assertTrue(voltageLevel.hasProperty("CGMES.Analog_Angle"));
        assertEquals("analog", voltageLevel.getProperty("CGMES.Analog_Angle"));

        DiscreteMeasurements<VoltageLevel> ext = voltageLevel.getExtension(DiscreteMeasurements.class);
        assertNotNull(ext);
        assertEquals(1, ext.getDiscreteMeasurements().size());

        DiscreteMeasurement meas = ext.getDiscreteMeasurement("discrete");
        assertNotNull(meas);
        assertEquals(DiscreteMeasurement.Type.OTHER, meas.getType());
        assertEquals(DiscreteMeasurement.ValueType.STRING, meas.getValueType());
        assertNull(meas.getValueAsString());
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        String property = meas.getProperty("type");
        assertEquals("TestType", property);
    }
}
