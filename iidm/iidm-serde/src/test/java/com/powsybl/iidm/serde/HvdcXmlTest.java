/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class HvdcXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripLccTest() throws IOException {
        allFormatsRoundTripTest(HvdcTestNetwork.createLcc(), "LccRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("LccRoundTripRef.xml");
    }

    @Test
    void roundTripVscTest() throws IOException {
        allFormatsRoundTripTest(HvdcTestNetwork.createVsc(), "VscRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("VscRoundTripRef.xml");
    }

    @Test
    void vscWithVoltageRegulationExportedOnNewerVersionShouldSucceed() throws IOException {
        //Given
        Network networkImportedV16 = NetworkSerDe.read(getVersionedNetworkAsStream("VscRoundTripRef.xml", IidmVersion.V_1_16));
        Path xmlV16 = tmpDir.resolve("vsc_v116.xiidm");
        ExportOptions exportOptionsV16 = new ExportOptions().setFormat(TreeDataFormat.XML).setVersion(IidmVersion.V_1_16.toString("."));
        NetworkSerDe.write(networkImportedV16, exportOptionsV16, xmlV16);
        //When
        //export v17
        Path xmlV17 = tmpDir.resolve("vsc_v117.xiidm");
        ExportOptions exportOptionsV17 = new ExportOptions().setFormat(TreeDataFormat.XML).setVersion(IidmVersion.V_1_17.toString("."));
        NetworkSerDe.write(networkImportedV16, exportOptionsV17, xmlV17);
        //Then
        String contentV16 = Files.readString(xmlV16, StandardCharsets.UTF_8);
        assertThat(contentV16).containsIgnoringWhitespaces("""
            <iidm:vscConverterStation id="C2" name="Converter2" voltageRegulatorOn="false" lossFactor="1.1" reactivePowerSetpoint="123.0" node="2">
                <iidm:minMaxReactiveLimits minQ="0.0" maxQ="10.0"/>
                <iidm:regulatingTerminal id="C1"/>
            </iidm:vscConverterStation>
            """);
        String contentV17 = Files.readString(xmlV17, StandardCharsets.UTF_8);
        // TODO regulating should be false
        assertThat(contentV17).containsIgnoringWhitespaces("""
            <iidm:vscConverterStation id="C2" name="Converter2" lossFactor="1.1" node="2">
                <iidm:minMaxReactiveLimits minQ="0.0" maxQ="10.0"/>
                <iidm:voltageRegulation targetValue="123.0" mode="REACTIVE_POWER" regulating="true">
                    <iidm:terminal id="C1"/>
                </iidm:voltageRegulation>
            </iidm:vscConverterStation>
            """);
    }

    @Test
    void vscWithVoltageRegulationExportedOnOlderVersionShouldSucceed() throws IOException {
        //Given
        Network networkImportedV16 = NetworkSerDe.read(getVersionedNetworkAsStream("VscRoundTripRef.xml", IidmVersion.V_1_17));
        Path xmlV17 = tmpDir.resolve("vsc_v117.xiidm");
        ExportOptions exportOptionsV17 = new ExportOptions().setFormat(TreeDataFormat.XML).setVersion(IidmVersion.V_1_17.toString("."));
        NetworkSerDe.write(networkImportedV16, exportOptionsV17, xmlV17);
        //When
        //export on v16
        Path xmlV16 = tmpDir.resolve("vsc_v116.xiidm");
        ExportOptions exportOptionsV16 = new ExportOptions().setFormat(TreeDataFormat.XML).setVersion(IidmVersion.V_1_16.toString("."));
        NetworkSerDe.write(networkImportedV16, exportOptionsV16, xmlV16);
        //Then
        String contentV17 = Files.readString(xmlV17, StandardCharsets.UTF_8);
        assertThat(contentV17).containsIgnoringWhitespaces("""
            <iidm:vscConverterStation id="C2" name="Converter2" lossFactor="1.1" node="2">
                <iidm:minMaxReactiveLimits minQ="0.0" maxQ="10.0"/>
                <iidm:voltageRegulation targetValue="123.0" mode="REACTIVE_POWER" regulating="true">
                    <iidm:terminal id="C1"/>
                </iidm:voltageRegulation>
            </iidm:vscConverterStation>
            """);
        String contentV16 = Files.readString(xmlV16, StandardCharsets.UTF_8);
        // TODO regulating should be true
        assertThat(contentV16).containsIgnoringWhitespaces("""
            <iidm:vscConverterStation id="C2" name="Converter2" voltageRegulatorOn="false" lossFactor="1.1" reactivePowerSetpoint="123.0" node="2">
                <iidm:minMaxReactiveLimits minQ="0.0" maxQ="10.0"/>
                <iidm:regulatingTerminal id="C1"/>
            </iidm:vscConverterStation>
            """);

    }

}
