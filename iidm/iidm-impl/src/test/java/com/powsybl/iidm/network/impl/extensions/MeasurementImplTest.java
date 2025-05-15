package com.powsybl.iidm.network.impl.extensions;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;

class MeasurementImplTest {

    @Test
    void setValueAndValidity() {
        MeasurementsImpl<? extends Connectable<?>> measurements = new MeasurementsImpl<>();
        Measurement measurement;
        measurement = new MeasurementImpl(measurements, "heidi", Measurement.Type.OTHER, Map.of(), Double.NaN, 1.0, false, null);

        measurement.setValue(1.0);
        assertFalse(measurement.isValid());
        measurement.setValid(true);
        assertTrue(measurement.isValid());
        assertThrows(PowsyblException.class, () -> measurement.setValue(Double.NaN));

        measurement.setValid(false);
        measurement.setValue(Double.NaN);
        assertThrows(PowsyblException.class, () -> measurement.setValid(true));

        measurement.setValueAndValidity(200.0d, true);
        assertEquals(200.0d, measurement.getValue());
        assertTrue(measurement.isValid());

        measurement.setValueAndValidity(Double.NaN, false);
        assertFalse(measurement.isValid());

        assertThrows(PowsyblException.class, () -> measurement.setValueAndValidity(Double.NaN, true));
    }
}
