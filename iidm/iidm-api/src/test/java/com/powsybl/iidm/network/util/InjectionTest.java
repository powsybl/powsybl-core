/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class InjectionTest {

    @Test
    public void testPQI() {
        Terminal terminal = mock(Terminal.class);
        Injection injection = new Injection() {
            @Override
            public Terminal getTerminal() {
                return terminal;
            }

            @Override
            public ConnectableType getType() {
                return null;
            }

            @Override
            public List<? extends Terminal> getTerminals() {
                return null;
            }

            @Override
            public void remove() {

            }

            @Override
            public Network getNetwork() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public boolean hasProperty() {
                return false;
            }

            @Override
            public boolean hasProperty(String key) {
                return false;
            }

            @Override
            public String getProperty(String key) {
                return null;
            }

            @Override
            public String getProperty(String key, String defaultValue) {
                return null;
            }

            @Override
            public String setProperty(String key, String value) {
                return null;
            }

            @Override
            public Set<String> getPropertyNames() {
                return null;
            }

            @Override
            public void addExtension(Class type, Extension extension) {

            }

            @Override
            public Extension getExtension(Class type) {
                return null;
            }

            @Override
            public Extension getExtensionByName(String name) {
                return null;
            }

            @Override
            public boolean removeExtension(Class type) {
                return false;
            }

            @Override
            public Collection getExtensions() {
                return null;
            }
        };

        when(terminal.getP()).thenReturn(99.0);
        assertEquals(99.0, injection.getP());
        terminal.setP(99.1);
        verify(terminal, times(1)).setP(99.1);

        when(terminal.getQ()).thenReturn(88.0);
        assertEquals(88.0, injection.getQ());
        terminal.setQ(88.1);
        verify(terminal, times(1)).setQ(88.1);

        when(terminal.getI()).thenReturn(1.0);
        assertEquals(1.0, injection.getI());

        when(terminal.connect()).thenReturn(true);
        assertTrue(injection.connect());
        when(terminal.disconnect()).thenReturn(false);
        assertFalse(injection.disconnect());
        when(terminal.isConnected()).thenReturn(true);
        assertTrue(injection.isConnected());
    }
}
