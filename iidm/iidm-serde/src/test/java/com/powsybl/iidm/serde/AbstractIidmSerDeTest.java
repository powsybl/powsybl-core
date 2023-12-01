/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractIidmSerDeTest extends AbstractSerDeTest {

    /**
     * Return path (as String) of the test resource directory containing all IIDM-XML files of a given IIDM version.
     */
    public static String getVersionDir(IidmVersion version) {
        return "/V" + version.toString("_") + "/";
    }

    /**
     * Return path (as String) of the test resource IIDM-XML file with a given file name in a given IIDM version.
     */
    public static String getVersionedNetworkPath(String fileName, IidmVersion version) {
        return getVersionDir(version) + fileName;
    }

    /**
     * Return an input stream of the test resource IIDM-XML file with a given file name in a given IIDM version.
     */
    protected InputStream getVersionedNetworkAsStream(String fileName, IidmVersion version) {
        return getClass().getResourceAsStream(getVersionedNetworkPath(fileName, version));
    }

    /**
     * Return an input stream of the test resource IIDM-XML file with a given file name.
     */
    protected InputStream getNetworkAsStream(String fileName) {
        return getClass().getResourceAsStream(fileName);
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for the given IIDM versions.
     */
    protected void roundTripVersionedXmlTest(String file, IidmVersion... versions) throws IOException {
        for (IidmVersion version : versions) {
            fullRoundTripTest(NetworkSerDe.read(getVersionedNetworkAsStream(file, version)), file, version);
        }
    }

    /**
     * Execute a round trip test on the test resource IIDM-JSON file with a given file name for the given IIDM versions.
     */
    protected void roundTripVersionedJsonTest(String file, IidmVersion... versions) throws IOException {
        for (IidmVersion version : versions) {
            ImportOptions options = new ImportOptions().setFormat(TreeDataFormat.JSON);
            roundTripTest(NetworkSerDe.read(getVersionedNetworkAsStream(file, version), options, null),
                    (n, xmlFile) -> NetworkSerDe.write(n, new ExportOptions().setVersion(version.toString(".")).setFormat(TreeDataFormat.JSON), xmlFile),
                    xmlFile -> NetworkSerDe.read(xmlFile, options),
                    getVersionedNetworkPath(file, version));
        }
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM versions
     * strictly older than the current IIDM version.
     */
    protected void roundTripAllPreviousVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(CURRENT_IIDM_VERSION) < 0)
                .toArray(IidmVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-JSON file with a given file name for all IIDM versions
     * strictly older than the current IIDM version.
     */
    protected void roundTripAllPreviousVersionedJsonTest(String file) throws IOException {
        roundTripVersionedJsonTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(CURRENT_IIDM_VERSION) < 0)
                .toArray(IidmVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM versions
     * equals or more recent than a given minimum IIDM version <b>and</b> strictly older than the current IIDM version.
     */
    protected void roundTripVersionedXmlFromMinToCurrentVersionTest(String file, IidmVersion minVersion) throws IOException {
        roundTripVersionedXmlTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0 && v.compareTo(CURRENT_IIDM_VERSION) < 0)
                .toArray(IidmVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-JSON file with a given file name for all IIDM versions
     * equals or more recent than a given minimum IIDM version <b>and</b> strictly older than the current IIDM version.
     */
    protected void roundTripVersionedJsonFromMinToCurrentVersionTest(String file, IidmVersion minVersion) throws IOException {
        roundTripVersionedJsonTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0 && v.compareTo(CURRENT_IIDM_VERSION) < 0)
                .toArray(IidmVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM versions.
     */
    protected void roundTripAllVersionedXmlTest(String file) throws IOException {
        roundTripVersionedXmlTest(file, IidmVersion.values());
    }

    /**
     * Execute a given test for all IIDM versions strictly older than a given maximum IIDM version.
     */
    protected void testForAllPreviousVersions(IidmVersion maxVersion, Consumer<IidmVersion> test) {
        Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(maxVersion) < 0)
                .forEach(test);
    }

    /**
     * @deprecated Use {@link #roundTripVersionedXmlTest(String, IidmVersion...)} instead.
     */
    @Deprecated
    protected void roundTripVersionnedXmlTest(String file, IidmVersion... versions) throws IOException {
        roundTripVersionedXmlTest(file, versions);
    }

    /**
     * @deprecated Use {@link #roundTripAllVersionedXmlTest(String)} instead.
     */
    @Deprecated
    protected void roundTripAllVersionnedXmlTest(String file) throws IOException {
        roundTripAllVersionedXmlTest(file);
    }

    /**
     * Full round trip from given network with reference xml file:
     * <ul>
     *     <li>write given network to a JSON file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>validate the resulting file</li>
     *     <li>compare the resulting file to reference file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>compare the resulting file to reference file</li>
     * </ul>
     * @param network the network to start with
     * @param refXmlFile the name of the reference file resource, including its path
     * @return the Network read just before the end of the round trip
     */
    public Network fullRoundTripTest(Network network, String refXmlFile) throws IOException {
        return fullRoundTripTest(network, refXmlFile, new ExportOptions());
    }

    /**
     * Full round trip from given network with versioned reference xml file:
     * <ul>
     *     <li>write given network to a JSON file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>validate the resulting file</li>
     *     <li>compare the resulting file to reference file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>compare the resulting file to reference file</li>
     * </ul>
     * @param network the network to start with
     * @param filename the filename of the reference versioned file resource
     * @return the Network read just before the end of the round trip
     */
    public Network fullRoundTripTest(Network network, String filename, IidmVersion version) throws IOException {
        return fullRoundTripTest(network, filename, version, new ExportOptions());
    }

    /**
     * Full round trip from given network with versioned reference xml file:
     * <ul>
     *     <li>write given network to a JSON file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>validate the resulting file</li>
     *     <li>compare the resulting file to reference file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>compare the resulting file to reference file</li>
     * </ul>
     * @param network the network to start with
     * @param filename the filename of the reference versioned file resource
     * @param version the version to use for exporting and for the versioned filename
     * @param exportOptions the options to use for exporting
     * @return the Network read just before the end of the round trip
     */
    public Network fullRoundTripTest(Network network, String filename, IidmVersion version, ExportOptions exportOptions) throws IOException {
        return fullRoundTripTest(network, getVersionedNetworkPath(filename, version), exportOptions.setVersion(version.toString(".")));
    }

    /**
     * Full round trip from given network with reference xml file:
     * <ul>
     *     <li>write given network to a JSON file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>validate the resulting file</li>
     *     <li>compare the resulting file to reference file</li>
     *     <li>read the resulting file</li>
     *     <li>write the resulting network to a XML file</li>
     *     <li>compare the resulting file to reference file</li>
     * </ul>
     * @param network the network to start with
     * @param refXmlFile the name of the reference file resource, including its path
     * @param exportOptions the options to use for exporting
     * @return the Network read just before the end of the round trip
     */
    public Network fullRoundTripTest(Network network, String refXmlFile, ExportOptions exportOptions) throws IOException {
        return roundTripXmlTest(network,
                (n, p) -> jsonWriteAndRead(n, exportOptions, p),
                (n, p) -> NetworkSerDe.writeAndValidate(n, exportOptions, p),
                NetworkSerDe::read,
                refXmlFile);
    }

    /**
     * Writes given network to JSON file, then reads the resulting file and returns the resulting network
     */
    private static Network jsonWriteAndRead(Network networkInput, ExportOptions options, Path path) {
        TreeDataFormat previousFormat = options.getFormat();
        options.setFormat(TreeDataFormat.JSON);
        Anonymizer anonymizer = NetworkSerDe.write(networkInput, options, path);
        try (InputStream is = Files.newInputStream(path)) {
            Network networkOutput = NetworkSerDe.read(is, new ImportOptions().setFormat(TreeDataFormat.JSON), anonymizer);
            options.setFormat(previousFormat);
            return networkOutput;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
