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
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.TwoWindingsTransformer;
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
    public void test() {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseMeasurements().dataSource(),
                NetworkFactory.findDefault(), properties);
        assertNotNull(network);

        Line line = network.getLine("_b58bf21a-096a-4dae-9a01-3f03b60c24c7");
        assertNotNull(line);

        Measurements<Line> measExt = line.getExtension(Measurements.class);
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

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("_b94318f6-6d24-4f56-96b9-df2531ad6543");
        assertNotNull(twt);

        DiscreteMeasurements<TwoWindingsTransformer> discMeasExt = twt.getExtension(DiscreteMeasurements.class);
        assertNotNull(discMeasExt);
        assertEquals(1, discMeasExt.getDiscreteMeasurements().size());

        DiscreteMeasurement discrMeas = discMeasExt.getDiscreteMeasurement("test_discrete");
        assertNotNull(discrMeas);
        assertEquals(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER, discrMeas.getTapChanger());
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, discrMeas.getType());
        assertEquals(DiscreteMeasurement.ValueType.STRING, discrMeas.getValueType());
        assertNull(discrMeas.getValueAsString());
        try {
            discrMeas.getValueAsBoolean();
            fail();
        } catch (PowsyblException e) {
            // Ignore
        }
        try {
            discrMeas.getValueAsInt();
            fail();
        } catch (PowsyblException e) {
            // Ignore
        }
        assertFalse(discrMeas.isValid());
        assertTrue(discrMeas.getPropertyNames().isEmpty());
    }
}
