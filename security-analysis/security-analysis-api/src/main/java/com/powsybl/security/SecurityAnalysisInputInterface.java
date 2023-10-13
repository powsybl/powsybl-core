package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;

public interface SecurityAnalysisInputInterface {

    SecurityAnalysisInputInterface setContingencies(ContingenciesProvider contingencies);
}
