package com.powsybl.commons.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigVersionTest {

    @Test
    public void test() {
        ConfigVersion version = new ConfigVersion("1.0");
        assertEquals("1.0", version.toString());
        assertTrue(version.isStrictlyOlderThan("1.1"));
        assertFalse(version.equalsOrIsNewerThan("1.1"));

        version = new ConfigVersion("1.1");
        assertEquals("1.1", version.toString());
        assertFalse(version.isStrictlyOlderThan("1.1"));
        assertTrue(version.equalsOrIsNewerThan("1.1"));

        version = new ConfigVersion("1.1.1");
        assertEquals("1.1.1", version.toString());
        assertFalse(version.isStrictlyOlderThan("1.1"));
        assertTrue(version.equalsOrIsNewerThan("1.1"));

        version = new ConfigVersion("2.0");
        assertEquals("2.0", version.toString());
        assertFalse(version.isStrictlyOlderThan("1.1"));
        assertTrue(version.equalsOrIsNewerThan("1.1"));
    }

}
