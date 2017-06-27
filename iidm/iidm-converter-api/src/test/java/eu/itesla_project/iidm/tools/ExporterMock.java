/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.network.Network;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(Exporter.class)
public class ExporterMock implements Exporter {

    public ExporterMock() {

    }

    @Override
    public String getFormat() {
        return "OUT";
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        assertEquals(2, parameters.size());
        assertEquals("value2", parameters.getProperty("param2"));
        assertEquals("value", parameters.getProperty("export.parameter"));
    }
}
