/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ShuntCompensatorXmlTest extends AbstractIidmSerDeTest {

    @Test
    void linearShuntTest() throws IOException {
        Network network = ShuntTestCaseFactory.createWithActivePower();
        ShuntCompensator sc = network.getShuntCompensator("SHUNT");
        sc.setProperty("test", "test");
        allFormatsRoundTripTest(network, "shuntRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("shuntRoundTripRef.xml", IidmVersion.V_1_2);
    }

    @Test
    void nonLinearShuntTest() throws IOException {
        Network network = ShuntTestCaseFactory.createNonLinear();
        ShuntCompensator sc = network.getShuntCompensator("SHUNT");
        sc.setProperty("test", "test");
        allFormatsRoundTripTest(network, "nonLinearShuntRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility from version 1.2
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("nonLinearShuntRoundTripRef.xml", IidmVersion.V_1_3);

        // check that it fails for versions previous to 1.2
        testForAllPreviousVersions(IidmVersion.V_1_3, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkSerDe.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("shunt.shuntNonLinearModel is not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.3",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.2 when log error is the IIDM version incompatibility behavior
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "nonLinearShuntRoundTripRef.xml", IidmVersion.V_1_3);
    }

    @Test
    void unsupportedWriteTest() {
        Network network = ShuntTestCaseFactory.create();
        testForAllPreviousVersions(IidmVersion.V_1_2, v -> write(network, v.toString(".")));
    }

    @Test
    void nullBPerSection() {
        Network network = ShuntTestCaseFactory.create(0.0);
        Path path = tmpDir.resolve("shunt.xml");

        NetworkSerDe.write(network, new ExportOptions().setVersion(IidmVersion.V_1_4.toString(".")), path);
        Network n = NetworkSerDe.read(path);
        ShuntCompensator sc = n.getShuntCompensator("SHUNT");
        assertEquals(Double.MIN_NORMAL, sc.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0);

        network.getShuntCompensator("SHUNT").setVoltageRegulatorOn(false).setTargetV(Double.NaN).setTargetDeadband(Double.NaN).setRegulatingTerminal(null);
        NetworkSerDe.write(network, new ExportOptions().setVersion(IidmVersion.V_1_1.toString(".")), path);
        Network n2 = NetworkSerDe.read(path);
        ShuntCompensator sc2 = n2.getShuntCompensator("SHUNT");
        assertEquals(Double.MIN_NORMAL, sc2.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0);
    }

    @Test
    void solvedSectionCount() throws IOException {
        Network network = ShuntTestCaseFactory.create();
        network.getShuntCompensator("SHUNT").setSolvedSectionCount(1);

        allFormatsRoundTripTest(network, "shuntWithSolvedSectionCountRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("shuntWithSolvedSectionCountRoundTripRef.xml", IidmVersion.V_1_14);
    }

    @ParameterizedTest(name = "import using {1} format")
    @CsvSource({"shuntOldTagName.xml,XML", "shuntOldTagName.jiidm,JSON"})
    void shouldRejectShuntOldTagName(String fileName, String format) {
        testForAllVersionsSince(IidmVersion.V_1_16, version -> {
            //Given (XML and JSON format)
            InputStream inputStream = getVersionedNetworkAsStream(fileName, version);
            //When
            TreeDataFormat treeDataFormat = TreeDataFormat.valueOf(format);
            ImportOptions options = new ImportOptions().setFormat(treeDataFormat);
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(inputStream, options, null));
            //Then
            assertThat(e.getMessage()).isEqualTo("shunt is not supported for IIDM version 1.16. IIDM version should be <= 1.15");
        });
    }

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest("shuntCompensator.jiidm", IidmVersion.V_1_16);
    }

    private void write(Network network, String version) {
        try {
            ExportOptions options = new ExportOptions().setVersion(version);
            NetworkSerDe.write(network, options, tmpDir.resolve("fail.xml"));
            fail();
        } catch (PowsyblException e) {
            assertEquals("shunt.voltageRegulatorOn is not defined as default and not supported for IIDM version " +
                            version + ". IIDM version should be >= 1.2",
                    e.getMessage());
        }
    }
}
