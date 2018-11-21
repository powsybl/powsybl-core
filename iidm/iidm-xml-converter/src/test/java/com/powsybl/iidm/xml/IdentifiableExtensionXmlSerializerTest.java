/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IdentifiableExtensionXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        LoadZipModel zipModel = new LoadZipModel(load, 1, 2, 3, 4, 5, 6, 380);
        load.addExtension(LoadZipModel.class, zipModel);
        byte[] buffer;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkXml.write(network, new ExportOptions(), os);
            buffer = os.toByteArray();
        }
        // try to validate the schema with extensions
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            NetworkXml.validateWithExtensions(is);
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            Network network2 = NetworkXml.read(is);
            LoadZipModel zipModel2 = network2.getLoad("LOAD").getExtension(LoadZipModel.class);
            assertTrue(zipModel2 != null);
            assertTrue(zipModel.getA1() == zipModel2.getA1()
                    && zipModel.getA2() == zipModel2.getA2()
                    && zipModel.getA3() == zipModel2.getA3()
                    && zipModel.getA4() == zipModel2.getA4()
                    && zipModel.getA5() == zipModel2.getA5()
                    && zipModel.getA6() == zipModel2.getA6()
                    && zipModel.getV0() == zipModel2.getV0()
            );
        }
    }

    @Test
    public void testMultipleExtensions() throws IOException {
        roundTripXmlTest(MultipleExtensionsTestNetworkFactory.create(),
            NetworkXml::writeAndValidate,
            NetworkXml::read,
            "/multiple-extensions.xml");
    }
}
