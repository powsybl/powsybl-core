package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContingencyJsonTest extends AbstractConverterTest {

    private static Contingency create() {
        List<ContingencyElement> elements = new ArrayList<>();
        elements.add(new BranchContingency("NHV1_NHV2_2", "VLHV1"));
        elements.add(new BranchContingency("NHV1_NHV2_1"));
        elements.add(new HvdcLineContingency("HVDC1"));
        elements.add(new HvdcLineContingency("HVDC1", "VL1"));
        elements.add(new GeneratorContingency("GEN"));
        elements.add(new BusbarSectionContingency("BBS1"));

        return new Contingency("contingency", elements);
    }

    private static Contingency read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Contingency.class, new ContingencyDeserializer());
            module.addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
            objectMapper.registerModule(module);

            return objectMapper.readValue(is, Contingency.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Contingency object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper mapper = JsonUtil.createObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ContingencyElement.class, new ContingencyElementSerializer());
            mapper.registerModule(module);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), ContingencyJsonTest::write, ContingencyJsonTest::read, "/contingency.json");
    }
}
