/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PowerFactoryImporterTest extends AbstractConverterTest {

    private void importAndCompareXml(String id) {
        Network network = new PowerFactoryImporter()
                .importData(new ResourceDataSource(id, new ResourceSet("/", id + ".dgs")),
                        NetworkFactory.findDefault(),
                        null);
        Path file = fileSystem.getPath("/work/" + id + ".xiidm");
        network.setCaseDate(DateTime.parse("2021-01-01T10:00:00.000+02:00"));
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + id + ".xiidm"), is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void ieee14Test() {
        importAndCompareXml("ieee14");
    }
}
