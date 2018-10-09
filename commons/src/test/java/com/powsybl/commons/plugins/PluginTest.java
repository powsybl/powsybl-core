/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.plugins;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class PluginTest {

    Plugin plugin;

    @Before
    public void setUp() {
        plugin = Plugins.getPluginByName(PluginA.PLUGIN_NAME);
    }

    @Test
    public void testPlugins() {
        assertEquals(1, Plugins.getPlugins().stream().map(p -> p.getPluginInfo().getPluginName()).filter(pName -> PluginA.PLUGIN_NAME.equals(pName)).count());
    }

    @Test
    public void testPluginExist() {
        assertNotNull(plugin);
    }

    @Test
    public void testPluginNotExist() {
        assertNull(Plugins.getPluginByName("DOES_NOT_EXIST"));
    }

    @Test
    public void testGetPluginImplementationsIds() {
        List<String> testPluginsIds = Arrays.asList("A1", "A2");
        Collection<String> pluginImplementationsIds = Plugins.getPluginImplementationsIds(plugin);
        assertTrue(testPluginsIds.stream().allMatch(t -> pluginImplementationsIds.contains(t)));
    }

}
