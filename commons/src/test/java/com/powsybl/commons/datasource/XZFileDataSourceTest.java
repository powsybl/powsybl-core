/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

/**
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 */
class XZFileDataSourceTest extends AbstractDataSourceTest {

    @Override
    protected boolean appendTest() {
        return false; // FIXME append does not work with xz compression
    }

    @Override
    protected DataSource createDataSource() {
        return new XZFileDataSource(testDir, getBaseName());
    }
}
