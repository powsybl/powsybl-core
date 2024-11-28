/**
 * Copyright (c) 2018-2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.DefaultContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.powsybl.contingency.ContingencyElementType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class ContingencyJsonTest extends AbstractSerDeTest {

    @BeforeEach
    void setup() throws IOException {
        super.setUp();

        Files.copy(getClass().getResourceAsStream("/contingencies.json"), fileSystem.getPath("/contingencies.json"));
        Files.copy(getClass().getResourceAsStream("/contingenciesBatteries.json"), fileSystem.getPath("/contingenciesBatteries.json"));
        Files.copy(getClass().getResourceAsStream("/contingenciesWithOptionalName.json"), fileSystem.getPath("/contingenciesWithOptionalName.json"));
        Files.copy(getClass().getResourceAsStream("/contingenciesWithSeveralElements.json"), fileSystem.getPath("/contingenciesWithSeveralElements.json"));
        Files.copy(getClass().getResourceAsStream("/contingenciesWith3wt.json"), fileSystem.getPath("/contingenciesWith3wt.json"));
        Files.copy(getClass().getResourceAsStream("/contingenciesWithDlAndTl.json"), fileSystem.getPath("/contingenciesWithDlAndTl.json"));
    }

    private static Contingency create() {
        Contingency contingency = Contingency.builder("contingency")
                                             .addBranch("NHV1_NHV2_2", "VLHV1")
                                             .addBranch("NHV1_NHV2_1")
                                             .addHvdcLine("HVDC1")
                                             .addHvdcLine("HVDC1", "VL1")
                                             .addGenerator("GEN")
                                             .addShuntCompensator("SC")
                                             .addStaticVarCompensator("SVC")
                                             .addBusbarSection("BBS1")
                                             .addDanglingLine("DL1")
                                             .addLine("LINE1")
                                             .addTwoWindingsTransformer("TRANSFO1")
                                             .addThreeWindingsTransformer("TWT3")
                                             .addLoad("LOAD")
                                             .addBus("BUS1")
                                             .build();

        contingency.addExtension(DummyExtension.class, new DummyExtension());
        return contingency;
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            objectMapper.registerModule(module);

            return (T) objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Contingency readContingency(Path jsonFile) {
        return read(jsonFile, Contingency.class);
    }

    private static DefaultContingencyList readContingencyList(Path jsonFile) {
        return read(jsonFile, DefaultContingencyList.class);
    }

    private static <T> void write(T object, Path jsonFile) {
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
    void roundTripTest() throws IOException {
        roundTripTest(create(), ContingencyJsonTest::write, ContingencyJsonTest::readContingency, "/contingency.json");
    }

    @Test
    void readJsonList() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/contingencies.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals(2, contingencies.get(0).getElements().size());
        assertEquals("contingency2", contingencies.get(1).getId());
        assertEquals(1, contingencies.get(1).getElements().size());

        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingencies.json");
    }

    @Test
    void readJsonListWithBatteryContingency() throws IOException {
        Network network = BatteryNetworkFactory.create();

        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/contingenciesBatteries.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(4, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals(2, contingencies.get(0).getElements().size());
        assertEquals("contingency2", contingencies.get(1).getId());
        assertEquals(1, contingencies.get(1).getElements().size());
        assertEquals("contingency3", contingencies.get(2).getId());
        assertEquals(1, contingencies.get(2).getElements().size());
        assertEquals("contingency4", contingencies.get(3).getId());
        assertEquals(1, contingencies.get(3).getElements().size());

        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingenciesBatteries.json");
    }

    @Test
    void readJsonListWithSwitchHvdcSvcShuntBbsTwtContingency() throws IOException {
        Network network = FourSubstationsNodeBreakerFactory.create();

        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/contingenciesWithSeveralElements.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(6, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals(1, contingencies.get(0).getElements().size());
        assertEquals(HVDC_LINE, contingencies.get(0).getElements().get(0).getType());
        assertEquals("contingency2", contingencies.get(1).getId());
        assertEquals(1, contingencies.get(1).getElements().size());
        assertEquals(TWO_WINDINGS_TRANSFORMER, contingencies.get(1).getElements().get(0).getType());
        assertEquals("contingency3", contingencies.get(2).getId());
        assertEquals(1, contingencies.get(2).getElements().size());
        assertEquals(SHUNT_COMPENSATOR, contingencies.get(2).getElements().get(0).getType());
        assertEquals("contingency4", contingencies.get(3).getId());
        assertEquals(1, contingencies.get(3).getElements().size());
        assertEquals(STATIC_VAR_COMPENSATOR, contingencies.get(3).getElements().get(0).getType());
        assertEquals("contingency5", contingencies.get(4).getId());
        assertEquals(1, contingencies.get(4).getElements().size());
        assertEquals(BUSBAR_SECTION, contingencies.get(4).getElements().get(0).getType());
        assertEquals("contingency6", contingencies.get(5).getId());
        assertEquals(1, contingencies.get(5).getElements().size());
        assertEquals(SWITCH, contingencies.get(5).getElements().get(0).getType());

        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingenciesWithSeveralElements.json");
    }

    @Test
    void readJsonListWithTwt3Contingency() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/contingenciesWith3wt.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals(1, contingencies.get(0).getElements().size());
        assertEquals(THREE_WINDINGS_TRANSFORMER, contingencies.get(0).getElements().get(0).getType());
        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingenciesWith3wt.json");
    }

    @Test
    void readJsonListWithTieLineAndDanglingLineContingency() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/contingenciesWithDlAndTl.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals(1, contingencies.get(0).getElements().size());
        assertEquals(DANGLING_LINE, contingencies.get(0).getElements().get(0).getType());
        assertEquals("contingency2", contingencies.get(1).getId());
        assertEquals(1, contingencies.get(1).getElements().size());
        assertEquals(TIE_LINE, contingencies.get(1).getElements().get(0).getType());
        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingenciesWithDlAndTl.json");
    }

    @Test
    void readNotDefaultJsonList() throws IOException {
        Files.copy(getClass().getResourceAsStream("/identifierContingencyList.json"), fileSystem.getPath("/identifierContingencyList.json"));

        ContingencyList contingencyList = ContingencyList.load(fileSystem.getPath("/identifierContingencyList.json"));
        assertEquals("list1", contingencyList.getName());
        assertEquals("identifier", contingencyList.getType());
    }

    @Test
    void readJsonListContingenciesWithOptionalName() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        DefaultContingencyList contingencyList = (DefaultContingencyList) ContingencyList.load(fileSystem.getPath("/contingenciesWithOptionalName.json"));
        assertEquals("list", contingencyList.getName());

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals("contingency name", contingencies.get(0).getName().get());
        assertEquals(2, contingencies.get(0).getElements().size());
        assertEquals("contingency2", contingencies.get(1).getId());
        assertEquals(1, contingencies.get(1).getElements().size());

        roundTripTest(contingencyList, ContingencyJsonTest::write, ContingencyJsonTest::readContingencyList, "/contingenciesWithOptionalName.json");
    }

    static class DummyExtension extends AbstractExtension<Contingency> {

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
