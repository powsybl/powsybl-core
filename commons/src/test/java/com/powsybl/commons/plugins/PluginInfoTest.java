/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.plugins;

import com.powsybl.commons.util.ServiceLoaderCache;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class PluginInfoTest {

    private PluginInfo<A> pluginInfo;

    @Before
    public void setUp() {
        pluginInfo = Plugins.getPluginInfoByName(PluginInfoA.PLUGIN_NAME);
    }

    @Test
    public void testPlugins() {
        assertEquals(1, Plugins.getPluginInfos().stream().map(PluginInfo::getPluginName).filter(PluginInfoA.PLUGIN_NAME::equals).count());
    }

    @Test
    public void testPluginExist() {
        assertNotNull(pluginInfo);
        assertNotNull(pluginInfo.toString());
    }

    @Test
    public void testPluginDetails() {
        new ServiceLoaderCache<>(A.class).getServices().forEach(a ->
                assertNotNull(pluginInfo.getId(a))
        );
    }

    @Test
    public void testPluginNotExist() {
        assertNull(Plugins.getPluginInfoByName("DOES_NOT_EXIST"));
    }

    @Test
    public void testGetPluginImplementationsIds() {
        List<String> testPluginsIds = Arrays.asList("A1", "A2");
        Collection<String> pluginImplementationsIds = Plugins.getPluginImplementationsIds(pluginInfo);
        assertEquals(testPluginsIds.size(), pluginImplementationsIds.size());
        assertTrue(pluginImplementationsIds.containsAll(testPluginsIds));
    }

    @Test
    public void testGetPluginInfoId() {
        A1 a1 = new A1();
        A2 a2 = new A2();
        assertEquals("A1", pluginInfo.getId(a1));
        assertEquals("A2", pluginInfo.getId(a2));
    }

    @Test
    public void testGetPluginInfoIdDefault() {
        PluginInfo<B> pluginInfoB = Plugins.getPluginInfoByName(PluginInfoB.PLUGIN_NAME);
        B b1 = () -> "B1";
        String bPluginID = pluginInfoB.getId(b1);
        assertNotEquals("B1", bPluginID);
        assertEquals(b1.getClass().getName(), bPluginID);
    }

}
