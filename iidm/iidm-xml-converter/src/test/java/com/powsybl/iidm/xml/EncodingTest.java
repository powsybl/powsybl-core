package com.powsybl.iidm.xml;

import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EncodingTest {

    public static final Path XML_FILE_PATH_ENCODE_ISO_8859_1 = Paths.get("src/test/resources/encoding/network.xml");

    @Test
    public void testEncodingISO88591ToISO88591() throws IOException {
        Network network = NetworkXml.read(XML_FILE_PATH_ENCODE_ISO_8859_1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ExportOptions options = new ExportOptions();
        options.setCharset(StandardCharsets.ISO_8859_1);
        NetworkXml.write(network, options, baos);
        assertEquals(new String(Files.readAllBytes(XML_FILE_PATH_ENCODE_ISO_8859_1)), baos.toString());
    }

    @Test
    public void testEncodingISO88591ToUTF8() throws IOException {
        Network network = NetworkXml.read(XML_FILE_PATH_ENCODE_ISO_8859_1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ExportOptions options = new ExportOptions();
        options.setCharset(StandardCharsets.UTF_8);
        NetworkXml.write(network, options, baos);
        assertNotEquals(new String(Files.readAllBytes(XML_FILE_PATH_ENCODE_ISO_8859_1)), baos.toString());
    }
}
