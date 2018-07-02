/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityComputationResultExportersTest {

    @Test
    public void testExporterForFormat() {
        SensitivityComputationResultExporter exporter = SensitivityComputationResultExporters.getExporter("JSON");
        assertNotNull(exporter);
        assertEquals(JsonSensitivityComputationResultExporter.class, exporter.getClass());
    }

    @Test
    public void testExporterForUnavailableFormat() {
        SensitivityComputationResultExporter exporter = SensitivityComputationResultExporters.getExporter("Invalid");
        assertNull(exporter);
    }
}
