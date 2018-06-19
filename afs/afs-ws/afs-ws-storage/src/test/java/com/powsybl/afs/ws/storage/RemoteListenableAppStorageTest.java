/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteListenableAppStorageTest {

    @Test
    public void test() throws Exception {
        assertEquals("ws://server:80/app", RemoteListenableAppStorage.getWebSocketUri(new URI("http://server:80/app")).toString());
        assertEquals("wss://server:443/app", RemoteListenableAppStorage.getWebSocketUri(new URI("https://server:443/app")).toString());
    }
}
