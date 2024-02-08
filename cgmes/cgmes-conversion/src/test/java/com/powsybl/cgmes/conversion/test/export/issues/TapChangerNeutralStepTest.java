/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class TapChangerNeutralStepTest extends AbstractSerDeTest {

    @Test
    void testNeutralStep() {
        Network network = createNetwork();
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "EQ,TP");
        network.write("CGMES", exportParams, tmpDir.resolve("neutral"));
        assertEquals(Map.of("NGEN_NHV1_PT_PTC_1", 2, "NGEN_NHV1_PT_RTC_1", 2, "NHV2_NLOAD_PT_RTC_1", 1),
                readTapChangerNeutralSteps(tmpDir.resolve("neutral_EQ.xml")));
    }

    private static final String ATTR_ID = "ID";
    private static final String PHASE_TAP_CHANGER_TABULAR = "PhaseTapChangerTabular";
    private static final String RATIO_TAP_CHANGER = "RatioTapChanger";
    private static final String TAP_CHANGER_NEUTRAL_STEP = "TapChanger.neutralStep";

    private static Map<String, Integer> readTapChangerNeutralSteps(Path eq) {
        Map<String, Integer> map = new HashMap<>();
        String tapChangerId = null;
        Integer neutralStep = null;
        try (InputStream is = Files.newInputStream(eq)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (isTapChanger(reader.getLocalName())) {
                        tapChangerId = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, ATTR_ID).substring(1);
                    } else if (reader.getLocalName().equals(TAP_CHANGER_NEUTRAL_STEP)) {
                        neutralStep = Integer.parseInt(reader.getElementText());
                    }
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    if (isTapChanger(reader.getLocalName()) && tapChangerId != null && neutralStep != null) {
                        map.put(tapChangerId, neutralStep);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    private static boolean isTapChanger(String elementName) {
        return elementName.equals(PHASE_TAP_CHANGER_TABULAR) || elementName.equals(RATIO_TAP_CHANGER);
    }

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        twt.newRatioTapChanger()
                .beginStep()
                    .setRho(0.9)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(0.95)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(0.99)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(1.05)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(1.1)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(158.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(twt.getTerminal2())
                .add();
        twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationTerminal(twt.getTerminal2())
                .setRegulationValue(1.0)
                .setTargetDeadband(0)
                .beginStep()
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setAlpha(-10.0)
                    .setRho(1.0)
                .endStep()
                .beginStep()
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setAlpha(-5.0)
                    .setRho(1.0)
                .endStep()
                .beginStep()
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setAlpha(0.01)
                    .setRho(1.0)
                .endStep()
                .beginStep()
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setAlpha(6.0)
                    .setRho(1.0)
                .endStep()
                .beginStep()
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setAlpha(10.0)
                    .setRho(1.0)
                .endStep()
                .add();
        return network;
    }
}
