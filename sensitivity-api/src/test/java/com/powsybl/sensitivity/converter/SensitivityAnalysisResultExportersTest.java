/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisResultExportersTest {

    @Test
    public void testGetFormats() {
        assertEquals("[CSV, JSON]", SensitivityAnalysisResultExporters.getFormats().toString());
    }

    @Test
    public void testGetExporter() {
        assertEquals("CSV", SensitivityAnalysisResultExporters.getExporter("CSV").getFormat());
        assertEquals("JSON", SensitivityAnalysisResultExporters.getExporter("JSON").getFormat());
    }

    @Test
    public void testExporterForUnavailableFormat() {
        SensitivityAnalysisResultExporter exporter = SensitivityAnalysisResultExporters.getExporter("Invalid");
        assertNull(exporter);
    }
}
