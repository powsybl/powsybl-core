/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class MemDataStoreTest extends AbstractDataStoreTest {

    @Override
    protected DataStore createDataStore() throws IOException {
        return new MemDataStore();
    }

}
