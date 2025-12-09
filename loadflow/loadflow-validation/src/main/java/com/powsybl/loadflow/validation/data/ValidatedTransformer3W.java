package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.util.TwtData;

import java.util.Objects;

public record ValidatedTransformer3W(String twtId, TwtData twtData, boolean validated) {
    public ValidatedTransformer3W {
        Objects.requireNonNull(twtId);
        // Note that twtDat could be null
    }
}
