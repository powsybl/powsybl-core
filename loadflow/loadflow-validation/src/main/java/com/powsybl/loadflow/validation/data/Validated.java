package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record Validated<T>(T data, boolean validated) {
    public Validated {
        Objects.requireNonNull(data);
    }
}
