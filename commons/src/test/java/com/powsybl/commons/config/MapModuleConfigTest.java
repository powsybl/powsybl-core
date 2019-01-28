/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class MapModuleConfigTest {

    protected FileSystem fileSystem;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void emptyTest() {
        // sonar issue
    }

    protected void assertModConfig(ModuleConfig modConfig) {
        Path p = fileSystem.getPath("/tmp");
        Path p2 = fileSystem.getPath("/home");

        //  string tests
        assertEquals("hello", modConfig.getStringProperty("s"));
        assertEquals("oups", modConfig.getStringProperty("s2", "oups"));
        try {
            modConfig.getStringProperty("s2");
            fail();
        } catch (Exception ignored) {
        }

        // int tests
        assertEquals(3, modConfig.getIntProperty("i"));
        try {
            modConfig.getIntProperty("i2");
            fail();
        } catch (Exception ignored) {
        }
        assertFalse(modConfig.getOptionalIntProperty("i2").isPresent());
        assertEquals(4, modConfig.getIntProperty("i2", 4));

        // long tests
        assertEquals(33333333333L, modConfig.getLongProperty("l"));
        try {
            modConfig.getLongProperty("l2");
            fail();
        } catch (Exception ignored) {
        }
        assertTrue(modConfig.getOptionalLongProperty("l").isPresent());
        assertFalse(modConfig.getOptionalLongProperty("l2").isPresent());
        assertEquals(33333333333L, modConfig.getLongProperty("l", 5555555555L));
        assertEquals(5555555555L, modConfig.getLongProperty("l2", 5555555555L));

        // boolean tests
        assertFalse(modConfig.getBooleanProperty("b"));
        try {
            modConfig.getBooleanProperty("b2");
            fail();
        } catch (Exception ignored) {
        }
        assertFalse(modConfig.getOptionalBooleanProperty("b2").isPresent());
        assertTrue(modConfig.getBooleanProperty("b2", true));

        // float tests
        assertFalse(modConfig.getOptionalFloatProperty("f").isPresent());
        assertEquals(1.5f, modConfig.getFloatProperty("f", 1.5f), 0d);
        assertEquals(2.3f, modConfig.getFloatProperty("d"), 0d);

        // double tests
        assertEquals(2.3d, modConfig.getDoubleProperty("d"), 0d);
        try {
            modConfig.getDoubleProperty("d2");
            fail();
        } catch (Exception ignored) {
        }
        assertEquals(4.5d, modConfig.getDoubleProperty("d2", 4.5d), 0d);

        // string list tests
        assertEquals(ArrayList.class, modConfig.getClassProperty("c", List.class));
        assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl1"));
        assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl2"));
        try {
            modConfig.getStringListProperty("sl3");
            fail();
        } catch (Exception ignored) {
        }

        // enum test
        assertEquals(StandardOpenOption.APPEND, modConfig.getEnumProperty("e", StandardOpenOption.class));
        try {
            modConfig.getEnumProperty("e2", StandardOpenOption.class);
            fail();
        } catch (Exception ignored) {
        }

        // enum set test
        assertEquals(EnumSet.of(StandardOpenOption.APPEND, StandardOpenOption.CREATE), modConfig.getEnumSetProperty("el", StandardOpenOption.class));
        try {
            modConfig.getEnumSetProperty("el2", StandardOpenOption.class);
            fail();
        } catch (Exception ignored) {
        }

        // path tests
        assertEquals(p, modConfig.getPathProperty("p"));
        try {
            modConfig.getPathProperty("p2");
            fail();
        } catch (Exception ignored) {
        }
        assertFalse(modConfig.getOptionalPathProperty("p2").isPresent());
        assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl"));
        assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl2"));
        try {
            modConfig.getPathListProperty("pl3");
            fail();
        } catch (Exception ignored) {
        }
        assertFalse(modConfig.getOptionalPathListProperty("pf3").isPresent());

        // time
        assertEquals(DateTime.parse("2009-01-03T18:15:05Z"), modConfig.getDateTimeProperty("dt").withZone(DateTimeZone.UTC));
        assertEquals(Interval.parse("2009-01-03T18:15:05Z/2009-01-09T02:54:25Z"), modConfig.getIntervalProperty("it"));

        assertTrue(modConfig.hasProperty("p"));
    }
}
