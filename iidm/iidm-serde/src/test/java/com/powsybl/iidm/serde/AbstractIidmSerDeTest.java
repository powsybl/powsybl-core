/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
     * Execute an all-formats round trip test on the test resource IIDM-XML file with a given file name for the given IIDM versions.
     */
    protected void allFormatsRoundTripFromVersionedXmlTest(String file, IidmVersion... versions) throws IOException {
        for (IidmVersion version : versions) {
            allFormatsRoundTripTest(NetworkSerDe.read(getVersionedNetworkAsStream(file, version)), file, version);
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
     * Execute an all-formats round trip test on the test resource IIDM-XML file with a given file name for all IIDM
     * versions strictly older than the current IIDM version.
     */
    protected void allFormatsRoundTripAllPreviousVersionedXmlTest(String file) throws IOException {
        allFormatsRoundTripFromVersionedXmlTest(file, allPreviousVersions(CURRENT_IIDM_VERSION));
    }

    /**
     * Execute a round trip test on the test resource IIDM-JSON file with a given file name for all IIDM versions
     * strictly older than the current IIDM version.
     */
    protected void roundTripAllPreviousVersionedJsonTest(String file) throws IOException {
        roundTripVersionedJsonTest(file, allPreviousVersions(CURRENT_IIDM_VERSION));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM versions
     * equals or more recent than a given minimum IIDM version <b>and</b> strictly older than the current IIDM version.
     */
    protected void allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest(String file, IidmVersion minVersion) throws IOException {
        allFormatsRoundTripFromVersionedXmlTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0 && v.compareTo(CURRENT_IIDM_VERSION) < 0)
                .toArray(IidmVersion[]::new));
    }

    /**
     * Execute a round trip test on the test resource IIDM-XML file with a given file name for all IIDM versions
     * equals or more recent than a given minimum IIDM version.
     */
    protected void allFormatsRoundTripFromVersionedXmlFromMinVersionTest(String file, IidmVersion minVersion) throws IOException {
        allFormatsRoundTripFromVersionedXmlTest(file, Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0)
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
    protected void allFormatsRoundTripAllVersionedXmlTest(String file) throws IOException {
        allFormatsRoundTripFromVersionedXmlTest(file, IidmVersion.values());
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
     * Execute a given test for all IIDM versions newer or equal than a given minimum IIDM version.
     */
    protected void testForAllVersionsSince(IidmVersion minVersion, Consumer<IidmVersion> test) {
        Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(minVersion) >= 0)
                .forEach(test);
    }

    /**
     * Execute a write test for the given network, for all IIDM versions strictly older than a given maximum IIDM
     * version, and compare to the given versioned xml reference test resource.
     */
    protected void testWriteXmlAllPreviousVersions(Network network, ExportOptions exportOptions, String filename,
                                                   IidmVersion maxVersionExcluded) throws IOException {
        testWriteVersionedXml(network, exportOptions, filename, allPreviousVersions(maxVersionExcluded));
    }

    /**
     * Execute a write test for the given network, for all IIDM versions given, and compare to the given versioned xml
     * reference test resource.
     */
    protected void testWriteVersionedXml(Network network, ExportOptions exportOptions, String filename,
                                                   IidmVersion... versions) throws IOException {
        for (IidmVersion version : versions) {
            writeXmlTest(network,
                    (n, p) -> NetworkSerDe.write(n, exportOptions.setVersion(version.toString(".")), p),
                    getVersionedNetworkPath(filename, version));
        }
    }

    private static IidmVersion[] allPreviousVersions(IidmVersion maxVersionExcluded) {
        return Stream.of(IidmVersion.values())
                .filter(v -> v.compareTo(maxVersionExcluded) < 0)
                .toArray(IidmVersion[]::new);
    }

    /**
     * All-formats round trip from given network with reference xml file:
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
    public Network allFormatsRoundTripTest(Network network, String refXmlFile) throws IOException {
        return allFormatsRoundTripTest(network, refXmlFile, new ExportOptions());
    }

    /**
     * All-formats round trip from given network with versioned reference xml file:
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
     * @return the Network read just before the end of the round trip
     */
    public Network allFormatsRoundTripTest(Network network, String filename, IidmVersion version) throws IOException {
        return allFormatsRoundTripTest(network, filename, version, new ExportOptions());
    }

    /**
     * All-formats round trip from given network with versioned reference xml file:
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
    public Network allFormatsRoundTripTest(Network network, String filename, IidmVersion version, ExportOptions exportOptions) throws IOException {
        return allFormatsRoundTripTest(network, getVersionedNetworkPath(filename, version), exportOptions.setVersion(version.toString(".")));
    }

    /**
     * All-formats round trip from given network with reference xml file:
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
    public Network allFormatsRoundTripTest(Network network, String refXmlFile, ExportOptions exportOptions) throws IOException {
        return roundTripXmlTest(network,
                (n, p) -> binWriteAndRead(jsonWriteAndRead(n, exportOptions, p), exportOptions, p),
                (n, p) -> NetworkSerDe.write(n, exportOptions, p),
                NetworkSerDe::validateAndRead,
                refXmlFile);
    }

    /**
     * Writes given network to JSON file, then reads the resulting file and returns the resulting network
     */
    private static Network jsonWriteAndRead(Network networkInput, ExportOptions options, Path path) {
        return writeAndRead(TreeDataFormat.JSON, networkInput, options, path);
    }


    private static Network binWriteAndRead(Network networkInput, ExportOptions options, Path path) {
        return writeAndRead(TreeDataFormat.BIN, networkInput, options, path);
    }

    /**
     * Writes given network to file of given format, then reads the resulting file and returns the resulting network
     */
    private static Network writeAndRead(TreeDataFormat format, Network networkInput, ExportOptions options, Path path) {
        TreeDataFormat previousFormat = options.getFormat();
        options.setFormat(format);
        Anonymizer anonymizer = NetworkSerDe.write(networkInput, options, path);
        try (InputStream is = Files.newInputStream(path)) {
            Network networkOutput = NetworkSerDe.read(is, new ImportOptions().setFormat(format), anonymizer);
            options.setFormat(previousFormat);
            return networkOutput;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
