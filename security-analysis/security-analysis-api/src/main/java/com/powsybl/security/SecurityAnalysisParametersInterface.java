package com.powsybl.security;

import java.nio.file.Path;

public interface SecurityAnalysisParametersInterface {

    void write(Path parametersPath);

    void update(Path parametersPath);
}
