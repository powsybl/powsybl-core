/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.cim;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cim.CimAnonymizer;
import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CimAnonymizerTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void anonymizeZip() throws Exception {
        String eq = String.join(System.lineSeparator(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\txmlns:cim=\"http://iec.ch/TC57/2009/CIM-schema-cim14#\">",
                "<cim:ACLineSegment rdf:ID=\"L1\">",
                "<cim:Conductor.gch>0</cim:Conductor.gch>",
                "<cim:Conductor.x>0.001</cim:Conductor.x>",
                "<cim:Conductor.bch>0</cim:Conductor.bch>",
                "<cim:Conductor.r>0.0008</cim:Conductor.r>",
                "<cim:ConductingEquipment.BaseVoltage rdf:resource=\"#_BV_10\"/>",
                "<cim:IdentifiedObject.name>Line 1</cim:IdentifiedObject.name>",
                "</cim:ACLineSegment>",
                "<cim:BaseVoltage rdf:ID=\"_BV_10\">",
                "<cim:BaseVoltage.isDC>false</cim:BaseVoltage.isDC>",
                "<cim:BaseVoltage.nominalVoltage>1</cim:BaseVoltage.nominalVoltage>",
                "<cim:IdentifiedObject.name>10</cim:IdentifiedObject.name>",
                "</cim:BaseVoltage>",
                "</rdf:RDF>");
        Path workDir = fileSystem.getPath("work");
        Path cimZipFile = workDir.resolve("sample.zip");
        Path anonymizedCimFileDir = workDir.resolve("result");
        Files.createDirectories(anonymizedCimFileDir);
        Path dictionaryFile = workDir.resolve("dic.csv");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cimZipFile))) {
            zos.putNextEntry(new ZipEntry("sample_EQ.xml"));
            zos.write(eq.getBytes(StandardCharsets.UTF_8));
        }

        new CimAnonymizer().anonymizeZip(cimZipFile, anonymizedCimFileDir, dictionaryFile, new CimAnonymizer.DefaultLogger(), false);

        Path anonymizedCimZipFile = anonymizedCimFileDir.resolve("sample.zip");
        assertTrue(Files.exists(anonymizedCimZipFile));
        try (ZipFile anonymizedCimZipFileData = new ZipFile(anonymizedCimZipFile)) {
            assertNotNull(anonymizedCimZipFileData.entry("sample_EQ.xml"));
            String anonymizedEq = String.join(System.lineSeparator(),
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:cim=\"http://iec.ch/TC57/2009/CIM-schema-cim14#\">",
                    "<cim:ACLineSegment rdf:ID=\"A\">",
                    "<cim:Conductor.gch>0</cim:Conductor.gch>",
                    "<cim:Conductor.x>0.001</cim:Conductor.x>",
                    "<cim:Conductor.bch>0</cim:Conductor.bch>",
                    "<cim:Conductor.r>0.0008</cim:Conductor.r>",
                    "<cim:ConductingEquipment.BaseVoltage rdf:resource=\"#B\"></cim:ConductingEquipment.BaseVoltage>",
                    "<cim:IdentifiedObject.name>C</cim:IdentifiedObject.name>",
                    "</cim:ACLineSegment>",
                    "<cim:BaseVoltage rdf:ID=\"B\">",
                    "<cim:BaseVoltage.isDC>false</cim:BaseVoltage.isDC>",
                    "<cim:BaseVoltage.nominalVoltage>1</cim:BaseVoltage.nominalVoltage>",
                    "<cim:IdentifiedObject.name>D</cim:IdentifiedObject.name>",
                    "</cim:BaseVoltage>",
                    "</rdf:RDF>");
            Source control = Input.fromString(anonymizedEq).build();
            try (InputStream is = anonymizedCimZipFileData.getInputStream("sample_EQ.xml")) {
                Source test = Input.fromStream(is).build();
                Diff myDiff = DiffBuilder.compare(control)
                        .withTest(test)
                        .ignoreWhitespace()
                        .ignoreComments()
                        .build();
                boolean hasDiff = myDiff.hasDifferences();
                if (hasDiff) {
                    System.err.println(myDiff.toString());
                }
                assertFalse(hasDiff);
            }
        }

        String dictionaryCsv = String.join(System.lineSeparator(),
                "L1;A",
                "_BV_10;B",
                "Line 1;C",
                "10;D") + System.lineSeparator();
        assertEquals(dictionaryCsv, new String(Files.readAllBytes(dictionaryFile), StandardCharsets.UTF_8));
    }
}
