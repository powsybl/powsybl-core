/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.xml;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class XmlReaderTest {

    private FileSystem fileSystem;
    private Path workDir;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workDir = fileSystem.getPath("work");
        Files.createDirectories(workDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void secureDeserializationTest() throws Exception {
        // Write the XML exploit file
        Path xmlPath = workDir.resolve("exploit.xml");
        String exploitMessage = "Secret data";
        prepareExploitXml(workDir, xmlPath, exploitMessage);

        try (InputStream is = Files.newInputStream(xmlPath)) {
            XmlReader reader = new XmlReader(is, Collections.emptyMap(), Collections.emptyList());

            // Dirty reflection to advance the reader to correct element.
            Field readerField = XmlReader.class.getDeclaredField("reader");
            readerField.setAccessible(true);
            XMLStreamReader xmlStreamReader = (XMLStreamReader) readerField.get(reader);
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    break;
                }
            }

            // Compare the reader content with the exploit message
            assertNoLeak(reader.readContent());
            reader.close();
        }
    }

    private void assertNoLeak(String content) {
        assertTrue(content == null || "null".equals(content),
                () -> "Leaked content: \"" + content + "\"");
    }

    @Test
    void anotherSecuredDeserializationTest() throws Exception {
        // Write the XML exploit file
        Path xmlPath = workDir.resolve("exploit.xml");
        prepareAnotherExploitXml(xmlPath);

        try (InputStream is = Files.newInputStream(xmlPath)) {
            XmlReader reader = new XmlReader(is, Collections.emptyMap(), Collections.emptyList());

            // Dirty reflection to advance the reader to correct element.
            Field readerField = XmlReader.class.getDeclaredField("reader");
            readerField.setAccessible(true);
            XMLStreamReader xmlStreamReader = (XMLStreamReader) readerField.get(reader);
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    break;
                }
            }

            // Compare the reader content with the exploit message
            assertNoLeak(reader.readContent());
            reader.close();
        } catch (UncheckedXmlStreamException e) {
            fail("Reader should not throw an exception", e);
        }
    }

    private void prepareExploitXml(Path workDir, Path xmlPath, String exploitMessage) throws IOException {
        // Write a secret file
        Path secretFile = workDir.resolve("secret");
        Files.writeString(secretFile, exploitMessage, StandardCharsets.UTF_8);
        String uri = secretFile.toUri().toString();

        // Write XXE XML (modified from sample_EQ.xml)
        String exploitXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE rdf:RDF [\n"
                + "  <!ENTITY xxe SYSTEM \""
                + uri
                + "\">\n"
                + "]>\n"
                + "<foo>&xxe;</foo>\n";
        Files.writeString(xmlPath, exploitXml, StandardCharsets.UTF_8);
    }

    private void prepareAnotherExploitXml(Path xmlPath) throws IOException {
        // Write XXE XML (modified from sample_EQ.xml)
        String exploitXml =
            """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE rdf:RDF [
                  <!ENTITY xxe SYSTEM "\
                http://localhost:12345/ssrf">
                ]>
                <foo>&xxe;</foo>
                """;
        Files.writeString(xmlPath, exploitXml, StandardCharsets.UTF_8);
    }
}
