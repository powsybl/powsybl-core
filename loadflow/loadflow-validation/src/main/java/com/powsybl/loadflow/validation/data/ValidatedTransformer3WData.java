package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.util.TwtData;

import java.util.Objects;

public record ValidatedTransformer3WData(String twtId, TwtData twtData, boolean validated) {
    public ValidatedTransformer3WData {
        Objects.requireNonNull(twtId);
    }
}
