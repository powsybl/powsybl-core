/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractXmlConverterTest extends AbstractConverterTest {

    /**
     * Return path (as String) of the test resource directory containing all IIDM-XML files of a given IIDM-XML version.
     */
    public static String getVersionDir(IidmXmlVersion version) {
        return "/V" + version.toString("_") + "/";
    }

    /**
     * Return path (as String) of the test resource IIDM-XML file with a given file name in a given IIDM-XML version.
     */
    public static String getVersionedNetworkPath(String fileName, IidmXmlVersion version) {
        return getVersionDir(version) + fileName;
    }

    /**
     * Return an input stream of the test resource IIDM-XML file with a given file name in a given IIDM-XML version.
     */
    protected InputStream getVersionedNetworkAsStream(String fileName, IidmXmlVersion version) {
        return getClass().getResourceAsStream(getVersionedNetworkPath(fileName, version));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for the given IIDM-XML versions.
     */
    protected void roundTripVersionedXmlTest(String file, IidmXmlVersion... versions) throws IOException {
        for (IidmXmlVersion version : versions) {
            roundTripXmlTest(NetworkXml.read(getVersionedNetworkAsStream(file, version)),
                    writeAndValidate(version),
                    NetworkXml::validateAndRead,
                    getVersionedNetworkPath(file, version));
        }
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM-XML versions
     * strictly older than the current IIDM-XML version.
     */
    protected void roundTripAllPreviousVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, Stream.of(IidmXmlVersion.values())
                .filter(v -> v.compareTo(CURRENT_IIDM_XML_VERSION) < 0)
                .toArray(IidmXmlVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM-XML versions
     * equals or more recent than a given minimum IIDM-XML version <b>and</b> strictly older than the current IIDM-XML version.
     */
    protected void roundTripVersionedXmlFromMinToCurrentVersionTest(String file, IidmXmlVersion minVersion) throws IOException {
        roundTripVersionedXmlTest(file, Stream.of(IidmXmlVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0 && v.compareTo(CURRENT_IIDM_XML_VERSION) < 0)
                .toArray(IidmXmlVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM-XML versions.
     */
    protected void roundTripAllVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, IidmXmlVersion.values());
    }

    /**
     * Execute a given test for all IIDM-XML versions strictly older than a given maximum IIDM-XML version.
     */
    protected void testForAllPreviousVersions(IidmXmlVersion maxVersion, Consumer<IidmXmlVersion> test) {
        Stream.of(IidmXmlVersion.values())
                .filter(v -> v.compareTo(maxVersion) < 0)
                .forEach(test);
    }

    /**
     * @deprecated Use {@link #roundTripVersionedXmlTest(String, IidmXmlVersion...)} instead.
     */
    @Deprecated
    protected void roundTripVersionnedXmlTest(String file, IidmXmlVersion... versions) throws IOException {
        roundTripVersionedXmlTest(file, versions);
    }

    /**
     * @deprecated Use {@link #roundTripAllVersionedXmlTest(String)} instead.
     */
    @Deprecated
    protected void roundTripAllVersionnedXmlTest(String file) throws IOException {
        roundTripAllVersionedXmlTest(file);
    }

    private static BiConsumer<Network, Path> writeAndValidate(IidmXmlVersion version) {
        ExportOptions options = new ExportOptions().setVersion(version.toString("."));
        return (n, p) -> NetworkXml.writeAndValidate(n, options, p);
    }
}
