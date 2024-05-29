package com.powsybl.loadflow;

import java.io.InputStream;

public class LoadFlowDefaultParametersLoaderMock implements LoadFlowDefaultParametersLoader {

    private final String resourceFile;

    private final String name;

    LoadFlowDefaultParametersLoaderMock(String name, String resourceFilePath) {
        this.name = name;
        this.resourceFile = resourceFilePath;
    }

    @Override
    public String getSourceName() {
        return name;
    }

    @Override
    public InputStream loadDefaultParametersFromFile() {
        return getClass().getResourceAsStream(resourceFile);
    }
}
