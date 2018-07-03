/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteServiceConfigTest {

    @Test
    public void test() {
        RemoteServiceConfig config = new RemoteServiceConfig("host", "test", 443, true);
        assertEquals("https://host:443/test", config.getRestUri().toString());
        assertEquals("wss://host:443/test", config.getWsUri().toString());

        RemoteServiceConfig config2 = new RemoteServiceConfig("host", "test", 80, false);
        assertEquals("http://host:80/test", config2.getRestUri().toString());
        assertEquals("ws://host:80/test", config2.getWsUri().toString());
    }
}
