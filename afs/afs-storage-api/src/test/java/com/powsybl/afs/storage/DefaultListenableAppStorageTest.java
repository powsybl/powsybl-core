/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorageTest {

    private ListenableAppStorage listenableStorage;

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(this, new DefaultAppStorageListener() {
            @Override
            public void nodeCreated(NodeId id) {

            }

            @Override
            public void nodeRemoved(NodeId id) {

            }

            @Override
            public void attributeUpdated(NodeId id, String attributeName) {

            }

            @Override
            public void dependencyAdded(NodeId id, String dependencyName) {

            }

            @Override
            public void timeSeriesCreated(NodeId id, String timeSeriesName) {

            }

            @Override
            public void timeSeriesDataUpdated(NodeId id, String timeSeriesName) {

            }

            @Override
            public void timeSeriesRemoved(NodeId id) {

            }
        });
    }

    @Test
    public void test() {
    }
}