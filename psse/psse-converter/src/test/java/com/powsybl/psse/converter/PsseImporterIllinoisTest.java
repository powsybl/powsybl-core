/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.psse.model.PsseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class PsseImporterIllinoisTest extends AbstractSerDeTest {

    // From Illinois Center for a Smarter Electric Grid (ICSEG) (https://icseg.iti.illinois.edu/power-cases/)
    // A repository of publicly available, non-confidential power flow cases.
    // The cases are available in a variety of different formats, including Siemens PSSE (*.raw)

    @Test
    void testLiteratureBasedIeee14() {
        testValid("/illinois/literature-based", "IEEE 14 bus.raw");
    }

    @Test
    void testLiteratureBasedIeee24() {
        testValid("/illinois/literature-based", "IEEE 24 bus.RAW");
    }

    @Test
    void testLiteratureBasedIeee30() {
        testValid("/illinois/literature-based", "IEEE 30 bus.RAW");
    }

    @Test
    void testLiteratureBasedIeee39() {
        testValid("/illinois/literature-based", "IEEE 39 bus.RAW");
    }

    @Test
    void testLiteratureBasedIeee39Fixed() {
        testValid("/illinois/literature-based", "IEEE 39 bus-fixed-mixed-delimiters.RAW");
    }

    @Test
    void testLiteratureBasedIeee57() {
        Network n = testValid("/illinois/literature-based", "IEEE 57 bus.RAW");
        // Check that lines and transformers with duplicated ids are correctly imported
        assertNotNull(n.getLine("L-24-25-1 "));
        assertNotNull(n.getLine("L-24-25-10"));
        assertNotNull(n.getTwoWindingsTransformer("T-4-18-1 "));
        assertNotNull(n.getTwoWindingsTransformer("T-4-18-10"));
    }

    @Test
    void testLiteratureBasedIeeeRts96() {
        testValid("/illinois/literature-based", "IEEE RTS 96 bus (1).RAW");
    }

    @Test
    void testLiteratureBasedIeee118() {
        Network n = testValid("/illinois/literature-based", "IEEE 118 Bus.RAW");
        assertNotNull(n.getLine("L-42-49-1 "));
        assertNotNull(n.getLine("L-42-49-10"));
        assertNotNull(n.getLine("L-77-80-1 "));
        assertNotNull(n.getLine("L-77-80-10"));
        assertNotNull(n.getLine("L-49-66-1 "));
        assertNotNull(n.getLine("L-49-66-10"));
        assertNotNull(n.getLine("L-49-54-1 "));
        assertNotNull(n.getLine("L-49-54-10"));
        assertNotNull(n.getLine("L-89-92-1 "));
        assertNotNull(n.getLine("L-89-92-10"));
        assertNotNull(n.getLine("L-56-59-1 "));
        assertNotNull(n.getLine("L-56-59-10"));
        assertNotNull(n.getLine("L-89-90-1 "));
        assertNotNull(n.getLine("L-89-90-10"));
    }

    @Test
    void testLiteratureBasedIeee300() {
        testValid("/illinois/literature-based", "IEEE300Bus.raw");
    }

    @Test
    void testLiteratureBasedTwoArea() {
        testValid("/illinois/literature-based", "two_area_case.RAW");
    }

    @Test
    void testLiteratureBasedWscc9() {
        testValid("/illinois/literature-based", "WSCC 9 bus.raw");
    }

    @Test
    void testSyntheticUiuc150() {
        testValid("/illinois/synthetic", "uiuc-150bus.RAW");
    }

    @Test
    void testSyntheticIllinois200() {
        testValid("/illinois/synthetic", "Illinois200.RAW");
    }

    @Test
    void testSyntheticSouthCarolina500() {
        testValid("/illinois/synthetic", "SouthCarolina500.RAW");
    }

    @Test
    void testSyntheticTexas200June2016() {
        testValid("/illinois/synthetic", "Texas2000_June2016.RAW");
    }

    private static Network load(String resourcePath, String sample) {
        String baseName = sample.substring(0, sample.lastIndexOf('.'));
        return Network.read(new ResourceDataSource(baseName, new ResourceSet(resourcePath, sample)));
    }

    private static void testInvalid(String resourcePath, String sample, String message) {
        PsseException exception = Assertions.assertThrows(PsseException.class, () -> load(resourcePath, sample));
        Assertions.assertEquals(message, exception.getMessage());
    }

    private static Network testValid(String resourcePath, String sample) {
        return load(resourcePath, sample);
    }
}
