/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class LoadFlowDefaultParametersLoaderTest {

    @Test
    void testLoadParametersFromClassPath() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader));

        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(1, extensions.size());
        JsonLoadFlowParametersTest.DummyExtension dummyExtension = (JsonLoadFlowParametersTest.DummyExtension) extensions.get(0);
        assertEquals(5, dummyExtension.getParameterDouble());
    }

    @Test
    void testConflictBetweenDefaultParametersLoader() {
        LoadFlowDefaultParametersLoaderMock loader1 = new LoadFlowDefaultParametersLoaderMock("test1");
        LoadFlowDefaultParametersLoaderMock loader2 = new LoadFlowDefaultParametersLoaderMock("test2");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader1, loader2));
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(0, extensions.size());
    }
}
