/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class BaseVoltagesConfigTest {

    @Test
    void test() {
        BaseVoltagesConfig config = BaseVoltagesConfig.fromInputStream(getClass().getResourceAsStream("/base-voltages.yml"));
        assertNotNull(config);
        assertEquals(Arrays.asList("vl300to500", "vl180to300", "vl120to180", "vl70to120", "vl50to70", "vl30to50", "vl0to30"),
                config.getBaseVoltageNames("Default"));

        BaseVoltageConfig config180to300 = config.getBaseVoltages().get(1);
        assertEquals("vl180to300", config180to300.getName());
        assertEquals("Default", config180to300.getProfile());
        assertEquals("vl180to300", config180to300.getName());
        assertEquals(180, config180to300.getMinValue(), 0);
        assertEquals(300, config180to300.getMaxValue(), 0);

        BaseVoltageConfig config300to500 = config.getBaseVoltages().get(0);
        assertEquals(300, config300to500.getMinValue(), 0);
        assertEquals(500, config300to500.getMaxValue(), 0);

        assertEquals("Default", config.getBaseVoltages().get(2).getProfile());
        assertEquals("Default", config.getDefaultProfile());
        assertEquals(Collections.singletonList("Default"), config.getProfiles());

        assertFalse(config.getBaseVoltageName(500, "Default").isPresent());
        assertEquals("vl300to500", config.getBaseVoltageName(450, "Default").orElseThrow(IllegalStateException::new));
        assertEquals("vl300to500", config.getBaseVoltageName(400, "Default").orElseThrow(IllegalStateException::new));
        assertEquals("vl300to500", config.getBaseVoltageName(300, "Default").orElseThrow(IllegalStateException::new));
        assertEquals("vl180to300", config.getBaseVoltageName(250, "Default").orElseThrow(IllegalStateException::new));
        assertEquals("vl180to300", config.getBaseVoltageName(180, "Default").orElseThrow(IllegalStateException::new));
        assertFalse(config.getBaseVoltageName(700, "Default").isPresent());
        assertFalse(config.getBaseVoltageName(400, "unknownProfile").isPresent());
    }

    @Test
    void testMissingPlatformConfigFile() {
        PlatformConfig platformConfig = new PlatformConfig((ModuleConfigRepository) null, Path.of("./"));
        assertThrows(PowsyblException.class,
            () -> BaseVoltagesConfig.fromPlatformConfig(platformConfig, "unknown.yml"), "No base voltages configuration found");
    }

    @Test
    void testNoConfigDir() {
        PlatformConfig mockedPc = Mockito.mock(PlatformConfig.class);
        Mockito.when(mockedPc.getConfigDir()).thenReturn(Optional.empty());

        PowsyblException e = assertThrows(PowsyblException.class, () -> BaseVoltagesConfig.fromPlatformConfig(mockedPc, "myUnknownFile.yml"));
        assertEquals("No base voltages configuration found in resources: myUnknownFile.yml", e.getMessage());
    }

    @Test
    void testFromPath() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/base-voltages.yml")), fs.getPath("/work/my-base-voltages.yml"));
            Path workDir = fs.getPath("/work");
            BaseVoltagesConfig config = BaseVoltagesConfig.fromPath(workDir, "my-base-voltages.yml");
            assertNotNull(config);
            assertEquals("vl30to50", config.getBaseVoltages().get(5).getName());

            // Testing non-existing path
            assertThrows(PowsyblException.class, () -> BaseVoltagesConfig.fromPath(workDir, "unknown.yml"));
        }
    }

    @Test
    void testEmpty() {
        String empty = "baseVoltages:\ndefaultProfile: \"Def\"";
        BaseVoltagesConfig config = BaseVoltagesConfig.fromInputStream(new ByteArrayInputStream(empty.getBytes()));
        assertNotNull(config);
        assertEquals(Collections.emptyList(), config.getBaseVoltages());
        assertEquals("Def", config.getDefaultProfile());
    }

    @Test
    void testMalformed() {
        String malformed1 = "baseVoltages:";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(malformed1.getBytes());
        YAMLException e1 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream1));
        assertEquals("class com.powsybl.commons.config.BaseVoltagesConfig is missing defaultProfile", e1.getMessage());

        String malformed2 = "baseVoltages:\n" +
                "  - name: \"name1\"\n" +
                "    minValue: 0\n" +
                "    maxValue: 1\n" +
                "    profile: \"Default\"\n" +
                "  - name: \"name2\"\n" +
                "    minValue: 1\n" +
                "    profile: \"Default\"\n" +
                "defaultProfile: \"Default\"";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(malformed2.getBytes());
        YAMLException e2 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream2));
        assertEquals("class com.powsybl.commons.config.BaseVoltageConfig is missing maxValue", e2.getCause().getMessage());

        String malformed3 = "baseVoltages:\n" +
                "  - name: \"name1\"\n" +
                "defaultProfile: \"Default\"";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(malformed3.getBytes());
        YAMLException e3 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream3));
        assertEquals("class com.powsybl.commons.config.BaseVoltageConfig is missing minValue, maxValue, profile",
                e3.getCause().getMessage());

        String malformed4 = "baseVoltages:\n" +
                "  - name: null\n" +
                "    minValue: 1\n" +
                "    maxValue: 2\n" +
                "    profile: \"Default\"\n" +
                "defaultProfile: \"Default\"";
        ByteArrayInputStream inputStream4 = new ByteArrayInputStream(malformed4.getBytes());
        YAMLException e4 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream4));
        assertTrue(e4.getCause().getCause().getCause() instanceof NullPointerException);

        String malformed5 = "baseVoltages:\n" +
                "  - \n" +
                "defaultProfile: \"Default\"";
        ByteArrayInputStream inputStream5 = new ByteArrayInputStream(malformed5.getBytes());
        YAMLException e5 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream5));
        assertEquals("class com.powsybl.commons.config.BaseVoltageConfig is missing name, minValue, maxValue, profile",
                e5.getCause().getMessage());

        String malformed6 = "{baseVoltages, value}:";
        ByteArrayInputStream inputStream6 = new ByteArrayInputStream(malformed6.getBytes());
        YAMLException e6 = assertThrows(YAMLException.class, () -> BaseVoltagesConfig.fromInputStream(inputStream6));
        assertEquals("class com.powsybl.commons.config.BaseVoltagesConfig is missing baseVoltages, defaultProfile", e6.getMessage());
    }

}
