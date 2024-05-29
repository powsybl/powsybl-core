package com.powsybl.loadflow;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadFlowDefaultParametersLoaderTest {

    @Test
    void testLoadParametersFromClassPath() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test",
                "/LoadFlowParametersUpdate.json");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader));

        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(1, extensions.size());
        JsonLoadFlowParametersTest.DummyExtension dummyExtension = (JsonLoadFlowParametersTest.DummyExtension) extensions.get(0);
        assertEquals(5, dummyExtension.getParameterDouble());
    }

    @Test
    void testConflictBetweenDefaultParametersLoader() {
        LoadFlowDefaultParametersLoaderMock loader1 = new LoadFlowDefaultParametersLoaderMock("test1",
                "/LoadFlowParametersUpdate.json");
        LoadFlowDefaultParametersLoaderMock loader2 = new LoadFlowDefaultParametersLoaderMock("test2",
                "/LoadFlowParametersUpdate.json");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader1, loader2));
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(0, extensions.size());
    }
}
