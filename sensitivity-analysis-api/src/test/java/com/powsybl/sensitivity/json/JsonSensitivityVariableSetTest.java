package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityVariableSet;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonSensitivityVariableSetTest extends AbstractConverterTest {
    private static SensitivityVariableSet create() {
        SensitivityVariableSetJsonDeserializer deserializer = new SensitivityVariableSetJsonDeserializer();
        JsonFactory factory = JsonUtil.createJsonFactory();
        InputStreamReader reader = new InputStreamReader(JsonSensitivityVariableSetTest.class.getResourceAsStream("/SensitivityVariableSetExample.json"));
        try (JsonParser parser = factory.createParser(reader)) {
            return deserializer.deserialize(parser, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static SensitivityVariableSet read(Path jsonFile) {
        SensitivityVariableSetJsonDeserializer deserializer = new SensitivityVariableSetJsonDeserializer();
        JsonFactory factory = JsonUtil.createJsonFactory();
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return deserializer.deserialize(factory.createParser(new InputStreamReader(is)), null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(SensitivityVariableSet variableSet, Path file) {
        SensitivityVariableSetJsonSerializer serializer = new SensitivityVariableSetJsonSerializer();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(Files.newOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (JsonGenerator generator = new JsonFactory().createGenerator(writer).setPrettyPrinter(new DefaultPrettyPrinter())) {
            serializer.serialize(variableSet, generator, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), JsonSensitivityVariableSetTest::write, JsonSensitivityVariableSetTest::read, "/SensitivityVariableSetExample.json");
    }
}
