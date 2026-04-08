/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.commons.datasource;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ReadOnlyMemDataSourceTest extends AbstractReadOnlyDataSourceTest {

    @Override
    protected ReadOnlyMemDataSource createDataSourceForPolynomialRegexTest() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource();
        String filename = "a".repeat(100) + "!";
        dataSource.putData(filename, new byte[1]);
        return dataSource;
    }
}
