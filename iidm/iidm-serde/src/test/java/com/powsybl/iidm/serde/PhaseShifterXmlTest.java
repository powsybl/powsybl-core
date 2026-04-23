/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseShifterXmlTest extends AbstractIidmSerDeTest {
    @Test
    void roundTripTest() throws IOException {
        // backward and current compatibility
        allFormatsRoundTripFromVersionedXmlTest("phaseShifterRoundTripRef.xml", IidmVersion.values());

        allFormatsRoundTripTest(PhaseShifterTestCaseFactory.createWithTargetDeadband(), "phaseShifterRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void currentLimiterWithNegativeRegulationValueConversionTest() {
        Network n = NetworkSerDe.read(getNetworkAsStream("/phaseShifterCurrentLimiter.xml"));
        assertEquals(20, n.getTwoWindingsTransformer("PS1").getPhaseTapChanger().getRegulationValue());
    }

    @Test
    void importAndExportPhaseTapChangerWithFixedTapRegulationModeTest() {
        // for IIDM version < 1.14 a phase tap changer can have a regulation mode set to FIXED_TAP and should still be imported as CURRENT_LIMITER with regulating=false
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("phaseShifterFixedTapRegulationModeRef.xml", IidmVersion.V_1_13));
        checkPhaseTapChangerRegulation(network);

        // Export network and read it again
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setIncludedExtensions(Collections.emptySet());
        Path exportedPath = tmpDir.resolve("exported.xml");
        NetworkSerDe.write(network, exportOptions, exportedPath);

        Network network2 = NetworkSerDe.read(exportedPath);
        checkPhaseTapChangerRegulation(network2);
    }

    private void checkPhaseTapChangerRegulation(Network network) {
        checkPhaseTapChangerRegulation(network, "PS1");
    }

    private void checkPhaseTapChangerRegulation(Network network, String twtId) {
        Assertions.assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, network.getTwoWindingsTransformer(twtId).getPhaseTapChanger().getRegulationMode());
        Assertions.assertFalse(network.getTwoWindingsTransformer(twtId).getPhaseTapChanger().isRegulating());
    }

    @Test
    void phaseTapChangerWithFixedTapAndNoValueTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getTwoWindingsTransformer("TWT").getPhaseTapChanger()
                .setRegulationValue(Double.NaN)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulating(false);
        assertOldIidmWithFixedTap(network);
    }

    @Test
    void phaseTapChangerWithFixedTapAndNoRegulationTerminalTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getTwoWindingsTransformer("TWT").getPhaseTapChanger()
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationTerminal(null)
                .setRegulating(false);
        assertOldIidmWithFixedTap(network);
    }

    private void assertOldIidmWithFixedTap(Network network) {
        Map<String, String> expectedAttributesOld = new HashMap<>();
        expectedAttributesOld.put("regulating", "false");
        expectedAttributesOld.put("regulationMode", "FIXED_TAP");
        expectedAttributesOld.put("regulationValue", null);

        Map<String, String> expectedAttributesNew = new HashMap<>();
        expectedAttributesNew.put("regulating", "false");
        expectedAttributesNew.put("regulationMode", "CURRENT_LIMITER");
        expectedAttributesNew.put("regulationValue", null);

        testForAllPreviousVersions(IidmVersion.V_1_6, iidmVersion -> assertPhaseTapChangerAttributesCompatibility(network, iidmVersion, expectedAttributesOld));
        testForAllVersionsSince(IidmVersion.V_1_6, iidmVersion -> assertPhaseTapChangerAttributesCompatibility(network, iidmVersion, expectedAttributesNew));
    }

    private void assertPhaseTapChangerAttributesCompatibility(Network network, IidmVersion iidmVersion,
                                                              Map<String, String> expectedAttributes) {
        // Export network and read it again
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setVersion(iidmVersion.toString("."));
        Path exportedPath = tmpDir.resolve("exported.xml");
        NetworkSerDe.write(network, exportOptions, exportedPath);

        assertXmlAttributes(expectedAttributes, exportedPath);
        Network network2 = NetworkSerDe.read(exportedPath);
        checkPhaseTapChangerRegulation(network2, "TWT");
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void assertXmlAttributes(Map<String, String> expectedAttributes, Path xmlPath) {
        Optional<Boolean> result = Optional.empty();
        StringBuilder errors = new StringBuilder("Errors:");
        XMLStreamReader reader = null;
        try {
            try (InputStream is = Files.newInputStream(xmlPath)) {
                reader = XmlUtil.getXMLInputFactory().createXMLStreamReader(is);
                while (result.isEmpty() && reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT && "phaseTapChanger".equals(reader.getLocalName())) {
                        boolean res = true;
                        for (Map.Entry<String, String> attribute : expectedAttributes.entrySet()) {
                            String val = reader.getAttributeValue(null, attribute.getKey());
                            String expectedVal = attribute.getValue();
                            if (expectedVal != null && !expectedVal.equals(val)
                                    || expectedVal == null && val != null) {
                                errors.append(System.lineSeparator())
                                        .append("Attribute \"")
                                        .append(attribute.getKey())
                                        .append("\": expected: ").append(expectedVal)
                                        .append(" - encountered: ").append(val);
                                res = false;
                            }
                        }
                        result = Optional.of(res);
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (result.isPresent()) {
            if (!result.get()) {
                throw new RuntimeException(errors.toString());
            }
        } else {
            throw new RuntimeException("No \"phaseTapChanger\" attribute found.");
        }
    }
}
