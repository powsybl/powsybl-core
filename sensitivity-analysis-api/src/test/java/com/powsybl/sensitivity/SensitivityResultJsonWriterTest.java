/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.condition.TrueCondition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SensitivityResultJsonWriterTest {

    @Test
    void test() throws IOException {
        List<Contingency> contingencies = List.of(Contingency.line("NHV1_NHV2_2"));
        List<OperatorStrategy> operatorStrategies = List.of(new OperatorStrategy("Open NHV1_NHV2_2", ContingencyContext.all(),
                new TrueCondition(), List.of("Open branch NHV1_NHV2_2")));
        try (Writer writer = new StringWriter()) {
            try (JsonGenerator generator = JsonUtil.createJsonFactory().createGenerator(writer).useDefaultPrettyPrinter();
                SensitivityResultJsonWriter sensiWriter = new SensitivityResultJsonWriter(generator, contingencies, operatorStrategies)) {
                sensiWriter.writeStateStatus(-1, -1, SensitivityAnalysisResult.Status.SUCCESS);
                sensiWriter.writeSensitivityValue(0, -1, -1, 1d, 2d);
                sensiWriter.writeStateStatus(0, 0, SensitivityAnalysisResult.Status.SUCCESS);
                sensiWriter.writeSensitivityValue(1, 0, 0, 3d, 4d);
            }
            writer.flush();
            assertEquals(new String(ByteStreams.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/SensitivityResultJsonWriter.json"))), StandardCharsets.UTF_8),
                    writer.toString());
        }
    }
}
