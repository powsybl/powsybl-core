/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.powsybl.commons.PowsyblException;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class DirectoryDataStoreTest extends AbstractDataStoreTest {

    @Override
    protected DataStore createDataStore() throws IOException {
        return DataStores.createDataStore(testDir);
    }

    @Test(expected = PowsyblException.class)
    public void testSubfolder() throws IOException {

        try (OutputStream os = dataStore.newOutputStream("b/a.txt", false)) {
            // subfolder creation not supported
        }
    }

    @Test(expected = PowsyblException.class)
    public void testSubfolderRead() throws IOException {

        try (InputStream os = dataStore.newInputStream("b/a.txt")) {
            // subfolders not supported
        }
    }
}
