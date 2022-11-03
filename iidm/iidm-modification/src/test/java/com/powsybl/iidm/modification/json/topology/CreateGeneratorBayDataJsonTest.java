/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.json.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.modification.data.topology.CreateGeneratorBayData;
import com.powsybl.iidm.modification.json.NetworkModificationDataJsonModule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.commons.ComparisonUtils.compareTxt;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataJsonTest extends AbstractConverterTest {

    private final ObjectMapper objectMapper = JsonUtil.createObjectMapper()
            .registerModule(new NetworkModificationDataJsonModule());

    @Test
    public void testSerialize() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("testG")
                .setGeneratorName("testGN")
                .setGeneratorFictitious(true)
                .setGeneratorMinP(0.0)
                .setGeneratorMaxP(100.0)
                .setGeneratorRegulatingConnectableId("twt")
                .setGeneratorRegulatingSide("TWO")
                .setGeneratorVoltageRegulatorOn(true)
                .setGeneratorTargetV(225.0)
                .setGeneratorTargetP(0.0)
                .setGeneratorTargetQ(0.0)
                .setBusbarSectionId("testBBS")
                .setPositionOrder(10);
        roundTripTest(data,
                this::write, path -> JsonUtil.readJson(path, CreateGeneratorBayData.class, objectMapper),
                "/data/createGeneratorBayData.json");
    }

    @Test
    public void testCopy() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("testG")
                .setGeneratorName("testGN")
                .setGeneratorFictitious(true)
                .setGeneratorMinP(0.0)
                .setGeneratorMaxP(100.0)
                .setGeneratorRegulatingConnectableId("twt")
                .setGeneratorRegulatingSide("TWO")
                .setGeneratorVoltageRegulatorOn(true)
                .setGeneratorTargetV(225.0)
                .setGeneratorTargetP(0.0)
                .setGeneratorTargetQ(0.0)
                .setBusbarSectionId("testBBS")
                .setPositionOrder(10);
        CreateGeneratorBayData data1 = new CreateGeneratorBayData();
        data1.copy(data);
        write(data1, tmpDir.resolve("/test.json"));
        try (InputStream expected = getClass().getResourceAsStream("/data/createGeneratorBayData.json");
             InputStream is = Files.newInputStream(tmpDir.resolve("/test.json"))) {
            compareTxt(expected, is);
        }
    }

    private void write(CreateGeneratorBayData data, Path path) {
        JsonUtil.writeJson(path, data, objectMapper);
    }
}
