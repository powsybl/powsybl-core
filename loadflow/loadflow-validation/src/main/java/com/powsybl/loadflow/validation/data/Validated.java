package com.powsybl.loadflow.validation.data;

public record Validated<T extends ValidationData>(T data, boolean validated) {
}
