/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.ampl.converter.AmplExportConfig.ExportActionType;
import com.powsybl.ampl.converter.AmplExportConfig.ExportScope;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class AmplExportConfigTest {

    @Test
    public void test() {
        AmplExportConfig config = new AmplExportConfig(ExportScope.ALL, true, ExportActionType.CURATIVE);

        assertEquals(ExportScope.ALL, config.getExportScope());
        config.setExportScope(ExportScope.ONLY_MAIN_CC);
        assertEquals(ExportScope.ONLY_MAIN_CC, config.getExportScope());
        try {
            config.setExportScope(null);
            fail();
        } catch (NullPointerException e) {
            // NullPointerException is expected here
        }

        assertTrue(config.isExportXNodes());
        config.setExportXNodes(false);
        assertFalse(config.isExportXNodes());

        assertEquals(ExportActionType.CURATIVE, config.getActionType());
        config.setActionType(ExportActionType.PREVENTIVE);
        assertEquals(ExportActionType.PREVENTIVE, config.getActionType());
        try {
            config.setActionType(null);
        } catch (NullPointerException e) {
            // NullPointerException is expected here
        }

        assertFalse(config.isExportRatioTapChangerVoltageTarget());
        config.setExportRatioTapChangerVoltageTarget(true);
        assertTrue(config.isExportRatioTapChangerVoltageTarget());

        assertFalse(config.isSpecificCompatibility());
        config.setSpecificCompatibility(true);
        assertTrue(config.isSpecificCompatibility());
    }
}
