/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
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

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ContingencyJsonTest extends AbstractConverterTest {

    private static Contingency create() {
        List<ContingencyElement> elements = new ArrayList<>();
        elements.add(new BranchContingency("NHV1_NHV2_2", "VLHV1"));
        elements.add(new BranchContingency("NHV1_NHV2_1"));
        elements.add(new HvdcLineContingency("HVDC1"));
        elements.add(new HvdcLineContingency("HVDC1", "VL1"));
        elements.add(new GeneratorContingency("GEN"));
        elements.add(new BusbarSectionContingency("BBS1"));

        Contingency contingency = new Contingency("contingency", elements);

        contingency.addExtension(DummyExtension.class, new DummyExtension());
        return contingency;
    }

    private static Contingency read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
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
            ContingencyJsonModule module = new ContingencyJsonModule();
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

    static class DummyExtension implements Extension<Contingency> {

        private Contingency contingency;

        @Override
        public void setExtendable(Contingency contingency) {
            this.contingency = contingency;
        }

        @Override
        public Contingency getExtendable() {
            return contingency;
        }

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(ExtensionJsonSerializer.class)
    public static class DummySerializer implements ExtensionJsonSerializer<Contingency, DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummy-extension";
        }

        @Override
        public String getCategoryName() {
            return "security-analysis";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }


}
