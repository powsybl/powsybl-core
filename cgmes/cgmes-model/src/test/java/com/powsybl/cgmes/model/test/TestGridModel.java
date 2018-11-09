/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import java.io.IOException;
import java.nio.file.FileSystem;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

public interface TestGridModel {
    CgmesModel expected();

    boolean exists();

    ReadOnlyDataSource dataSource();

    DataSource dataSourceBasedOn(FileSystem fs) throws IOException;

    String name();
}
