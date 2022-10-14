/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology.data;

import com.powsybl.commons.AbstractConverterTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.powsybl.commons.ComparisonUtils.compareTxt;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("testG")
                .setGeneratorName("testGN")
                .setGeneratorFictitious(true)
                .setGeneratorMinP(0.0)
                .setGeneratorMaxP(100.0)
                .setGeneratorVoltageRegulatorOn(false)
                .setGeneratorTargetP(0.0)
                .setGeneratorTargetQ(0.0)
                .setBusbarSectionId("testBBS")
                .setPositionOrder(10);
        roundTripTest(data, CreateGeneratorBayData::write, path -> {
            CreateGeneratorBayData d = new CreateGeneratorBayData();
            d.update(path);
            return d;
        }, "/data/createGeneratorBayData.json");
        CreateGeneratorBayData data1 = new CreateGeneratorBayData();
        data1.copy(data);
        data1.write(tmpDir.resolve("/test.json"));
        try (InputStream is = Files.newInputStream(tmpDir.resolve("/test.json"))) {
            compareTxt(getClass().getResourceAsStream("/data/createGeneratorBayData.json"), is);
        }
    }
}
