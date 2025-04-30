/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.triplestore.api.PropertyBags;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesMeasurementsTest {

    @Test
    void testBusBranch() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);

        Measurements<Line> measExt = network.getLine("b58bf21a-096a-4dae-9a01-3f03b60c24c7").getExtension(Measurements.class);
        assertNotNull(measExt);
        assertEquals(2, measExt.getMeasurements().size());

        Measurement meas1 = measExt.getMeasurement("test_analog_1");
        assertNotNull(meas1);
        assertEquals(ThreeSides.TWO, meas1.getSide());
        assertTrue(Double.isNaN(meas1.getValue()));
        assertTrue(Double.isNaN(meas1.getStandardDeviation()));
        assertFalse(meas1.isValid());
        assertEquals(Measurement.Type.CURRENT, meas1.getType());
        assertEquals(1, meas1.getPropertyNames().size());
        String property = meas1.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("Current", property);
        Measurement meas2 = measExt.getMeasurement("test_analog_2");
        assertNotNull(meas2);
        assertEquals(ThreeSides.TWO, meas2.getSide());
        assertTrue(Double.isNaN(meas2.getValue()));
        assertTrue(Double.isNaN(meas2.getStandardDeviation()));
        assertFalse(meas2.isValid());
        assertEquals(Measurement.Type.ACTIVE_POWER, meas2.getType());
        assertEquals(1, meas2.getPropertyNames().size());
        property = meas2.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("ThreePhaseActivePower", property);
        assertTrue(measExt.getMeasurements(Measurement.Type.ACTIVE_POWER).contains(meas2));
        assertEquals(1, measExt.getMeasurements(Measurement.Type.ACTIVE_POWER).size());

        Measurements<Generator> measExt2 = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getExtension(Measurements.class);
        assertNotNull(measExt2);
        assertEquals(5, measExt2.getMeasurements().size());

        Measurement meas = measExt2.getMeasurement("test_analog_3");
        assertNotNull(meas);
        assertEquals(Measurement.Type.REACTIVE_POWER, meas.getType());
        assertNull(meas.getSide());
        assertTrue(Double.isNaN(meas.getValue()));
        assertTrue(Double.isNaN(meas.getStandardDeviation()));
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        property = meas.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("ThreePhaseReactivePower", property);

        meas = measExt2.getMeasurement("test_analog_4");
        assertNotNull(meas);
        assertEquals(Measurement.Type.VOLTAGE, meas.getType());
        assertNull(meas.getSide());
        assertTrue(Double.isNaN(meas.getValue()));
        assertTrue(Double.isNaN(meas.getStandardDeviation()));
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        property = meas.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("PhaseVoltage", property);

        meas = measExt2.getMeasurement("test_analog_5");
        assertNotNull(meas);
        assertEquals(Measurement.Type.ANGLE, meas.getType());
        assertNull(meas.getSide());
        assertTrue(Double.isNaN(meas.getValue()));
        assertTrue(Double.isNaN(meas.getStandardDeviation()));
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        property = meas.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("Angle", property);

        meas = measExt2.getMeasurement("test_analog_6");
        assertNotNull(meas);
        assertEquals(Measurement.Type.OTHER, meas.getType());
        assertNull(meas.getSide());
        assertTrue(Double.isNaN(meas.getValue()));
        assertTrue(Double.isNaN(meas.getStandardDeviation()));
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        property = meas.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("TestType", property);

        meas = measExt2.getMeasurement("test_analog_7");
        assertNotNull(meas);
        assertEquals(Measurement.Type.FREQUENCY, meas.getType());
        assertNull(meas.getSide());
        assertTrue(Double.isNaN(meas.getValue()));
        assertTrue(Double.isNaN(meas.getStandardDeviation()));
        assertFalse(meas.isValid());
        assertEquals(1, meas.getPropertyNames().size());
        property = meas.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("Frequency", property);

        DiscreteMeasurements<TwoWindingsTransformer> discMeasExt = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543").getExtension(DiscreteMeasurements.class);
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
        assertEquals(1, discrMeas1.getPropertyNames().size());
        property = discrMeas1.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("TapPosition", property);

        DiscreteMeasurement discrMeas2 = discMeasExt.getDiscreteMeasurement("test_discrete_2");
        assertNotNull(discrMeas2);
        assertEquals(DiscreteMeasurement.Type.OTHER, discrMeas2.getType());
        assertNull(discrMeas2.getTapChanger());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas2.getValueType());
        assertNull(discrMeas2.getValueAsString());
        assertFalse(discrMeas2.isValid());
        assertEquals(1, discrMeas2.getPropertyNames().size());
        property = discrMeas2.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("TestType", property);

        DiscreteMeasurements<TwoWindingsTransformer> discMeasExt2 = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getExtension(DiscreteMeasurements.class);
        assertNotNull(discMeasExt2);
        assertEquals(1, discMeasExt2.getDiscreteMeasurements().size());

        DiscreteMeasurement discrMeas3 = discMeasExt2.getDiscreteMeasurement("test_discrete_3");
        assertNotNull(discrMeas3);
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, discrMeas3.getType());
        assertEquals(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER, discrMeas3.getTapChanger());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas3.getValueType());
        assertNull(discrMeas3.getValueAsString());
        assertFalse(discrMeas3.isValid());
        assertEquals(1, discrMeas3.getPropertyNames().size());
        property = discrMeas3.getProperty("cgmesType");
        assertNotNull(property);
        assertEquals("TapPosition", property);
    }

    @Test
    void testNodeBreaker() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);

        VoltageLevel voltageLevel = network.getVoltageLevel("a43d15db-44a6-4fda-a525-2402ff43226f");
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
        String property = meas.getProperty("cgmesType");
        assertEquals("TestType", property);
    }

    @Test
    void testDeprecated() {
        Properties properties = new Properties();
        Network network = new CgmesImport().importData(
                CgmesConformity1ModifiedCatalog.miniNodeBreakerMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);
        PropertyBags pbags = new PropertyBags();
        Map<String, String> mapping = new HashMap<>();
        CgmesAnalogPostProcessor.process(network, "paf", "pif", "pouf", "ActivePower", pbags, mapping);
        CgmesDiscretePostProcessor.process(network, "paf", "pif", "pouf", "ActivePower", pbags, mapping);
        assertTrue(true);
    }
}
