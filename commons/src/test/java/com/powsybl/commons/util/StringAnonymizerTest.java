/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StringAnonymizerTest {

    private static String toCsv(StringAnonymizer anonymizer) throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        try {
            anonymizer.writeCsv(writer);
        } finally {
            writer.close();
        }
        return stringWriter.toString();
    }

    private static StringAnonymizer fromCsv(String csv) throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            anonymizer.readCsv(reader);
        }
        return anonymizer;
    }

    @Test
    void test() throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        String anonymizedFoo = anonymizer.anonymize("foo");
        String anonymizedBar = anonymizer.anonymize("bar");
        assertNotEquals(anonymizedFoo, anonymizedBar);
        assertEquals("A", anonymizedFoo);
        assertEquals("B", anonymizedBar);
        assertEquals(anonymizedFoo, anonymizer.anonymize("foo"));
        assertEquals("foo", anonymizer.deanonymize(anonymizedFoo));
        assertEquals("bar", anonymizer.deanonymize(anonymizedBar));
        assertNull(anonymizer.anonymize(null));
        assertNull(anonymizer.deanonymize(null));
        try {
            anonymizer.deanonymize("baz");
            fail();
        } catch (Exception ignored) {
        }
        String csv = toCsv(anonymizer);
        assertEquals(String.join(System.lineSeparator(), "foo;A", "bar;B") + System.lineSeparator(),
                     csv.toString());
        StringAnonymizer anonymizer2 = fromCsv(csv);
        assertEquals("foo", anonymizer2.deanonymize(anonymizedFoo));
        assertEquals("bar", anonymizer2.deanonymize(anonymizedBar));
    }

    @Test
    void invalidFileTest() throws IOException {
        String csv = String.join(System.lineSeparator(),
                "",
                "A;B");
        StringAnonymizer anonymizer = fromCsv(csv);
        assertEquals(1, anonymizer.getStringCount());
    }

    @Test
    void invalidFileTest2() throws IOException {
        String csv = String.join(System.lineSeparator(),
                "C");
        assertThrows(RuntimeException.class, () -> fromCsv(csv));
    }

    @Test
    void embeddedNewLineTest() throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        String anonymizedFoo = anonymizer.anonymize("foo\nbar");
        StringAnonymizer anonymizer2 = fromCsv(toCsv(anonymizer));
        assertEquals("foo\nbar", anonymizer2.deanonymize(anonymizedFoo));
    }

    @Test
    void embeddedSeparatorTest() throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        String anonymizedFoo = anonymizer.anonymize("foo;bar");
        StringAnonymizer anonymizer2 = fromCsv(toCsv(anonymizer));
        assertEquals("foo;bar", anonymizer2.deanonymize(anonymizedFoo));
    }
}
