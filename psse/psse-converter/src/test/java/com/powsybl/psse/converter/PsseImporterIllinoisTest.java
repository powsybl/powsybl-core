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
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.psse.model.PsseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class PsseImporterIllinoisTest extends AbstractConverterTest {

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
        testInvalid("/illinois/literature-based", "IEEE 39 bus.RAW", "Parsing error");
    }

    @Test
    void testLiteratureBasedIeee39Fixed() {
        testValid("/illinois/literature-based", "IEEE 39 bus-fixed-mixed-delimiters.RAW");
    }

    @Test
    void testLiteratureBasedIeee57() {
        testValid("/illinois/literature-based", "IEEE 57 bus.RAW");
    }

    @Test
    void testLiteratureBasedIeee57Fixed() {
        testValid("/illinois/literature-based", "IEEE 57 bus-fixed-duplicated-ids.RAW");
    }

    @Test
    void testLiteratureBasedIeeeRts96() {
        testValid("/illinois/literature-based", "IEEE RTS 96 bus (1).RAW");
    }

    @Test
    void testLiteratureBasedIeee118() {
        testValid("/illinois/literature-based", "IEEE 118 Bus.RAW");
    }

    @Test
    void testLiteratureBasedIeee118Fixed() {
        testValid("/illinois/literature-based", "IEEE 118 Bus-fixed-duplicated-ids.RAW");
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

    private static void testValid(String resourcePath, String sample) {
        load(resourcePath, sample);
    }
}
