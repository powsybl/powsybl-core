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
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractXmlConverterTest extends AbstractConverterTest {

    public static String getVersionDir(IidmXmlVersion version) {
        return "/V" + version.toString("_") + "/";
    }

    public static String getVersionedNetworkPath(String fileName, IidmXmlVersion version) {
        return getVersionDir(version) + fileName;
    }

    protected InputStream getVersionedNetworkAsStream(String fileName, IidmXmlVersion version) {
        return getClass().getResourceAsStream(getVersionedNetworkPath(fileName, version));
    }

    protected void roundTripVersionedXmlTest(String file, IidmXmlVersion... versions) throws IOException {
        for (IidmXmlVersion version : versions) {
            roundTripXmlTest(NetworkXml.read(getVersionedNetworkAsStream(file, version)),
                    writeAndValidate(version),
                    NetworkXml::validateAndRead,
                    getVersionedNetworkPath(file, version));
        }
    }

    protected void roundTripAllPreviousVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, Stream.of(IidmXmlVersion.values())
                .filter(v -> v.compareTo(CURRENT_IIDM_XML_VERSION) < 0)
                .toArray(IidmXmlVersion[]::new));
    }

    protected void roundTripAllVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, IidmXmlVersion.values());
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
        return (n, p) -> {
            try {
                NetworkXml.writeAndValidate(n, options, p);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
