package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.util.TwtData;

import java.util.Objects;

public record Transformer3WData(String twtId, TwtData twtData) {
    public Transformer3WData {
        Objects.requireNonNull(twtId);
    }
}
