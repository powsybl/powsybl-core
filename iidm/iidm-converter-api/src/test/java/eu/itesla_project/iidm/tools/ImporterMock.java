/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.parameters.Parameter;

import java.io.InputStream;
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
    public InputStream get16x16Icon() {
        return null;
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
    public Network import_(ReadOnlyDataSource dataSource, Properties parameters) {
        assertEquals(2, parameters.size());
        assertEquals("value1", parameters.getProperty("param1"));
        assertEquals("value", parameters.getProperty("import.parameter"));

        return null;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {

    }
}
