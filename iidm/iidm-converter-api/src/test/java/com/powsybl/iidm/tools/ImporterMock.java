/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(Importer.class)
public class ImporterMock implements Importer {

    public ImporterMock() {
    }

    @Override
    public String getFormat() {
        return "IN";
    }

    @Override
    public List<Parameter> getParameters() {
        return null;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return true;
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        assertEquals(2, parameters.size());
        assertEquals("value1", parameters.getProperty("param1"));
        assertEquals("value", parameters.getProperty("import.parameter"));

        return null;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {

    }
}
