/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ConversionUtil {

    private ConversionUtil() {
    }

    public static Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        return networkModel(testGridModel, config, Collections.emptyList());
    }

    public static Network networkModel(GridModelReference testGridModel, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        Conversion c = new Conversion(cgmes, config, postProcessors);
        return c.convert();
    }

    public static boolean xmlContains(Path xml, String clazz, String ns, String attr, String expectedValue) {
        try (InputStream is = Files.newInputStream(xml)) {
            return xmlContains(is, clazz, ns, attr, expectedValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean xmlContains(byte[] xml, String clazz, String ns, String attr, String expectedValue) {
        try (InputStream is = new ByteArrayInputStream(xml)) {
            return xmlContains(is, clazz, ns, attr, expectedValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean xmlContains(InputStream is, String clazz, String ns, String attr, String expectedValue) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(clazz)) {
                    String actualValue = reader.getAttributeValue(ns, attr);
                    if (expectedValue.equals(actualValue)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static Network readCgmesFiles(String dir, String... files) {
        return readCgmesFiles(new Properties(), dir, files);
    }

    public static Network readCgmesFiles(Properties properties, String dir, String... files) {
        ReadOnlyDataSource ds = new ResourceDataSource("CGMES input file(s)", new ResourceSet(dir, files));
        return Network.read(ds, properties);
    }
}
