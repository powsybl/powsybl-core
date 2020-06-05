package com.powsybl.psse.converter;

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import static org.junit.Assert.assertEquals;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
public class PsseImporterTest extends AbstractConverterTest {

    @Test
    public void baseTest() {
        Importer importer = new PsseImporter();
        assertEquals("PSS/E", importer.getFormat());
        assertEquals("PSS/E Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    @Test
    public void importTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"));
        Network network = new PsseImporter().importData(dataSource, null);
        assertEquals("IEEE_14_bus", network.getId());
    }
}
