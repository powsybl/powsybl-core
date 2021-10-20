/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltagesConfigTest {

    @Test
    public void test1() {
        Yaml yaml = new Yaml(new Constructor(BaseVoltagesConfig.class));
        InputStream configInputStream = getClass().getResourceAsStream("/base-voltages.yml");
        BaseVoltagesConfig config = yaml.load(configInputStream);
        assertNotNull(config);
        assertNotNull(config.getBaseVoltages());
        assertEquals(7, config.getBaseVoltages().size());
        BaseVoltageConfig a = config.getBaseVoltages().get(1);
        String b = a.getName();
        assertEquals("vl180to300", config.getBaseVoltages().get(1).getName());
        assertEquals(300, config.getBaseVoltages().get(0).getMinValue(), 0);
        assertEquals(500, config.getBaseVoltages().get(0).getMaxValue(), 0);
        assertEquals("Default", config.getBaseVoltages().get(1).getProfile());
        assertEquals("vl180to300", config.getBaseVoltages().get(1).getName());
        assertEquals(180, config.getBaseVoltages().get(1).getMinValue(), 0);
        assertEquals(300, config.getBaseVoltages().get(1).getMaxValue(), 0);
        assertEquals("Default", config.getBaseVoltages().get(2).getProfile());
        assertEquals("Default", config.getDefaultProfile());
    }

    @Test
    public void test2() {
        BaseVoltagesConfig baseVoltageStyle = BaseVoltagesConfig.fromInputStream(getClass().getResourceAsStream("/base-voltages.yml"));
        // getProfiles
        assertEquals(Collections.singletonList("Default"), baseVoltageStyle.getProfiles());
        // getDefaultProfile
        assertEquals("Default", baseVoltageStyle.getDefaultProfile());
        // getBaseVoltageNames
        assertEquals(Arrays.asList("vl300to500", "vl180to300", "vl120to180", "vl70to120", "vl50to70", "vl30to50", "vl0to30"), baseVoltageStyle.getBaseVoltageNames("Default"));
        // getBaseVoltageName
        assertFalse(baseVoltageStyle.getBaseVoltageName(500, "Default").isPresent());
        assertEquals("vl300to500", baseVoltageStyle.getBaseVoltageName(450, "Default").orElseThrow(AssertionError::new));
        assertEquals("vl300to500", baseVoltageStyle.getBaseVoltageName(400, "Default").orElseThrow(AssertionError::new));
        assertEquals("vl300to500", baseVoltageStyle.getBaseVoltageName(300, "Default").orElseThrow(AssertionError::new));
        assertEquals("vl180to300", baseVoltageStyle.getBaseVoltageName(250, "Default").orElseThrow(AssertionError::new));
        assertEquals("vl180to300", baseVoltageStyle.getBaseVoltageName(180, "Default").orElseThrow(AssertionError::new));
        assertFalse(baseVoltageStyle.getBaseVoltageName(700, "Default").isPresent());
    }

}
